package app.loveu;

import org.json.*;
import java.util.*;

/** Reviewed, immutable field projections; no network or personal-record mutation. */
final class BulkShare {
 static final class Plan {
  final String type,relationship;final boolean location;final List<JSONObject> items=new ArrayList<>();
  int total,existing,withdrawn,invalid;String first="",last="";
  Plan(String type,boolean location,JSONObject couple)throws Exception{this.type=type;this.location=location;relationship=relationship(couple);}
 }
 static JSONObject pair(JSONObject root)throws Exception{JSONObject c=root.optJSONObject("couple");if(c==null||c.optJSONObject("peer")==null||c.optString("pairId").isEmpty()||c.optString("privateKey").isEmpty()||c.optString("token").isEmpty())throw new SecurityException("请先在爱人页完成安全连接");return c;}
 static String relationship(JSONObject c)throws Exception{return c.getString("id")+"|"+c.getString("publicKey")+"|"+c.getString("pairId")+"|"+c.getJSONObject("peer").getString("id")+"|"+c.getJSONObject("peer").getString("publicKey");}
 static Plan plan(JSONObject root,String type,boolean location)throws Exception{
  if(!type.equals("sex")&&!type.equals("period"))throw new IllegalArgumentException("Unknown record type");JSONObject c=pair(root);Plan p=new Plan(type,location,c);Set<String> seen=new HashSet<>();
  for(String bucket:type.equals("sex")?new String[]{"events"}:new String[]{"menstrualSelf","menstrualPartner"}){
   JSONArray rows=root.optJSONArray(bucket);if(rows==null)continue;String subject=bucket.equals("menstrualPartner")?c.getJSONObject("peer").getString("id"):c.getString("id");
   for(int i=0;i<rows.length();i++){p.total++;try{JSONObject raw=rows.getJSONObject(i);String rawId=raw.getString("id"),source=subject+":"+rawId;if(rawId.isEmpty()||source.length()>200)throw new JSONException("Invalid source");JSONObject data=SharedCoupleRecords.snapshot(raw,type,location);String key=SharedCoupleRecords.key(type,subject,source,data);if(!seen.add(key)){p.existing++;continue;}JSONObject all=c.optJSONObject("sharedRecords"),prior=all==null?null:all.optJSONObject(key);if(prior!=null){if(prior.optBoolean("withdrawn"))p.withdrawn++;else p.existing++;continue;}
     String date=data.getString(type.equals("sex")?"date":"start");if(p.first.isEmpty()||date.compareTo(p.first)<0)p.first=date;if(p.last.isEmpty()||date.compareTo(p.last)>0)p.last=date;
     p.items.add(new JSONObject().put("bucket",bucket).put("rawId",rawId).put("subject",subject).put("source",source).put("key",key).put("data",data));
    }catch(JSONException|IllegalArgumentException bad){p.invalid++;}}
  }return p;
 }
 static String canonical(Object value)throws Exception{
  if(value instanceof JSONObject){JSONObject o=(JSONObject)value;List<String> keys=new ArrayList<>();Iterator<String> it=o.keys();while(it.hasNext())keys.add(it.next());Collections.sort(keys);StringBuilder s=new StringBuilder("{");for(String key:keys)s.append(JSONObject.quote(key)).append(':').append(canonical(o.get(key))).append(',');return s.append('}').toString();}
  if(value instanceof JSONArray){JSONArray a=(JSONArray)value;StringBuilder s=new StringBuilder("[");for(int i=0;i<a.length();i++)s.append(canonical(a.get(i))).append(',');return s.append(']').toString();}return value instanceof String?JSONObject.quote((String)value):String.valueOf(value);
 }
 static void validateCurrent(JSONObject root,Plan plan)throws Exception{
  JSONObject c=pair(root);if(!plan.relationship.equals(relationship(c)))throw new SecurityException("连接对象发生变化，请重新确认分享");Map<String,JSONObject> originals=new HashMap<>();
  for(String bucket:new String[]{"events","menstrualSelf","menstrualPartner"}){JSONArray rows=root.optJSONArray(bucket);if(rows!=null)for(int i=0;i<rows.length();i++){JSONObject row=rows.optJSONObject(i);if(row!=null){String id=bucket+":"+row.optString("id");if(originals.put(id,row)!=null)throw new IllegalStateException("记录编号重复，请先核对记录");}}}
  JSONObject shared=c.optJSONObject("sharedRecords");for(JSONObject item:plan.items){JSONObject raw=originals.get(item.getString("bucket")+":"+item.getString("rawId"));if(raw==null||!canonical(SharedCoupleRecords.snapshot(raw,plan.type,plan.location)).equals(canonical(item.getJSONObject("data"))))throw new IllegalStateException("记录在预览后发生变化，请重新确认分享");if(shared!=null&&shared.has(item.getString("key")))throw new IllegalStateException("共享状态已更新，请重新打开分享预览");}
 }
 static JSONObject queuedCouple(JSONObject root,Plan plan)throws Exception{
  validateCurrent(root,plan);JSONObject c=new JSONObject(pair(root).toString());if(plan.items.isEmpty())return c;long clock=nextClock(c);String batch=UUID.randomUUID().toString();JSONArray records=new JSONArray();
  for(JSONObject item:plan.items){if(Thread.currentThread().isInterrupted())throw new InterruptedException();String id=UUID.randomUUID().toString(),op=UUID.randomUUID().toString();JSONObject payload=new JSONObject().put("kind","sharedRecord").put("recordType",plan.type).put("subject",item.getString("subject")).put("sourceId",item.getString("source")).put("action","share").put("operationId",op).put("clock",clock++).put("includeLocation",plan.location).put("data",new JSONObject(item.getJSONObject("data").toString()));
   CoupleCrypto.Envelope sealed=CoupleCrypto.encrypt(c.getString("privateKey"),c.getJSONObject("peer").getString("publicKey"),c.getString("pairId"),c.getString("id"),"sharedRecord",id,payload.toString());SharedCoupleRecords.apply(c,payload,c.getString("id"));
   array(c,"outbox").put(new JSONObject().put("id",id).put("type","sharedRecord").put("nonce",sealed.nonce).put("ciphertext",sealed.ciphertext).put("bulkBatchId",batch));array(c,"inbox").put(new JSONObject().put("id",id).put("sender",c.getString("id")).put("payload",payload).put("at",System.currentTimeMillis()));records.put(new JSONObject().put("key",item.getString("key")).put("messageId",id).put("operationId",op));
  }
  array(c,"bulkShareJobs").put(new JSONObject().put("id",batch).put("type",plan.type).put("createdAt",System.currentTimeMillis()).put("records",records));return c;
 }
 static JSONArray array(JSONObject c,String key)throws Exception{JSONArray a=c.optJSONArray(key);if(a==null){a=new JSONArray();c.put(key,a);}return a;}
 static long nextClock(JSONObject c){long max=0;JSONObject all=c.optJSONObject("sharedRecords");if(all!=null){Iterator<String> keys=all.keys();while(keys.hasNext()){JSONObject item=all.optJSONObject(keys.next());if(item==null)continue;for(String group:new String[]{"versions","interactions"}){JSONObject values=item.optJSONObject(group);if(values==null)continue;Iterator<String> entries=values.keys();while(entries.hasNext()){Object value=values.opt(entries.next());String version=value instanceof JSONObject?((JSONObject)value).optString("version"):String.valueOf(value);try{max=Math.max(max,Long.parseLong(version.substring(0,19)));}catch(RuntimeException ignored){}}}}}if(max>=Long.MAX_VALUE-100000)throw new IllegalStateException("共享版本无法继续，请检查记录");return max+1;}
 static String progress(JSONObject c)throws Exception{
  JSONArray jobs=c.optJSONArray("bulkShareJobs");if(jobs==null||jobs.length()==0)return "";JSONObject job=jobs.getJSONObject(jobs.length()-1);JSONArray records=job.getJSONArray("records");Set<String> queued=new HashSet<>();JSONArray out=c.optJSONArray("outbox");if(out!=null)for(int i=0;i<out.length();i++)queued.add(out.getJSONObject(i).optString("id"));int pending=0,received=0;JSONObject all=c.optJSONObject("sharedRecords"),peer=c.optJSONObject("peer");
  for(int i=0;i<records.length();i++){JSONObject row=records.getJSONObject(i);if(queued.contains(row.getString("messageId")))pending++;JSONObject item=all==null?null:all.optJSONObject(row.getString("key")),receipts=item==null?null:item.optJSONObject("receipts");if(receipts!=null&&peer!=null&&receipts.optBoolean(peer.optString("id")+":"+row.getString("operationId")))received++;}
  return (job.optString("type").equals("sex")?"亲密":"经期")+"批量分享：对方确认 "+received+" / "+records.length()+" · 本机待发 "+pending;
 }
}
