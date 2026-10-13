package app.loveu;

import org.json.*;
import java.time.*;
import java.util.*;

/** Shared proposal transitions, isolated from UI and personal-event persistence. */
public final class CoupleProposal {
 private CoupleProposal(){}
 public static void apply(JSONObject c,JSONObject p,String sender)throws Exception{
  if(!sender.equals(c.optString("id"))&&(c.optJSONObject("peer")==null||!sender.equals(c.getJSONObject("peer").optString("id"))))throw new SecurityException("Unknown participant");
  String kind=p.getString("kind");if("text".equals(kind)){if(p.getString("text").length()>4000)throw new JSONException("Message too long");return;}if(!"appointment".equals(kind)&&!"game".equals(kind))throw new JSONException("Unknown content");JSONObject items=c.optJSONObject("items");if(items==null){items=new JSONObject();c.put("items",items);}String id=p.getString("itemId"),action=p.getString("action");JSONObject item=items.optJSONObject(id);
  if("propose".equals(action)){if(item!=null)return;JSONArray labels=p.getJSONArray("labels");if(labels.length()<1||labels.length()>12)throw new JSONException("Invalid labels");for(int i=0;i<labels.length();i++)if(labels.getString(i).length()>40)throw new JSONException("Label too long");if("appointment".equals(kind)){LocalDate.parse(p.getString("date"));LocalTime.parse(p.getString("time"));}item=new JSONObject(p.toString());item.put("creator",sender).put("status","待回应").put("choices",new JSONObject()).put("confirmations",new JSONObject());items.put(id,item);String replaced=p.optString("replaces");JSONObject oldItem=items.optJSONObject(replaced);if(oldItem!=null&&kind.equals(oldItem.optString("kind")))oldItem.put("status","已修改");return;}
  if(item==null||!kind.equals(item.optString("kind")))throw new JSONException("Unknown proposal");if("choices".equals(action)){JSONArray selected=p.getJSONArray("selected"),labels=item.getJSONArray("labels");Set<String> allowed=new HashSet<>();for(int i=0;i<labels.length();i++)allowed.add(labels.getString(i));for(int i=0;i<selected.length();i++)if(!allowed.contains(selected.getString(i)))throw new JSONException("Invalid selection");if(rank(item.optString("status"))>=2)return;
   JSONArray previous=item.getJSONObject("choices").optJSONArray(sender);
   if(previous==null||!sameSelection(previous,selected)){JSONObject locks=item.optJSONObject("confirmations");if(locks!=null&&locks.has(sender))return;item.remove("pendingCompletion");item.getJSONObject("choices").put(sender,selected);item.put("confirmations",new JSONObject()).put("status","待回应");}return;}
  String old=item.optString("status"),next;switch(action){case "confirm":if(item.getJSONObject("choices").length()!=2)throw new JSONException("Both choices required");if(rank(old)>=2)return;
   JSONObject snapshot=p.optJSONObject("confirmationChoices");if(snapshot==null||!sameChoices(item.getJSONObject("choices"),snapshot))return;
   JSONObject confirmations=item.optJSONObject("confirmations");if(confirmations==null){confirmations=new JSONObject();item.put("confirmations",confirmations);}confirmations.put(sender,true);if(confirmations.length()<2)return;JSONObject completion=item.optJSONObject("pendingCompletion");next=completion!=null&&sameChoices(item.getJSONObject("choices"),completion)?"已完成":"已确认";item.remove("pendingCompletion");break;case "cancel":next="已取消";break;case "skip":next="已跳过";break;case "complete":if(rank(old)>=2)return;JSONObject completedChoices=p.optJSONObject("confirmationChoices");if(completedChoices==null)throw new JSONException("Completion snapshot required");if(!sameChoices(item.getJSONObject("choices"),completedChoices))return;if(!"已确认".equals(old)){item.put("pendingCompletion",new JSONObject(completedChoices.toString()));return;}next="已完成";break;default:throw new JSONException("Unknown action");}
  // Concurrent offline decisions converge: a decline wins over confirmation/completion.
  if(rank(next)>rank(old))item.put("status",next);
 }
 private static int rank(String phase){switch(phase){case "已确认":return 1;case "已完成":return 2;case "已跳过":return 3;case "已取消":return 4;case "已修改":return 5;default:return 0;}}
 private static boolean sameSelection(JSONArray first,JSONArray second){Set<String> a=new HashSet<>(),b=new HashSet<>();for(int i=0;i<first.length();i++)a.add(first.optString(i));for(int i=0;i<second.length();i++)b.add(second.optString(i));return a.equals(b);}
 private static boolean sameChoices(JSONObject current,JSONObject snapshot){if(current.length()!=2||snapshot.length()!=2)return false;Iterator<String> ids=current.keys();while(ids.hasNext()){String id=ids.next();JSONArray a=current.optJSONArray(id),b=snapshot.optJSONArray(id);if(a==null||b==null||!sameSelection(a,b))return false;}return true;}
}
