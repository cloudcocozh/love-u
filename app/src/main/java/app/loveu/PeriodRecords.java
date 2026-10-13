package app.loveu;

import org.json.*;
import java.util.*;

/** Read-only union for analysis: shared data never overwrites the personal record. */
final class PeriodRecords {
 static List<PeriodAnalysis.Entry> entries(JSONObject root,boolean partner){
  TreeMap<String,PeriodAnalysis.Entry> dates=new TreeMap<>();JSONObject couple=root.optJSONObject("couple");
  if(couple!=null){JSONObject peer=couple.optJSONObject("peer"),shared=couple.optJSONObject("sharedRecords");String subject=partner?(peer==null?"":peer.optString("id")):couple.optString("id");
   if(!subject.isEmpty()&&shared!=null){Iterator<String> keys=shared.keys();while(keys.hasNext()){JSONObject item=shared.optJSONObject(keys.next());if(item!=null&&!item.optBoolean("withdrawn")&&"period".equals(item.optString("recordType"))&&subject.equals(item.optString("subject")))add(dates,item.optJSONObject("data"));}}
  }
  JSONArray local=root.optJSONArray(partner?"menstrualPartner":"menstrualSelf");if(local!=null)for(int i=0;i<local.length();i++)add(dates,local.optJSONObject(i));return new ArrayList<>(dates.values());
 }
 static void add(Map<String,PeriodAnalysis.Entry> values,JSONObject row){if(row!=null&&!row.optString("start").isEmpty())values.put(row.optString("start"),new PeriodAnalysis.Entry(row.optString("start"),row.isNull("end")?null:row.optString("end")));}
}
