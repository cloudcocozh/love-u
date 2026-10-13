package app.loveu;
import java.security.KeyPair;
import java.util.Base64;
public class CoupleCryptoTest {
 interface Attempt { void run() throws Exception; }
 static void check(boolean value){if(!value)throw new AssertionError();}
 static void rejects(Attempt attempt)throws Exception {try{attempt.run();throw new AssertionError("Accepted tampering");}catch(java.security.GeneralSecurityException|IllegalArgumentException expected){}}
 public static void main(String[] args)throws Exception {
  KeyPair a=CoupleCrypto.generateIdentity(),b=CoupleCrypto.generateIdentity(),c=CoupleCrypto.generateIdentity();
  String ap=CoupleCrypto.publicEncoded(a),bp=CoupleCrypto.publicEncoded(b),as=CoupleCrypto.privateEncoded(a),bs=CoupleCrypto.privateEncoded(b);
  check(CoupleCrypto.safetyCode(ap,bp).equals(CoupleCrypto.safetyCode(bp,ap)));
  check(!CoupleCrypto.safetyCode(ap,bp).equals(CoupleCrypto.safetyCode(ap,CoupleCrypto.publicEncoded(c))));
  CoupleCrypto.Envelope e=CoupleCrypto.encrypt(as,bp,"pair","alice","text","msg","你好 💕");
  check(CoupleCrypto.decrypt(bs,ap,"pair","alice","text","msg",e.nonce,e.ciphertext).equals("你好 💕"));
  check(!e.nonce.equals(CoupleCrypto.encrypt(as,bp,"pair","alice","text","msg","你好 💕").nonce));
  rejects(()->CoupleCrypto.decrypt(bs,ap,"other","alice","text","msg",e.nonce,e.ciphertext));
  rejects(()->CoupleCrypto.decrypt(bs,ap,"pair","bob","text","msg",e.nonce,e.ciphertext));
  rejects(()->CoupleCrypto.decrypt(bs,ap,"pair","alice","proposal","msg",e.nonce,e.ciphertext));
  rejects(()->CoupleCrypto.decrypt(bs,ap,"pair","alice","text","other",e.nonce,e.ciphertext));
  byte[] damaged=Base64.getDecoder().decode(e.ciphertext);damaged[0]^=1;
  rejects(()->CoupleCrypto.decrypt(bs,ap,"pair","alice","text","msg",e.nonce,Base64.getEncoder().encodeToString(damaged)));
  rejects(()->CoupleCrypto.decrypt(CoupleCrypto.privateEncoded(c),ap,"pair","alice","text","msg",e.nonce,e.ciphertext));
  rejects(()->CoupleCrypto.decrypt(bs,ap,"pair","alice","text","msg","AA==",e.ciphertext));
  rejects(()->CoupleCrypto.encrypt(as,bp,"","alice","text","msg","hello"));
  System.out.println("CoupleCryptoTest passed");
 }
}
