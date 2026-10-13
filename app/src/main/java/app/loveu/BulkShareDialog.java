package app.loveu;

import android.app.*;
import android.widget.*;
import org.json.*;
import java.util.concurrent.atomic.AtomicBoolean;

final class BulkShareDialog {
 static final AtomicBoolean preparing=new AtomicBoolean();
 static void buttons(MainActivity a,LinearLayout parent){
  parent.addView(a.button("分享全部亲密记录给爱人",()->show(a,"sex")));
  parent.addView(a.button("分享所有经期记录给爱人",()->show(a,"period")));
  JSONObject c=a.root.optJSONObject("couple");if(c!=null)try{String status=BulkShare.progress(c);if(!status.isEmpty())parent.addView(a.text(status,12,Ui.MUTED));}catch(Exception ignored){}
 }
 static void show(MainActivity a,String type){a.attempt(()->{
  BulkShare.Plan initial=BulkShare.plan(a.root,type,false);final BulkShare.Plan[] plan={initial};LinearLayout content=a.column();content.setPadding(a.dp(18),a.dp(10),a.dp(18),a.dp(10));
  TextView summary=a.text("",16,Ui.INK);summary.setTag("bulk-share-summary");content.addView(summary);CheckBox location=new LanguageViews.Check(a);location.setText("同时分享地点与坐标");Ui.styleToggle(location);if(type.equals("sex"))content.addView(location);
  content.addView(a.text(type.equals("sex")?"日期、具体时间、时长、保护措施、标签、评分及高潮记录。私人备注和身体备注不分享。":"开始、结束、经量及疼痛。包含本人和代伴侣记录，分别标记归属；私人症状备注不分享。",13,Ui.MUTED));
  content.addView(a.text("已分享及已撤回的副本会跳过。本机原记录保留；这次确认不会开启自动分享。分批传送，对方确认接收后更新进度。",12,Ui.MUTED));
  ScrollView scroll=new ScrollView(a);scroll.addView(content);AlertDialog d=new LanguageDialog(a).setTitle(type.equals("sex")?"分享全部亲密记录":"分享所有经期记录").setView(scroll).setNegativeButton("取消",null).setPositiveButton("确认分享",null).create();a.dialogs.add(d);d.show();if(a.workbuddy())WorkbuddyUi.decorate(d);
  Runnable update=()->a.attempt(()->{plan[0]=BulkShare.plan(a.root,type,location.isChecked());BulkShare.Plan p=plan[0];summary.setText("共 "+p.total+" 条 · 本次分享 "+p.items.size()+" 条"+(p.items.isEmpty()?"":"\n"+p.first+" 至 "+p.last)+"\n已分享 "+p.existing+" · 已撤回 "+p.withdrawn+(p.invalid>0?" · 需核对 "+p.invalid:""));d.getButton(-1).setEnabled(!p.items.isEmpty()&&!preparing.get());});
  location.setOnCheckedChangeListener((button,on)->update.run());update.run();d.getButton(-1).setOnClickListener(v->{if(a.locked)return;BulkShare.Plan accepted=plan[0];if(accepted.items.isEmpty())return;d.dismiss();queue(a,accepted);});
 });}
 static void queue(MainActivity a,BulkShare.Plan plan){
  if(!preparing.compareAndSet(false,true)){Toast.makeText(a,Language.ui("上一批记录正在准备中"),Toast.LENGTH_SHORT).show();return;}
  String tab=a.tab,page=a.workbuddyPage;Toast.makeText(a,Language.ui("正在准备 "+plan.items.size()+" 条加密共享副本"),Toast.LENGTH_LONG).show();
  new Thread(()->{String error=null;try{synchronized(CoupleBackgroundSync.LOCK){Vault vault=new Vault(a.getApplicationContext());JSONObject before=vault.read();JSONObject queued=BulkShare.queuedCouple(before,plan);
    synchronized(Vault.class){JSONObject latest=vault.read();BulkShare.validateCurrent(latest,plan);if(!BulkShare.canonical(BulkShare.pair(before)).equals(BulkShare.canonical(BulkShare.pair(latest))))throw new IllegalStateException("连接状态已更新，请重新确认分享");queued.put("revision",queued.optLong("revision",0)+1);latest.put("couple",queued);vault.write(latest);}
   }}catch(Exception failure){error=failure instanceof SecurityException||failure instanceof IllegalStateException?failure.getMessage():"未能保存这批共享记录，原记录保留，请重试";}finally{preparing.set(false);}final String message=error;
   a.handler.post(()->{if(a.isFinishing()||a.isDestroyed()||a.locked||!a.resumed)return;Toast.makeText(a,Language.ui(message==null?"已加入加密待发箱 "+plan.items.size()+" 条":message),Toast.LENGTH_LONG).show();if(message==null&&a.tab.equals(tab)&&a.workbuddyPage.equals(page)){a.attempt(()->{a.root=a.vault.read();if(!a.tab.equals("爱人")||(a.workbuddy()?a.workbuddyCoupleView==null:a.coupleView==null)){a.workbuddyPage="";a.workbuddyBack.clear();a.tab="爱人";a.show();}if(a.workbuddyCoupleView!=null)a.workbuddyCoupleView.flushPending();else if(a.coupleView!=null)a.coupleView.flushPending();});}});
  },"loveu-bulk-share").start();
 }
}
