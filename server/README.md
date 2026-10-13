# Private couple relay

Python standard library only. The relay stores account public keys, hashed bearer tokens, relationship metadata and opaque encrypted envelopes. It does not receive private keys or plaintext shared records. Request bodies and tokens are never logged by the service. Public keys are bounded base64 DER; Android validates EC P-256 keys. This is store-and-forward HTTPS relay communication.

Run tests from the repository root:

```
python -m unittest discover -s server -v
```

Local development: `python server/service.py --db server/relay.sqlite`. Default bind is 127.0.0.1:8878. No TLS is provided by this process. A trusted reverse proxy must terminate HTTPS and strip `/love-u` before forwarding the route. Health returns only `{"status":"ok"}`.

Container example (deployment requires operator action; not executed by these tests):

```
docker build -t love-u-relay server
docker run -d --name love-u-relay --restart unless-stopped --memory 128m --cpus 0.5 -p 127.0.0.1:8878:8878 -v love-u-relay-data:/data love-u-relay
```

Keep the data volume private and protected. Do not log Authorization headers at the proxy. Limit request concurrency/connection count at the proxy. Do not publish this plain HTTP port externally.

## HTTP contract

All payloads are JSON objects. Authenticated endpoints require `Authorization: Bearer <token>`. Errors are `{"error":"stable_code"}` with HTTP 400 validation, 401 auth, 404 missing, 409 conflict, 410 expiry, 413 size, 429 limit or 500 internal error. Timestamps are Unix milliseconds.

- GET `/health`: 200, `{status:"ok"}` without authentication.
- POST `/accounts` `{publicKey}`: 201 `{id,token}`. Public key is base64 DER; token is issued once and only its SHA-256 hash is stored.
- GET `/me`: 200 `{id,publicKey,pairId,peer,pending}`. `pairId`, `peer`, `pending` are null if absent. `peer` is `{id,publicKey}`. `pending` is `{peer:{id,publicKey},role:"creator"|"acceptor",expiresAt}`. Pending relationships cannot send messages.
- POST `/invites` `{}`: 201 `{code}`. 192-bit code, ten-minute lifetime. New code invalidates the creator's previous unaccepted code.
- POST `/invites/accept` `{code}`: 200 `{pending}`. Rejects self, reuse, already paired or pending accounts, expired codes.
- POST `/pair/confirm` `{peerId}`: creator only, 200 `{pairId,peer}`. The UI must verify the matching safety code before confirmation. Server cannot verify that human action.
- DELETE `/pair`: 200 `{ok:true}`. Clears both accounts' current pair and deletes all queued and acknowledged envelopes for the pair; also cancels the caller's pending invite. Does not touch clients' personal histories.
- POST `/messages` `{id,type,nonce,ciphertext}`: 202 `{accepted:true}`. Recipient is always the current partner, never supplied by caller. Nonce is base64 12 bytes, ciphertext base64 16–32768 bytes. ID max128 chars; type max40 chars. Same pair+ID and identical envelope is idempotent; changed envelope or sender is 409.
- GET `/messages`: 200 `{messages:[{id,senderId,type,nonce,ciphertext,createdAt}]}`. Returns up to100 oldest unacknowledged messages, bounded to768KiB serialized JSON for authenticated recipient. Persist locally before acknowledgement.
- POST `/messages/ack` `{ids:[...]}`: 200 `{ok:true}`. At most100 IDs, only the authenticated recipient's messages are acknowledged. Repeat acknowledgements are safe.

Bodies max64KiB; public keys max512 decoded bytes. Default rate120 requests/minute per source IP and account (health excluded). Default500 unacknowledged envelopes per recipient, 10000 retained envelopes per pair, 10000 accounts. Acknowledged envelopes remain as idempotency records until seven-day expiry. Expiry is purged on authenticated traffic; offline delivery is available during that retention period. Configure proxy connection limits and monitor volume capacity for production. This is a small private deployment, not a public multi-tenant service.

Synthetic localhost tests cover authentication, no private health data, creator-only confirmation, invite expiry/reuse/self rejection, pending state, wrong-account reads/ack, offline queue, idempotent retry before and after ack, conflicting duplicate, process restart durability, body/nonce limits, rate/queue quota, message expiry and unpair deletion. No deployment, private data upload or phone installation is performed by tests.

Deployment health check: `curl --fail https://relay.example.com/love-u/health`. Synthetic full protocol checks run with `python -m unittest discover -s server -v`; they use an isolated local database and do not upload private data.
`python server/smoke.py https://relay.example.com/love-u` performs an opt-in deployed synthetic pair/message/ack/unpair check. It creates two public synthetic accounts (account rows remain), prints no secrets, and never reads or uploads existing private history.
