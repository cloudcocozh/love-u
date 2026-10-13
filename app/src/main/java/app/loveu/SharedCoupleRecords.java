package app.loveu;
import org.json.*;import java.time.*;import java.util.*;
/** Deterministic authenticated shared projection; never touches personal data. */
public final class SharedCoupleRecords {
 private SharedCoupleRecords(){}
 public static JSONObject snapshot(JSONObject raw,String type,boolean location)throws Exception{
  JSONObject out=new JSONObject();String[] allowed="period".equals(type)?new String[]{"start","end","flow","pain"}:new String[]{"date","time","durationMillis","method","rating","protection","outcomes","positions","vocalLevel","selfOrgasm","partnerOrgasm","selfOrgasmCount","partnerOrgasmCount"};
  for(String key:allowed)if(raw.has(key)){Object value=raw.get(key);validateField(key,value);out.put(key,value instanceof JSONArray?new JSONArray(value.toString()):value);}
  if("period".equals(type)){LocalDate start=LocalDate.parse(out.getString("start"));if(!out.isNull("end")&&LocalDate.parse(out.getString("end")).isBefore(start))throw new JSONException("结束日期早于开始日期");}else LocalDate.parse(out.getString("date"));
  if(location&&"sex".equals(type))for(String key:new String[]{"place","latitude","longitude","locationAccuracy","locationCapturedAt"})if(raw.has(key)){validateField(key,raw.get(key));out.put(key,raw.get(key));}
  if("sex".equals(type)&&!location)for(String key:new String[]{"place","latitude","longitude","locationAccuracy","locationCapturedAt"})out.put(key,JSONObject.NULL);if(out.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>12000)throw new JSONException("Shared card too large");return out;
 }
 private static void validateField(String key,Object value)throws Exception{
  if(value==JSONObject.NULL)return;
  if(Arrays.asList("outcomes","positions").contains(key)){if(!(value instanceof JSONArray)||((JSONArray)value).length()>20)throw new JSONException("Invalid tags");JSONArray list=(JSONArray)value;for(int i=0;i<list.length();i++)if(!(list.get(i) instanceof String)||list.getString(i).length()>80)throw new JSONException("Invalid tag");return;}
  if(Arrays.asList("selfOrgasm","partnerOrgasm").contains(key)){if(!(value instanceof Boolean))throw new JSONException("Invalid boolean");return;}
  if(Arrays.asList("durationMillis","rating","selfOrgasmCount","partnerOrgasmCount","latitude","longitude","locationAccuracy","locationCapturedAt").contains(key)){if(!(value instanceof Number))throw new JSONException("Invalid number");double n=((Number)value).doubleValue();if(!Double.isFinite(n))throw new JSONException("Invalid number");double min="latitude".equals(key)?-90:"longitude".equals(key)?-180:0,max="latitude".equals(key)?90:"longitude".equals(key)?180:"rating".equals(key)?5:"durationMillis".equals(key)?31536000000L:"locationCapturedAt".equals(key)?Long.MAX_VALUE:1000000;if(n<min||n>max)throw new JSONException("Number outside range");return;}
  if(!(value instanceof String)||((String)value).length()>200)throw new JSONException("Invalid text");if("time".equals(key))LocalTime.parse((String)value);
 }
 public static String key(String type,String subject,String source,JSONObject data)throws Exception{return "period".equals(type)?"period:"+subject+":"+data.getString("start"):"sex:"+subject+":"+source;}
 public static void apply(JSONObject c,JSONObject p,String actor)throws Exception{
  String me=c.getString("id"),peer=c.getJSONObject("peer").getString("id");if(!actor.equals(me)&&!actor.equals(peer))throw new SecurityException("Unknown participant");
  if(!"sharedRecord".equals(p.getString("kind")))throw new JSONException("Unknown content");
  String type=p.getString("recordType"),subject=p.getString("subject"),source=p.getString("sourceId"),action=p.getString("action"),op=p.getString("operationId");long clock=p.getLong("clock");
  if((!"sex".equals(type)&&!"period".equals(type))||(!subject.equals(me)&&!subject.equals(peer))||source.length()>200||op.length()>100||op.isEmpty()||clock<1)throw new JSONException("Invalid shared operation");
  if("sex".equals(type)&&!source.startsWith(subject+":"))throw new SecurityException("Invalid record owner");
  if(!Arrays.asList("share","edit","withdraw","like","tease","comment","received").contains(action))throw new JSONException("Unknown action");if("sex".equals(type)&&Arrays.asList("share","edit","withdraw").contains(action)&&!actor.equals(subject))throw new SecurityException("Only owner may change record");JSONObject data=p.getJSONObject("data");JSONObject clean=null;if("share".equals(action)||"edit".equals(action))clean=snapshot(data,type,p.optBoolean("includeLocation",false));String text=p.optString("text");if(text.length()>1000||("comment".equals(action)&&text.trim().isEmpty()))throw new JSONException("Invalid comment");if("received".equals(action)&&(p.optString("receivedOperation").isEmpty()||p.optString("receivedOperation").length()>100))throw new JSONException("Invalid receipt");String id=key(type,subject,source,data);JSONObject all=c.optJSONObject("sharedRecords");if(all==null){all=new JSONObject();c.put("sharedRecords",all);}JSONObject item=all.optJSONObject(id);
  if(item==null){item=new JSONObject().put("id",id).put("recordType",type).put("subject",subject).put("sourceId","period".equals(type)?id:source).put("data",new JSONObject()).put("versions",new JSONObject()).put("interactions",new JSONObject()).put("receipts",new JSONObject());all.put(id,item);}
  if("withdraw".equals(action)){if(!actor.equals(subject)&&"sex".equals(type))throw new SecurityException("Only owner may withdraw");item.put("withdrawn",true);item.put("data",new JSONObject());item.put("versions",new JSONObject());item.put("interactions",new JSONObject());item.put("receipts",new JSONObject());return;}
  if(item.optBoolean("withdrawn"))return;
  if("received".equals(action)){String target=p.getString("receivedOperation");if(target.isEmpty()||target.length()>100)throw new JSONException("Invalid receipt");item.getJSONObject("receipts").put(actor+":"+target,true);return;}
  String version=String.format(Locale.ROOT,"%019d:%s:%s",clock,actor,op);
  if("share".equals(action)||"edit".equals(action)){
   if("sex".equals(type)&&!actor.equals(subject))throw new SecurityException("Only owner may edit");
   JSONObject versions=item.getJSONObject("versions"),values=item.getJSONObject("data");Iterator<String> keys=clean.keys();while(keys.hasNext()){String field=keys.next();if(version.compareTo(versions.optString(field))>0){values.put(field,clean.get(field));versions.put(field,version);}}return;
  }
  JSONObject interactions=item.getJSONObject("interactions");if("like".equals(action)||"tease".equals(action)||"comment".equals(action)){
   String entry="comment".equals(action)?actor+":"+op:actor+":"+action;JSONObject old=interactions.optJSONObject(entry);if(old==null||version.compareTo(old.optString("version"))>0)interactions.put(entry,new JSONObject().put("actor",actor).put("action",action).put("text",text).put("enabled",p.optBoolean("enabled",true)).put("version",version));return;
  }throw new JSONException("Unknown action");
 }
}






