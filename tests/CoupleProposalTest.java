package app.loveu;

import org.json.*;

/** Runs the exact production state transitions using synthetic participants only. */
public final class CoupleProposalTest {
 interface Attempt {void run()throws Exception;}
 static void rejected(Attempt action)throws Exception{try{action.run();throw new AssertionError("invalid transition accepted");}catch(JSONException|SecurityException expected){}}
 static JSONObject state()throws Exception{return new JSONObject().put("id","a").put("peer",new JSONObject().put("id","b")).put("events",new JSONArray().put("personal-record"));}
 static JSONObject message(String id,String action)throws Exception{return new JSONObject().put("kind","appointment").put("itemId",id).put("action",action);}
 static JSONObject item(JSONObject state,String id)throws Exception{return state.getJSONObject("items").getJSONObject(id);}
 static void phase(JSONObject state,String id,String expected)throws Exception{if(!expected.equals(item(state,id).getString("status")))throw new AssertionError("expected "+expected+", got "+item(state,id));}
 static JSONObject proposal(String id)throws Exception{return message(id,"propose").put("date","2026-10-11").put("time","19:00").put("labels",new JSONArray().put("散步").put("音乐"));}
 static JSONObject selection(String id,String label)throws Exception{return message(id,"choices").put("selected",new JSONArray().put(label));}
 static JSONObject confirm(JSONObject state,String id)throws Exception{return message(id,"confirm").put("confirmationChoices",new JSONObject(item(state,id).getJSONObject("choices").toString()));}
 public static void main(String[] args)throws Exception{
  JSONObject s=state();CoupleProposal.apply(s,proposal("one"),"a");
  rejected(()->CoupleProposal.apply(s,confirm(s,"one"),"a"));
  rejected(()->CoupleProposal.apply(s,message("one","complete"),"a"));
  CoupleProposal.apply(s,selection("one","散步"),"a");
  rejected(()->CoupleProposal.apply(s,confirm(s,"one"),"b"));
  rejected(()->CoupleProposal.apply(s,selection("one","未知标签"),"b"));
  rejected(()->CoupleProposal.apply(s,selection("one","散步"),"outsider"));
  CoupleProposal.apply(s,selection("one","散步"),"b");
  JSONObject oldConfirm=confirm(s,"one");CoupleProposal.apply(s,oldConfirm,"a");phase(s,"one","待回应");
  CoupleProposal.apply(s,oldConfirm,"a");phase(s,"one","待回应");
  rejected(()->CoupleProposal.apply(s,message("one","complete"),"a"));
  CoupleProposal.apply(s,selection("one","音乐"),"b");phase(s,"one","待回应");
  if(item(s,"one").getJSONObject("confirmations").length()!=0)throw new AssertionError("choice edit retained confirmations");
  CoupleProposal.apply(s,oldConfirm,"a");phase(s,"one","待回应");
  if(item(s,"one").getJSONObject("confirmations").length()!=0)throw new AssertionError("stale confirmation applied");
  CoupleProposal.apply(s,new JSONObject(oldConfirm.toString()).put("action","complete"),"a");phase(s,"one","待回应");
  JSONObject reordered=new JSONObject(s.toString());CoupleProposal.apply(reordered,confirm(reordered,"one").put("action","complete"),"a");phase(reordered,"one","待回应");CoupleProposal.apply(reordered,confirm(reordered,"one"),"b");phase(reordered,"one","待回应");CoupleProposal.apply(reordered,confirm(reordered,"one"),"a");phase(reordered,"one","已完成");
  CoupleProposal.apply(s,confirm(s,"one"),"a");CoupleProposal.apply(s,confirm(s,"one"),"b");phase(s,"one","已确认");
  CoupleProposal.apply(s,selection("one","散步"),"b");phase(s,"one","已确认");if(!"音乐".equals(item(s,"one").getJSONObject("choices").getJSONArray("b").getString(0)))throw new AssertionError("confirmed participant edited choices");
  JSONObject canceledFirst=new JSONObject(s.toString());CoupleProposal.apply(canceledFirst,message("one","cancel"),"b");CoupleProposal.apply(canceledFirst,message("one","complete"),"a");phase(canceledFirst,"one","已取消");
  CoupleProposal.apply(s,confirm(s,"one").put("action","complete"),"a");phase(s,"one","已完成");
  CoupleProposal.apply(s,message("one","cancel"),"b");phase(s,"one","已取消");
  CoupleProposal.apply(s,proposal("two").put("replaces","one"),"a");phase(s,"one","已修改");phase(s,"two","待回应");
  CoupleProposal.apply(s,proposal("two"),"a");phase(s,"two","待回应");
  if(s.getJSONArray("events").length()!=1||!"personal-record".equals(s.getJSONArray("events").getString(0)))throw new AssertionError("personal history changed");
  System.out.println("Couple proposal semantic regressions passed");
 }
}
