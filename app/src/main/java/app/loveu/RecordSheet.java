package app.loveu;

import android.app.AlertDialog;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import java.util.*;

/** An isolated draft. Dismissal never changes the encrypted event. */
final class RecordSheet {
    final MainActivity activity;
    final Domain.Event draft;
    final LinearLayout fields;
    AlertDialog dialog; EditText selfNumber,partnerNumber;
    interface Choice { void set(String value); }
    RecordSheet(MainActivity activity, Domain.Event event) throws Exception {
        this.activity=activity;
        draft=Vault.eventFromJson(Vault.eventToJson(event));
        fields=activity.column();
        fields.setPadding(activity.dp(16),activity.dp(12),activity.dp(16),activity.dp(4));
    }
    LinearLayout card(LinearLayout parent) {
        LinearLayout box=activity.column();box.setPadding(activity.dp(14),activity.dp(14),activity.dp(14),activity.dp(14));
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(Ui.SURFACE);bg.setCornerRadius(activity.dp(16));bg.setStroke(activity.dp(1),Ui.BORDER);box.setBackground(bg);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=activity.dp(12);parent.addView(box,p);return box;
    }
    LinearLayout.LayoutParams chipParams(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,activity.dp(44),1);p.setMargins(activity.dp(3),activity.dp(4),activity.dp(3),activity.dp(4));return p;}
    void heading(String title) { fields.addView(activity.text(title,15,activity.ink)); }
    void choice(LinearLayout box,String title,String[] options,String selected,Choice callback) {
        box.addView(activity.text(title,15,activity.ink));
        final String[] current={selected==null?"未记录":selected};
        int longest=0;for(String option:options)longest=Math.max(longest,Language.ui(option).length());int columns=longest>4?2:3;final List<Button> buttons=new ArrayList<>();LinearLayout row=null;
        for(int i=0;i<options.length;i++) {
            if(i%columns==0){row=new LinearLayout(activity);box.addView(row);}
            String option=options[i];Button button=activity.button(option,()->{});
            buttons.add(button);row.addView(button,chipParams());
            button.setTextSize(13);button.setPadding(activity.dp(4),0,activity.dp(4),0);button.setMinWidth(0);button.setMinimumWidth(0);button.setMinHeight(0);button.setMinimumHeight(0);button.setStateListAnimator(null);button.setOnClickListener(v->{current[0]=option;callback.set(option.equals("未记录")?null:option);for(Button item:buttons)styleChoice(item,Language.source(item).equals(current[0]));});
            styleChoice(button,option.equals(current[0]));
        }
    }
    void styleChoice(Button button,boolean selected){android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(selected?activity.berry:Ui.PINK);bg.setCornerRadius(activity.dp(12));bg.setStroke(activity.dp(1),selected?activity.berry:Ui.BORDER);button.setBackground(bg);button.setTextColor(selected?Ui.ON_ACCENT:activity.ink);button.setContentDescription(Language.ui(Language.source(button)+(selected?" 已选择":" 未选择")));}
    void multi(String title,String[] values,List<String> selected,boolean outcomes) {
        LinearLayout section=card(fields);section.addView(activity.text(title,15,activity.ink));
        LinearLayout row=null;
        final List<CheckBox> checks=new ArrayList<>();
        for(int i=0;i<values.length;i++) {
            if(i%2==0){row=new LinearLayout(activity);section.addView(row);}
            String value=values[i];
            CheckBox check=new LanguageViews.Check(activity); check.setText(value);check.setMinHeight(activity.dp(44));
            Ui.styleToggle(check);check.setChecked(selected.contains(value));checks.add(check);
            row.addView(check,new LinearLayout.LayoutParams(0,-2,1));
            check.setOnCheckedChangeListener((b,on)->{
                if(on&&!selected.contains(value))selected.add(value); else if(!on)selected.remove(value);
                if(outcomes&&on) for(CheckBox other:checks)if(other!=check&&other.isChecked()&&
                    (value.equals("没射精")||Language.source(other).equals("没射精")))other.setChecked(false);
            });
        }
    }
    void orgasm(String title,boolean self) {
        Boolean value=self?draft.selfOrgasm:draft.partnerOrgasm;
        Integer count=self?draft.selfOrgasmCount:draft.partnerOrgasmCount;
        LinearLayout section=card(fields),countBox=activity.column();
        EditText number=activity.field(countBox,"次数（可留空）",Boolean.TRUE.equals(value)&&count!=null?count.toString():null,2);
        if(self)selfNumber=number;else partnerNumber=number;
        LinearLayout countActions=new LinearLayout(activity);
        countActions.addView(activity.button("−",()->activity.attempt(()->{Integer n=readCount(number);number.setText(String.valueOf(Math.max(1,(n==null?1:n)-1)));})),chipParams());
        countActions.addView(activity.button("+",()->activity.attempt(()->{Integer n=readCount(number);if(n==null)n=0;if(n==Integer.MAX_VALUE)return;number.setText(String.valueOf(n+1));})),chipParams());countBox.addView(countActions);
        choice(section,title,new String[]{"是","否","未记录"},value==null?null:value?"是":"否",s->{
            Boolean selected=s==null?null:s.equals("是");
            if(self)draft.selfOrgasm=selected;else draft.partnerOrgasm=selected;
            if(!Boolean.TRUE.equals(selected))number.setText("");
            countBox.setVisibility(Boolean.TRUE.equals(selected)?android.view.View.VISIBLE:android.view.View.GONE);
        });
        section.addView(countBox);countBox.setVisibility(Boolean.TRUE.equals(value)?android.view.View.VISIBLE:android.view.View.GONE);
    }
    void condition(LinearLayout box,String title,Domain.Condition condition) {
        box.addView(activity.text(title,15,activity.ink));
        choice(box,"精力",new String[]{"1","2","3","4","5","未记录"},str(condition.energy),s->condition.energy=num(s));
        choice(box,"疲劳",new String[]{"1","2","3","4","5","未记录"},str(condition.fatigue),s->condition.fatigue=num(s));
        choice(box,"心情",new String[]{"1","2","3","4","5","未记录"},str(condition.mood),s->condition.mood=num(s));
        choice(box,"舒适度",new String[]{"1","2","3","4","5","未记录"},str(condition.comfort),s->condition.comfort=num(s));
        box.addView(activity.text("1 较低 / 5 较高；仅描述当时感受",13,activity.muted));
        for(String tag:new String[]{"疼痛/不适","干涩","出血","疲惫","放松","其他"}){
            CheckBox b=new LanguageViews.Check(activity);b.setText(tag);Ui.styleToggle(b);b.setMinHeight(activity.dp(48));b.setChecked(condition.tags.contains(tag));box.addView(b);
            b.setOnCheckedChangeListener((v,on)->{if(on&&!condition.tags.contains(tag))condition.tags.add(tag);else if(!on)condition.tags.remove(tag);});
        }
    }
    String str(Integer n){return n==null?null:n.toString();}
    Integer num(String s){return s==null?null:Integer.valueOf(s);}
    void observedMaleCondition(LinearLayout box,Domain.Condition condition,boolean partner) {
        box.addView(activity.text(partner?"对方的表现（按自己的感受选填）":"自己的表现（选填）",15,activity.ink));
        extraChoice(box,"硬度（1 较低 / 4 较高）",new String[]{"1","2","3","4","未记录"},condition,"hardness",true);
        extraChoice(box,"感觉持续时间",new String[]{"短","适中","长","未记录"},condition,"perceivedDuration",false);
        extraChoice(box,"节奏",new String[]{"慢","适中","快","变化","未记录"},condition,"speed",false);
        box.addView(activity.text("只描述这一次的主观感受，无需测量。",13,activity.muted));
    }
    void extraChoice(LinearLayout box,String title,String[] options,Domain.Condition condition,String key,boolean number) {
        Object old=condition.extra.get(key);String selected=old==null||old==org.json.JSONObject.NULL?null:String.valueOf(old);
        if(selected!=null&&!Arrays.asList(options).contains(selected))box.addView(activity.text("原记录："+selected+"（选择后更新）",13,activity.muted));
        choice(box,title,options,selected,value->{if(value==null)condition.extra.remove(key);else condition.extra.put(key,number?Integer.valueOf(value):value);});
    }
    boolean showMalePartnerCondition() {
        String partner=activity.settings().optString("partnerGender","不设置");
        return partner.equals("男")||(!partner.equals("女")&&activity.settings().optString("profileGender","不设置").equals("女"));
    }
    void show() {
        LinearLayout protection=card(fields);
        String[] protectionOptions={"安全套","长效避孕药","紧急避孕药","体外射精","安全期","节育环","结扎","无措施","未记录"};
        if(draft.protection!=null&&!Arrays.asList(protectionOptions).contains(draft.protection))protection.addView(activity.text("原记录："+draft.protection+"（选择后更新）",13,activity.muted));
        choice(protection,"保护措施",protectionOptions,draft.protection,s->draft.protection=s);
        multi("射精情况（可多选）",new String[]{"内射","体外","口爆","颜射","乳射","套内","没射精","其他"},draft.outcomes,true);
        orgasm("对方高潮",false);orgasm("自己高潮",true);
        LinearLayout advanced=card(fields);advanced.setVisibility(android.view.View.GONE);
        Button expand=activity.button("更多细节 · 展开",()->{});
        fields.addView(expand,fields.indexOfChild(advanced));
        expand.setOnClickListener(v->{boolean open=advanced.getVisibility()==android.view.View.GONE;advanced.setVisibility(open?android.view.View.VISIBLE:android.view.View.GONE);expand.setText(open?"更多细节 · 收起":"更多细节 · 展开");});
        choice(advanced,"爱浪声",new String[]{"安静","轻声","一般","较大","未记录"},draft.vocalLevel,s->draft.vocalLevel=s);
        advanced.addView(activity.text("姿势（可多选）",15,activity.ink));
        LinearLayout row=null;
        for(int i=0;i<PoseIconView.CATEGORIES.length;i++){
            if(i%2==0){row=new LinearLayout(activity);advanced.addView(row);}
            String category=PoseIconView.CATEGORIES[i];LinearLayout card=activity.column();
            PoseIconView icon=new PoseIconView(activity,category);card.addView(icon,new LinearLayout.LayoutParams(-1,activity.dp(64)));
            CheckBox selected=new LanguageViews.Check(activity);selected.setText(category);Ui.styleToggle(selected);selected.setChecked(draft.positions.contains(category));selected.setMinHeight(activity.dp(44));card.addView(selected);
            selected.setOnCheckedChangeListener((b,on)->{if(on&&!draft.positions.contains(category))draft.positions.add(category);else if(!on)draft.positions.remove(category);});
            row.addView(card,new LinearLayout.LayoutParams(0,-2,1));
        }
        EditText customPose=activity.field(advanced,"自定义姿势（可留空）",customPositions(),1);
        LinearLayout wellbeing=activity.column();wellbeing.setVisibility(android.view.View.GONE);
        advanced.addView(activity.button("身体状况 · 展开 / 收起",()->wellbeing.setVisibility(wellbeing.getVisibility()==android.view.View.GONE?android.view.View.VISIBLE:android.view.View.GONE)));
        advanced.addView(wellbeing);LinearLayout selfBox=card(wellbeing),partnerBox=card(wellbeing);
        condition(selfBox,"本人",draft.selfCondition);condition(partnerBox,"对方",draft.partnerCondition);
        if(activity.settings().optString("profileGender","不设置").equals("男"))observedMaleCondition(selfBox,draft.selfCondition,false);
        if(showMalePartnerCondition())observedMaleCondition(partnerBox,draft.partnerCondition,true);
        EditText note=activity.field(card(fields),"备注（可留空）",draft.note,131073);
        LinearLayout shell=activity.column();shell.setBackgroundColor(activity.bg);
        shell.setPadding(activity.dp(12),activity.dp(16),activity.dp(12),activity.dp(12));shell.addView(activity.text("补充这一次",20,activity.ink));
        shell.addView(activity.text(draft.date+"  "+activity.val(draft.time)+" · 已保存",13,activity.muted));
        ScrollView scroll=new ScrollView(activity);scroll.setVerticalScrollBarEnabled(false);scroll.setHorizontalScrollBarEnabled(false);scroll.addView(fields);shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout actions=new LinearLayout(activity);actions.setPadding(activity.dp(1),activity.dp(8),activity.dp(1),0);
        actions.addView(activity.button("稍后填写",()->dialog.dismiss()),chipParams());
        actions.addView(activity.button("保存补充",()->activity.attempt(()->{
            if(activity.locked)return;
            normalizeOrgasm();String custom=activity.empty(customPose.getText().toString());if(!Objects.equals(custom,activity.empty(customPositions()))){draft.positions.removeIf(value->!Arrays.asList(PoseIconView.CATEGORIES).contains(value));if(custom!=null)draft.positions.add(custom);}draft.note=activity.empty(note.getText().toString());
            java.util.List<Domain.Event> events=activity.events();boolean found=false;
            for(Domain.Event current:events)if(current.id.equals(draft.id)){
                current.protection=draft.protection;current.outcomes=new ArrayList<>(draft.outcomes);
                current.positions=new ArrayList<>(draft.positions);current.vocalLevel=draft.vocalLevel;
                current.selfOrgasm=draft.selfOrgasm;current.partnerOrgasm=draft.partnerOrgasm;
                current.selfOrgasmCount=draft.selfOrgasmCount;current.partnerOrgasmCount=draft.partnerOrgasmCount;
                current.selfCondition=draft.selfCondition;current.partnerCondition=draft.partnerCondition;
                current.note=draft.note;current.detailsPending=false;found=true;break;
            }
            if(!found)throw new IllegalStateException("这次记录已删除");
            activity.saveEvents(events);dialog.dismiss();activity.show();
        })),chipParams());shell.addView(actions);
        dialog=new LanguageDialog(activity).setView(shell).create();
        activity.dialogs.add(dialog);activity.activeSheet=dialog;
        dialog.setOnDismissListener(d->{if(activity.activeSheet==dialog)activity.activeSheet=null;activity.show();});
        dialog.show();Window w=dialog.getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setBackgroundDrawableResource(android.R.color.transparent);w.setLayout(-1,(int)(activity.getResources().getDisplayMetrics().heightPixels*.88));}
    }
    String customPositions(){List<String> custom=new ArrayList<>();for(String value:draft.positions)if(!Arrays.asList(PoseIconView.CATEGORIES).contains(value))custom.add(value);return String.join(" / ",custom);}
    Integer readCount(EditText field){String s=field.getText().toString().trim();if(s.isEmpty())return null;try{return Integer.valueOf(s);}catch(NumberFormatException error){throw new IllegalArgumentException("高潮次数需为有效非负整数");}}
    void normalizeOrgasm(){
        draft.selfOrgasmCount=normalizedCount(draft.selfOrgasm,selfNumber);
        draft.partnerOrgasmCount=normalizedCount(draft.partnerOrgasm,partnerNumber);
    }
    Integer normalizedCount(Boolean selected,EditText field){
        if(selected==null)return null;
        if(!selected)return 0;
        Integer count=readCount(field);
        if(count!=null&&count<1)throw new IllegalArgumentException("选择高潮后次数至少为 1，可留空保持未知");
        return count;
    }
}
