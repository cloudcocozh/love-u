package app.loveu;
import org.json.*;
import java.security.*;
/** Synthetic two-identity encrypted protocol acceptance; no live accounts. */
public final class SharedEncryptedRoundTripTest {
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static JSONObject state(String self,String peer)throws Exception{return new JSONObject().put("id",self).put("peer",new JSONObject().put("id",peer)).put("events",new JSONArray().put("private:"+self)).put("menstrualSelf",new JSONArray().put("private-period:"+self));}
 static JSONObject op(String action,long n,JSONObject data)throws Exception{return new JSONObject().put("kind","sharedRecord").put("recordType","sex").put("subject","alice").put("sourceId","alice:one").put("operationId","op"+n).put("clock",n).put("action",action).put("data",data);}
 static void transfer(JSONObject local,JSONObject remote,KeyPair from,KeyPair to,JSONObject p)throws Exception{
  String sender=local.getString("id"),id=p.getString("operationId");
  CoupleCrypto.Envelope wire=CoupleCrypto.encrypt(CoupleCrypto.privateEncoded(from),CoupleCrypto.publicEncoded(to),"synthetic-pair",sender,"sharedRecord",id,p.toString());
  String clear=CoupleCrypto.decrypt(CoupleCrypto.privateEncoded(to),CoupleCrypto.publicEncoded(from),"synthetic-pair",sender,"sharedRecord",id,wire.nonce,wire.ciphertext);
  SharedCoupleRecords.apply(local,p,sender);SharedCoupleRecords.apply(remote,new JSONObject(clear),sender);
  check(local.getJSONObject("sharedRecords").toMap().equals(remote.getJSONObject("sharedRecords").toMap()),"role-swapped encrypted convergence "+p.getString("action"));
 }
 public static void main(String[] ignored)throws Exception{
  KeyPair ka=CoupleCrypto.generateIdentity(),kb=CoupleCrypto.generateIdentity();JSONObject a=state("alice","bob"),b=state("bob","alice");
  String personalA=a.getJSONArray("events").toString(),personalB=b.getJSONArray("menstrualSelf").toString();
  JSONObject raw=new JSONObject().put("date","2026-10-01").put("time","21:35:22").put("durationMillis",123456).put("note","private note").put("place","private address");
  JSONObject card=SharedCoupleRecords.snapshot(raw,"sex",false);transfer(a,b,ka,kb,op("share",1,card));
  JSONObject shared=b.getJSONObject("sharedRecords").getJSONObject("sex:alice:alice:one");check(shared.getJSONObject("data").isNull("place")&&!shared.getJSONObject("data").has("note"),"private fields excluded over wire");
  transfer(b,a,kb,ka,op("like",2,card).put("enabled",true));transfer(b,a,kb,ka,op("tease",3,card).put("enabled",true));transfer(b,a,kb,ka,op("comment",4,card).put("text","synthetic feedback"));
  check(a.getJSONObject("sharedRecords").getJSONObject("sex:alice:alice:one").getJSONObject("interactions").length()==3,"specific record thread");
  transfer(a,b,ka,kb,op("withdraw",5,card));transfer(b,a,kb,ka,op("comment",6,card).put("text","late synthetic comment"));
  check(a.getJSONObject("sharedRecords").getJSONObject("sex:alice:alice:one").optBoolean("withdrawn"),"withdraw cannot resurrect");
  check(personalA.equals(a.getJSONArray("events").toString())&&personalB.equals(b.getJSONArray("menstrualSelf").toString()),"both private histories retained");
  System.out.println("PASS two independent identities encrypted share/reactions/comments/withdraw and personal retention (synthetic JVM; not real phones)");
 }
}
