"""Private ciphertext relay. Run behind TLS; never send existing private history."""
import argparse
import base64
import hashlib
import json
import re
import secrets
import sqlite3
import threading
import time
from collections import defaultdict, deque
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

class APIError(Exception):
    def __init__(self, status, code): self.status, self.code = status, code

def fail(status, code): raise APIError(status, code)
def bounded(value, size=128):
    if not isinstance(value,str) or not value or len(value)>size: fail(400,'invalid_field')
    return value

def binary(value, minimum, maximum):
    try: raw=base64.b64decode(bounded(value,maximum*2),validate=True)
    except (ValueError,TypeError): fail(400,'invalid_base64')
    if not minimum<=len(raw)<=maximum: fail(400,'invalid_binary_length')
    return value

class State:
    def __init__(self, path, rate_limit):
        self.lock=threading.RLock(); self.rate_limit=rate_limit
        self.invite_ttl=600; self.message_ttl=7*86400; self.queue_limit=500
        self.rates=defaultdict(deque)
        self.db=sqlite3.connect(path,check_same_thread=False)
        self.db.executescript('''
          PRAGMA journal_mode=WAL; PRAGMA secure_delete=ON;
          CREATE TABLE IF NOT EXISTS accounts(id TEXT PRIMARY KEY, token TEXT UNIQUE, public_key TEXT, pair_id TEXT);
          CREATE TABLE IF NOT EXISTS invites(code TEXT PRIMARY KEY, creator TEXT, acceptor TEXT, expires REAL, used INTEGER DEFAULT 0);
          CREATE TABLE IF NOT EXISTS messages(pair_id TEXT, id TEXT, sender TEXT, recipient TEXT, type TEXT, nonce TEXT, ciphertext TEXT, created REAL, ack INTEGER DEFAULT 0, PRIMARY KEY(pair_id,id));
          CREATE INDEX IF NOT EXISTS message_recipient ON messages(recipient,ack);
        ''')
        self.db.row_factory=sqlite3.Row
    def rate(self,key):
        now=time.time(); q=self.rates[key]
        while q and q[0]<now-60: q.popleft()
        if len(q)>=self.rate_limit: fail(429,'rate_limited')
        q.append(now)
        if len(self.rates)>10000:
            for k in list(self.rates):
                if not self.rates[k] or self.rates[k][-1]<now-60: del self.rates[k]
    def one(self,sql,args=()): return self.db.execute(sql,args).fetchone()
    def peer(self,a):
        if not a['pair_id']: return None
        b=self.one('SELECT * FROM accounts WHERE pair_id=? AND id!=?',(a['pair_id'],a['id']))
        return {'id':b['id'],'publicKey':b['public_key']} if b else None
    def pending(self,a):
        r=self.one('SELECT * FROM invites WHERE used=0 AND expires>? AND acceptor IS NOT NULL AND (creator=? OR acceptor=?)',(time.time(),a['id'],a['id']))
        if not r: return None
        other=r['acceptor'] if r['creator']==a['id'] else r['creator']
        b=self.one('SELECT * FROM accounts WHERE id=?',(other,))
        return {'peer':{'id':b['id'],'publicKey':b['public_key']},'role':'creator' if r['creator']==a['id'] else 'acceptor','expiresAt':int(r['expires']*1000)}
    def dispatch(self,method,path,body,token,ip):
        with self.lock:
            try:
                result=self.route(method,path,body,token,ip); self.db.commit(); return result
            except Exception:
                self.db.rollback(); raise
    def route(self,method,path,b,token,ip):
        if method=='GET' and path=='/health': return 200,{'status':'ok'}
        self.rate('ip:'+ip)
        if method=='POST' and path=='/accounts':
            key=binary(b.get('publicKey'),64,512)
            # The relay treats key DER as opaque; clients validate EC P-256.
            if self.one('SELECT count(*) n FROM accounts')['n']>=10000: fail(429,'account_quota')
            id=secrets.token_urlsafe(24); secret=secrets.token_urlsafe(32)
            self.db.execute('INSERT INTO accounts VALUES(?,?,?,NULL)',(id,hashlib.sha256(secret.encode()).hexdigest(),key))
            return 201,{'id':id,'token':secret}
        a=self.one('SELECT * FROM accounts WHERE token=?',(hashlib.sha256(token.encode()).hexdigest(),)) if token else None
        if not a: fail(401,'unauthorized')
        self.rate('account:'+a['id'])
        now=time.time()
        self.db.execute('DELETE FROM messages WHERE created<=?',(now-self.message_ttl,))
        if method=='GET' and path=='/me': return 200,{'id':a['id'],'publicKey':a['public_key'],'pairId':a['pair_id'],'peer':self.peer(a),'pending':self.pending(a)}
        if method=='POST' and path=='/invites':
            if a['pair_id'] or self.pending(a): fail(409,'already_paired_or_pending')
            self.db.execute('DELETE FROM invites WHERE creator=? AND used=0',(a['id'],))
            code=secrets.token_urlsafe(24)
            self.db.execute('INSERT INTO invites(code,creator,expires) VALUES(?,?,?)',(code,a['id'],now+self.invite_ttl))
            return 201,{'code':code}
        if method=='POST' and path=='/invites/accept':
            code=bounded(b.get('code')); r=self.one('SELECT * FROM invites WHERE code=?',(code,))
            if not r: fail(404,'invite_not_found')
            if r['used'] or r['acceptor'] or r['creator']==a['id']: fail(409,'invite_unavailable')
            if r['expires']<=now: fail(410,'invite_expired')
            creator=self.one('SELECT * FROM accounts WHERE id=?',(r['creator'],))
            if a['pair_id'] or creator['pair_id'] or self.pending(a) or self.pending(creator): fail(409,'already_paired_or_pending')
            self.db.execute('UPDATE invites SET acceptor=? WHERE code=?',(a['id'],code))
            return 200,{'pending':self.pending(a)}
        if method=='POST' and path=='/pair/confirm':
            peer=bounded(b.get('peerId'))
            r=self.one('SELECT * FROM invites WHERE creator=? AND acceptor=? AND used=0',(a['id'],peer))
            if not r: fail(409,'no_pending_pair')
            if r['expires']<=now: fail(410,'invite_expired')
            other=self.one('SELECT * FROM accounts WHERE id=?',(peer,))
            if a['pair_id'] or other['pair_id']: fail(409,'already_paired')
            pair=secrets.token_urlsafe(24)
            self.db.execute('UPDATE accounts SET pair_id=? WHERE id IN (?,?)',(pair,a['id'],peer))
            self.db.execute('UPDATE invites SET used=1 WHERE creator IN (?,?) OR acceptor IN (?,?)',(a['id'],peer,a['id'],peer))
            return 200,{'pairId':pair,'peer':{'id':peer,'publicKey':other['public_key']}}
        if method=='DELETE' and path=='/pair':
            if a['pair_id']:
                self.db.execute('DELETE FROM messages WHERE pair_id=?',(a['pair_id'],))
                self.db.execute('UPDATE accounts SET pair_id=NULL WHERE pair_id=?',(a['pair_id'],))
            self.db.execute('DELETE FROM invites WHERE creator=? OR acceptor=?',(a['id'],a['id']))
            return 200,{'ok':True}
        if method=='POST' and path=='/messages':
            id=bounded(b.get('id')); kind=bounded(b.get('type'),40)
            nonce=binary(b.get('nonce'),12,12); cipher=binary(b.get('ciphertext'),16,32768)
            if not a['pair_id']: fail(409,'not_paired')
            peer=self.peer(a)
            r=self.one('SELECT * FROM messages WHERE pair_id=? AND id=?',(a['pair_id'],id))
            if r:
                if (r['sender'],r['type'],r['nonce'],r['ciphertext'])!=(a['id'],kind,nonce,cipher): fail(409,'id_conflict')
                return 202,{'accepted':True}
            if self.one('SELECT count(*) n FROM messages WHERE recipient=? AND ack=0',(peer['id'],))['n']>=self.queue_limit: fail(429,'queue_full')
            # Retained acknowledged rows are bounded per pair as well.
            if self.one('SELECT count(*) n FROM messages WHERE pair_id=?',(a['pair_id'],))['n']>=10000: fail(429,'pair_quota')
            self.db.execute('INSERT INTO messages VALUES(?,?,?,?,?,?,?,?,0)',(a['pair_id'],id,a['id'],peer['id'],kind,nonce,cipher,now))
            return 202,{'accepted':True}
        if method=='GET' and path=='/messages':
            rows=self.db.execute('SELECT * FROM messages WHERE recipient=? AND ack=0 ORDER BY created LIMIT 100',(a['id'],)).fetchall()
            messages=[]
            encoded_size=len(b'{"messages":[]}')
            for r in rows:
                message={'id':r['id'],'senderId':r['sender'],'type':r['type'],'nonce':r['nonce'],'ciphertext':r['ciphertext'],'createdAt':int(r['created']*1000)}
                size=len(json.dumps(message,separators=(',',':')).encode())+(1 if messages else 0)
                if encoded_size+size>768*1024: break
                messages.append(message); encoded_size+=size
            return 200,{'messages':messages}
        if method=='POST' and path=='/messages/ack':
            ids=b.get('ids')
            if not isinstance(ids,list) or len(ids)>100: fail(400,'invalid_ids')
            for id in ids:
                bounded(id)
                self.db.execute('UPDATE messages SET ack=1 WHERE recipient=? AND id=?',(a['id'],id))
            return 200,{'ok':True}
        fail(404,'not_found')

