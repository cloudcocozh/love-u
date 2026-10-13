package app.loveu;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECKey;
import java.security.spec.*;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.*;

/** Device-only cryptography. Callers retain identities exclusively inside the encrypted vault. */
public final class CoupleCrypto {
 private static final SecureRandom RANDOM=new SecureRandom();
 private CoupleCrypto(){}
 public static final class Envelope {
  public final String nonce,ciphertext;
  public Envelope(String nonce,String ciphertext){this.nonce=nonce;this.ciphertext=ciphertext;}
 }
 public static KeyPair generateIdentity()throws GeneralSecurityException {
  KeyPairGenerator g=KeyPairGenerator.getInstance("EC");g.initialize(new ECGenParameterSpec("secp256r1"),RANDOM);return g.generateKeyPair();
 }
 public static String publicEncoded(KeyPair pair){return encode(pair.getPublic().getEncoded());}
 public static String privateEncoded(KeyPair pair){return encode(pair.getPrivate().getEncoded());}
 public static String safetyCode(String first,String second)throws GeneralSecurityException {
  // Validate both identities before displaying their verification fingerprint.
  publicKey(first);publicKey(second);
  String[] sorted={first,second};Arrays.sort(sorted);
  byte[] hash=MessageDigest.getInstance("SHA-256").digest(fields("love-u-safety-v1",sorted[0],sorted[1]));
  StringBuilder out=new StringBuilder();for(int i=0;i<16;i++){if(i>0&&i%2==0)out.append(' ');out.append(String.format(java.util.Locale.ROOT,"%02X",hash[i]&255));}return out.toString();
 }
 public static Envelope encrypt(String privateB64,String peerPublicB64,String pairId,String senderId,String type,String id,String plaintext)throws GeneralSecurityException {
  byte[] nonce=new byte[12];RANDOM.nextBytes(nonce);
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key(privateB64,peerPublicB64,pairId),new GCMParameterSpec(128,nonce));
  cipher.updateAAD(aad(pairId,senderId,type,id));return new Envelope(encode(nonce),encode(cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8))));
 }
 public static String decrypt(String privateB64,String peerPublicB64,String pairId,String senderId,String type,String id,String nonceB64,String ciphertextB64)throws GeneralSecurityException {
  byte[] nonce=decode(nonceB64);if(nonce.length!=12)throw new GeneralSecurityException("Invalid encrypted message");
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key(privateB64,peerPublicB64,pairId),new GCMParameterSpec(128,nonce));
  cipher.updateAAD(aad(pairId,senderId,type,id));return new String(cipher.doFinal(decode(ciphertextB64)),StandardCharsets.UTF_8);
 }
 private static SecretKeySpec key(String privateB64,String peerPublicB64,String pairId)throws GeneralSecurityException {
  required(pairId);PrivateKey local=KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(decode(privateB64)));validate(local);
  KeyAgreement agreement=KeyAgreement.getInstance("ECDH");agreement.init(local);agreement.doPhase(publicKey(peerPublicB64),true);
  byte[] shared=agreement.generateSecret();try{
   MessageDigest digest=MessageDigest.getInstance("SHA-256");digest.update(fields("love-u-ecdh-aes-v1",pairId));digest.update(shared);return new SecretKeySpec(digest.digest(),"AES");
  }finally{Arrays.fill(shared,(byte)0);}
 }
 private static PublicKey publicKey(String value)throws GeneralSecurityException {PublicKey key=KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(decode(value)));validate(key);return key;}
 private static void validate(Key key)throws GeneralSecurityException {
  if(!(key instanceof ECKey))throw new GeneralSecurityException("Invalid identity");
  AlgorithmParameters expected=AlgorithmParameters.getInstance("EC");expected.init(new ECGenParameterSpec("secp256r1"));ECParameterSpec wanted=expected.getParameterSpec(ECParameterSpec.class),actual=((ECKey)key).getParams();
  if(!wanted.getCurve().equals(actual.getCurve())||!wanted.getGenerator().equals(actual.getGenerator())||!wanted.getOrder().equals(actual.getOrder())||wanted.getCofactor()!=actual.getCofactor())throw new GeneralSecurityException("Invalid identity curve");
 }
 private static byte[] aad(String pairId,String senderId,String type,String id){required(pairId);required(senderId);required(type);required(id);return fields("love-u-message-v1",pairId,senderId,type,id);}
 private static void required(String value){if(value==null||value.isEmpty())throw new IllegalArgumentException("Missing message context");}
 private static byte[] fields(String... values){try{ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream data=new DataOutputStream(bytes);for(String value:values){byte[] encoded=value.getBytes(StandardCharsets.UTF_8);data.writeInt(encoded.length);data.write(encoded);}return bytes.toByteArray();}catch(java.io.IOException impossible){throw new AssertionError(impossible);}}
 private static byte[] decode(String value){if(value==null||value.length()>1024*1024)throw new IllegalArgumentException("Invalid encoded value");return Base64.getDecoder().decode(value);}
 private static String encode(byte[] bytes){return Base64.getEncoder().encodeToString(bytes);}
}
