package app.loveu;
import org.json.*;
/** Bound each sync pass; prioritize chat/receipts over historical bulk records. */
final class CoupleDeliveryQueue {
 static final int PER_PASS=12;
 static JSONArray page(JSONArray out)throws Exception{JSONArray result=new JSONArray();if(out==null)return result;for(boolean bulk:new boolean[]{false,true})for(int i=0;i<out.length()&&result.length()<PER_PASS;i++){JSONObject row=out.getJSONObject(i);if(row.has("bulkBatchId")==bulk)result.put(row);}return result;}
 static JSONArray page(JSONObject c,long now)throws Exception{JSONArray out=c.optJSONArray("outbox"),result=new JSONArray();if(out==null)return result;int bulkCount=0;for(boolean bulk:new boolean[]{false,true})for(int i=0;i<out.length()&&result.length()<PER_PASS;i++){JSONObject row=out.getJSONObject(i);if(row.has("bulkBatchId")!=bulk)continue;if(bulk&&(now<c.optLong("bulkNextSendAt",0)||bulkCount>=8))continue;if(bulk)bulkCount++;result.put(row);}return result;}
 static boolean hasBulk(JSONArray page)throws Exception{for(int i=0;i<page.length();i++)if(page.getJSONObject(i).has("bulkBatchId"))return true;return false;}
 static JSONObject wire(JSONObject envelope)throws Exception{JSONObject wire=new JSONObject(envelope.toString());wire.remove("bulkBatchId");return wire;}
 static void remove(JSONArray out,String id)throws Exception{for(int i=out.length()-1;i>=0;i--)if(id.equals(out.getJSONObject(i).optString("id")))out.remove(i);}
}