class Handler(BaseHTTPRequestHandler):
    server_version='LoveURelay'
    def log_message(self,*args): pass
    def handle_one_request(self):
        self.connection.settimeout(10)
        super().handle_one_request()
    def do_GET(self): self.process()
    def do_POST(self): self.process()
    def do_DELETE(self): self.process()
    def process(self):
        try:
            if self.headers.get('Transfer-Encoding'): fail(400,'unsupported_encoding')
            try: length=int(self.headers.get('Content-Length','0'))
            except ValueError: fail(400,'invalid_length')
            if length<0 or length>65536: fail(413,'body_too_large')
            try: body=json.loads(self.rfile.read(length)) if length else {}
            except (ValueError,UnicodeError): fail(400,'invalid_json')
            if not isinstance(body,dict): fail(400,'invalid_json')
            auth=self.headers.get('Authorization','')
            token=auth[7:] if auth.startswith('Bearer ') else ''
            status,data=self.server.state.dispatch(self.command,self.path,body,token,self.client_address[0])
        except APIError as e: status,data=e.status,{'error':e.code}
        except Exception: status,data=500,{'error':'internal_error'}
        raw=json.dumps(data,separators=(',',':')).encode()
        self.send_response(status); self.send_header('Content-Type','application/json'); self.send_header('Content-Length',str(len(raw)))
        self.send_header('Cache-Control','no-store'); self.send_header('Connection','close'); self.end_headers()
        self.wfile.write(raw); self.close_connection=True

class Server(ThreadingHTTPServer):
    daemon_threads=True
    def server_close(self):
        super().server_close()
        with self.state.lock: self.state.db.close()

def make_server(host,port,db_path,rate_limit=120):
    server=Server((host,port),Handler); server.state=State(db_path,rate_limit); return server

if __name__=='__main__':
    p=argparse.ArgumentParser(); p.add_argument('--host',default='127.0.0.1'); p.add_argument('--port',type=int,default=8878); p.add_argument('--db',default='relay.sqlite')
    args=p.parse_args(); server=make_server(args.host,args.port,args.db)
    try: server.serve_forever()
    except KeyboardInterrupt: pass
    finally: server.server_close()
