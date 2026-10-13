import base64
import json
import tempfile
import os
import uuid
import shutil
import threading
import time
import unittest
import urllib.request
import urllib.error
from service import make_server

KEY = base64.b64encode(b'0' * 91).decode()
NONCE = base64.b64encode(b'0' * 12).decode()
CIPHER = base64.b64encode(b'x' * 32).decode()

class RelayTests(unittest.TestCase):
    def setUp(self):
        # Linux/container tests always use OS temp, never the read-only /app tree.
        # Windows sandbox disallows TemporaryDirectory's restrictive chmod;
        # create the same isolated OS-temp directory with inherited ACLs there.
        self.temp_directory = None
        if os.name == 'nt':
            self.tmp = os.path.join(tempfile.gettempdir(), 'love-u-test-' + uuid.uuid4().hex)
            os.mkdir(self.tmp)
        else:
            self.temp_directory = tempfile.TemporaryDirectory(prefix='love-u-test-')
            self.tmp = self.temp_directory.name
        self.server = make_server('127.0.0.1', 0, self.tmp + '/db.sqlite', rate_limit=1000)
        self.thread = threading.Thread(target=self.server.serve_forever, daemon=True)
        self.thread.start()
        self.url = 'http://127.0.0.1:%d' % self.server.server_port
    def tearDown(self):
        self.server.shutdown(); self.server.server_close(); self.thread.join()
        if self.temp_directory is not None:
            self.temp_directory.cleanup()
        else:
            shutil.rmtree(self.tmp)
    def req(self, method, path, body=None, token=None):
        headers = {'Content-Type':'application/json'}
        if token: headers['Authorization'] = 'Bearer ' + token
        req = urllib.request.Request(self.url + path, data=None if body is None else json.dumps(body).encode(), headers=headers, method=method)
        try:
            with urllib.request.urlopen(req) as r: return r.status, json.load(r)
        except urllib.error.HTTPError as e:
            with e: return e.code, json.load(e)
    def account(self):
        status, result = self.req('POST','/accounts',{'publicKey':KEY})
        self.assertEqual(status,201); return result
    def pair(self,a,b):
        s,i=self.req('POST','/invites',{},a['token']); self.assertEqual(s,201)
        self.assertEqual(self.req('POST','/invites/accept',{'code':i['code']},a['token'])[0],409)
        self.assertEqual(self.req('POST','/invites/accept',{'code':i['code']},b['token'])[0],200)
        self.assertEqual(self.req('POST','/pair/confirm',{'peerId':b['id']},a['token'])[0],200)
        return i
    def msg(self,id='m1'): return {'id':id,'type':'text','nonce':NONCE,'ciphertext':CIPHER}
    def test_auth_health_and_private_delivery_idempotency(self):
        self.assertEqual(self.req('GET','/health')[1],{'status':'ok'})
        self.assertEqual(self.req('GET','/me')[0],401)
        a,b,c=self.account(),self.account(),self.account()
        self.assertEqual(self.req('POST','/messages',self.msg(),a['token'])[0],409)
        invite=self.pair(a,b)
        self.assertEqual(self.req('POST','/invites/accept',{'code':invite['code']},c['token'])[0],409)
        for _ in range(2): self.assertEqual(self.req('POST','/messages',self.msg(),a['token'])[0],202)
        self.assertEqual(self.req('POST','/messages',{**self.msg(),'ciphertext':base64.b64encode(b'y'*32).decode()},a['token'])[0],409)
        messages=self.req('GET','/messages',token=b['token'])[1]['messages']
        self.assertEqual(len(messages),1); self.assertEqual(messages[0]['senderId'],a['id'])
        self.assertEqual(self.req('GET','/messages',token=c['token'])[1]['messages'],[])
        self.req('POST','/messages/ack',{'ids':['m1']},c['token'])
        self.assertEqual(len(self.req('GET','/messages',token=b['token'])[1]['messages']),1)
        self.req('POST','/messages/ack',{'ids':['m1']},b['token'])
        self.assertEqual(self.req('POST','/messages',self.msg(),a['token'])[0],202)
        self.assertEqual(self.req('GET','/messages',token=b['token'])[1]['messages'],[])
    def test_unpair_wipes_queue_and_pair_state(self):
        a,b=self.account(),self.account(); self.pair(a,b)
        self.req('POST','/messages',self.msg(),a['token'])
        self.assertEqual(self.req('DELETE','/pair',token=b['token'])[0],200)
        self.assertIsNone(self.req('GET','/me',token=a['token'])[1]['pairId'])
        self.assertEqual(self.req('GET','/messages',token=b['token'])[1]['messages'],[])
    def test_expiry_pending_and_quota(self):
        a,b=self.account(),self.account()
        self.server.state.invite_ttl=0
        code=self.req('POST','/invites',{},a['token'])[1]['code']
        self.assertEqual(self.req('POST','/invites/accept',{'code':code},b['token'])[0],410)
        self.server.state.invite_ttl=600; self.pair(a,b)
        self.server.state.queue_limit=1
        self.assertEqual(self.req('POST','/messages',self.msg(),a['token'])[0],202)
        self.assertEqual(self.req('POST','/messages',self.msg('m2'),a['token'])[0],429)
        self.server.state.message_ttl=0
        self.assertEqual(self.req('GET','/messages',token=b['token'])[1]['messages'],[])
    def test_pending_requires_creator_confirmation_and_expiry(self):
        a,b,c=self.account(),self.account(),self.account()
        code=self.req('POST','/invites',{},a['token'])[1]['code']
        self.req('POST','/invites/accept',{'code':code},b['token'])
        pa=self.req('GET','/me',token=a['token'])[1]
        self.assertIsNone(pa['pairId']); self.assertEqual(pa['pending']['role'],'creator')
        self.assertEqual(pa['pending']['peer']['id'],b['id'])
        self.assertEqual(self.req('POST','/pair/confirm',{'peerId':a['id']},b['token'])[0],409)
        self.assertEqual(self.req('POST','/pair/confirm',{'peerId':c['id']},a['token'])[0],409)
        self.assertEqual(self.req('POST','/messages',self.msg(),b['token'])[0],409)
        with self.server.state.lock:
            self.server.state.db.execute('UPDATE invites SET expires=?',(time.time()-1,))
            self.server.state.db.commit()
        self.assertIsNone(self.req('GET','/me',token=a['token'])[1]['pending'])
        self.assertEqual(self.req('POST','/pair/confirm',{'peerId':b['id']},a['token'])[0],410)
    def test_restart_preserves_ciphertext_queue(self):
        a,b=self.account(),self.account(); self.pair(a,b)
        self.req('POST','/messages',self.msg(),a['token'])
        self.server.shutdown(); self.server.server_close(); self.thread.join()
        self.server=make_server('127.0.0.1',0,self.tmp+'/db.sqlite',rate_limit=1000)
        self.thread=threading.Thread(target=self.server.serve_forever,daemon=True); self.thread.start()
        self.url='http://127.0.0.1:%d' % self.server.server_port
        self.assertEqual(self.req('GET','/messages',token=b['token'])[1]['messages'][0]['ciphertext'],CIPHER)
    def test_large_queue_is_byte_bounded_and_drains_after_ack(self):
        a,b=self.account(),self.account(); self.pair(a,b)
        large=base64.b64encode(b'x'*32768).decode()
        for n in range(30):
            self.assertEqual(self.req('POST','/messages',{**self.msg('large-%d'%n),'ciphertext':large},a['token'])[0],202)
        page=self.req('GET','/messages',token=b['token'])[1]
        self.assertLessEqual(len(json.dumps(page,separators=(',',':')).encode()),768*1024)
        self.assertGreater(len(page['messages']),0); self.assertLess(len(page['messages']),30)
        seen=set()
        while page['messages']:
            ids=[m['id'] for m in page['messages']]
            self.assertFalse(seen.intersection(ids)); seen.update(ids)
            self.req('POST','/messages/ack',{'ids':ids},b['token'])
            page=self.req('GET','/messages',token=b['token'])[1]
        self.assertEqual(len(seen),30)
    def test_concurrent_retries_and_cancelled_pending_invite(self):
        from concurrent.futures import ThreadPoolExecutor
        a,b=self.account(),self.account()
        code=self.req('POST','/invites',{},a['token'])[1]['code']
        self.assertEqual(self.req('POST','/invites/accept',{'code':code},b['token'])[0],200)
        self.assertEqual(self.req('DELETE','/pair',token=b['token'])[0],200)
        self.assertIsNone(self.req('GET','/me',token=a['token'])[1]['pending'])
        self.assertEqual(self.req('POST','/pair/confirm',{'peerId':b['id']},a['token'])[0],409)
        self.pair(a,b)
        with ThreadPoolExecutor(max_workers=8) as pool:
            results=list(pool.map(lambda _: self.req('POST','/messages',self.msg('retry'),a['token'])[0],range(16)))
        self.assertEqual(results,[202]*16)
        inbox=self.req('GET','/messages',token=b['token'])[1]['messages']
        self.assertEqual([m['id'] for m in inbox],['retry'])
        self.assertEqual(self.req('DELETE','/pair',token=a['token'])[0],200)
        self.assertEqual(self.req('POST','/messages',self.msg('retry'),a['token'])[0],409)
        self.assertEqual(self.req('GET','/messages',token=b['token'])[1]['messages'],[])
    def test_unacknowledged_delivery_repeats_and_ack_retry_is_safe(self):
        a,b=self.account(),self.account(); self.pair(a,b)
        self.assertEqual(self.req('POST','/messages',self.msg('offline'),a['token'])[0],202)
        first=self.req('GET','/messages',token=b['token'])[1]['messages']
        # Lost response / receiver crash before ACK must not consume the envelope.
        second=self.req('GET','/messages',token=b['token'])[1]['messages']
        self.assertEqual(first,second); self.assertEqual(len(second),1)
        # Sender cannot acknowledge its own outbound envelope for the recipient.
        self.assertEqual(self.req('POST','/messages/ack',{'ids':['offline']},a['token'])[0],200)
        self.assertEqual(self.req('GET','/messages',token=b['token'])[1]['messages'],first)
        for _ in range(2):
            self.assertEqual(self.req('POST','/messages/ack',{'ids':['offline']},b['token'])[0],200)
        self.assertEqual(self.req('GET','/messages',token=b['token'])[1]['messages'],[])
        self.assertEqual(self.req('POST','/messages',self.msg('offline'),a['token'])[0],202)
        self.assertEqual(self.req('GET','/messages',token=b['token'])[1]['messages'],[])
    def test_full_outbound_queue_does_not_block_opposite_inbox(self):
        a,b=self.account(),self.account(); self.pair(a,b)
        self.server.state.queue_limit=1
        self.assertEqual(self.req('POST','/messages',self.msg('to-b'),a['token'])[0],202)
        self.assertEqual(self.req('POST','/messages',self.msg('to-a'),b['token'])[0],202)
        self.assertEqual(self.req('POST','/messages',self.msg('blocked'),a['token'])[0],429)
        incoming=self.req('GET','/messages',token=a['token'])[1]['messages']
        self.assertEqual([m['id'] for m in incoming],['to-a'])
        self.assertEqual(self.req('POST','/messages/ack',{'ids':['to-a']},a['token'])[0],200)
        self.assertEqual(self.req('GET','/messages',token=a['token'])[1]['messages'],[])
        self.assertEqual(self.req('POST','/messages',self.msg('blocked'),a['token'])[0],429)
        self.req('POST','/messages/ack',{'ids':['to-b']},b['token'])
        self.assertEqual(self.req('POST','/messages',self.msg('blocked'),a['token'])[0],202)
        self.assertEqual([m['id'] for m in self.req('GET','/messages',token=b['token'])[1]['messages']],['blocked'])
    def test_identical_message_ids_are_isolated_between_pairs(self):
        a,b,c,d=[self.account() for _ in range(4)]
        self.pair(a,b); self.pair(c,d)
        self.assertEqual(self.req('POST','/messages',self.msg('same-id'),a['token'])[0],202)
        other={**self.msg('same-id'),'ciphertext':base64.b64encode(b'z'*32).decode()}
        self.assertEqual(self.req('POST','/messages',other,c['token'])[0],202)
        self.req('POST','/messages/ack',{'ids':['same-id']},b['token'])
        self.assertEqual(self.req('GET','/messages',token=b['token'])[1]['messages'],[])
        untouched=self.req('GET','/messages',token=d['token'])[1]['messages']
        self.assertEqual(len(untouched),1); self.assertEqual(untouched[0]['ciphertext'],other['ciphertext'])
        self.assertEqual(untouched[0]['senderId'],c['id'])
        self.req('DELETE','/pair',token=a['token'])
        self.assertEqual(self.req('GET','/messages',token=d['token'])[1]['messages'],untouched)
        self.assertIsNotNone(self.req('GET','/me',token=c['token'])[1]['pairId'])
    def test_limits_and_validation(self):
        a=self.account()
        self.assertEqual(self.req('POST','/messages',{**self.msg(),'nonce':'bad'},a['token'])[0],400)
        self.assertEqual(self.req('POST','/messages',{'blob':'x'*70000},a['token'])[0],413)
        self.server.state.rate_limit=1
        self.assertEqual(self.req('GET','/me',token=a['token'])[0],429)

if __name__ == '__main__': unittest.main()
