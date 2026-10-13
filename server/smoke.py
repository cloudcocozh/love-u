"""Opt-in synthetic deployed relay check. Creates two test accounts, prints no secrets."""
import argparse
import base64
import json
import secrets
import urllib.request


def check(endpoint):
    def request(method,path,body=None,token=None):
        headers={'Content-Type':'application/json'}
        if token: headers['Authorization']='Bearer '+token
        data=None if body is None else json.dumps(body).encode()
        req=urllib.request.Request(endpoint.rstrip('/')+path,data=data,headers=headers,method=method)
        with urllib.request.urlopen(req,timeout=15) as response: return json.load(response)
    assert request('GET','/health')=={'status':'ok'}
    # Valid P-256 generator point in X.509 SubjectPublicKeyInfo DER, public synthetic data.
    der=bytes.fromhex('3059301306072a8648ce3d020106082a8648ce3d03010703420004'
        '6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296'
        '4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5')
    key=base64.b64encode(der).decode()
    a=request('POST','/accounts',{'publicKey':key})
    b=request('POST','/accounts',{'publicKey':key})
    try:
        invite=request('POST','/invites',{},a['token'])
        request('POST','/invites/accept',{'code':invite['code']},b['token'])
        request('POST','/pair/confirm',{'peerId':b['id']},a['token'])
        id=secrets.token_urlsafe(16)
        envelope={'id':id,'type':'synthetic-test','nonce':base64.b64encode(secrets.token_bytes(12)).decode(),'ciphertext':base64.b64encode(secrets.token_bytes(32)).decode()}
        for _ in range(2): assert request('POST','/messages',envelope,a['token'])['accepted']
        inbox=request('GET','/messages',token=b['token'])['messages']
        assert len(inbox)==1 and inbox[0]['id']==id and inbox[0]['senderId']==a['id']
        assert inbox[0]['ciphertext']==envelope['ciphertext']
        request('POST','/messages/ack',{'ids':[id]},b['token'])
        assert request('GET','/messages',token=b['token'])['messages']==[]
    finally:
        request('DELETE','/pair',token=a['token'])
    assert request('GET','/me',token=b['token'])['pairId'] is None
    print('PASS: deployed health, synthetic pair, offline queue, idempotency, ack and unpair')

if __name__=='__main__':
    parser=argparse.ArgumentParser(); parser.add_argument('endpoint'); args=parser.parse_args()
    if not args.endpoint.startswith('https://'): parser.error('deployed endpoint must use HTTPS')
    check(args.endpoint)
