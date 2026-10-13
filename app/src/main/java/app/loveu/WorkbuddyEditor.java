package app.loveu;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/** Additional-suite editor. It patches the latest event, never a stale full event list. */
final class WorkbuddyEditor {
    final MainActivity activity;
    final Domain.Event baseline,draft;
    final boolean add,resolve;
    final JSONObject initialTimer;
    AlertDialog dialog;
    EditText date,time,duration,place,method,partner,note;
    Integer rating;
    LinearLayout ratingButtons;
    String initialDuration;
    boolean capturedLocation;

    static void show(MainActivity activity,Domain.Event original,boolean add,boolean resolve){
        activity.attempt(()->new WorkbuddyEditor(activity,original,add,resolve).show());
    }
    WorkbuddyEditor(MainActivity activity,Domain.Event original,boolean add,boolean resolve)throws Exception{
        this.activity=activity;this.add=add;this.resolve=resolve;
        baseline=Vault.eventFromJson(Vault.eventToJson(original));draft=Vault.eventFromJson(Vault.eventToJson(original));rating=baseline.rating;
        initialTimer=resolve&&!activity.root.isNull("timer")?new JSONObject(activity.root.getJSONObject("timer").toString()):null;
    }
    void show(){
        LinearLayout shell=activity.column();shell.setPadding(activity.dp(20),activity.dp(20),activity.dp(20),activity.dp(12));shell.setBackgroundColor(Ui.BG);
        TextView title=WorkbuddyUi.text(activity,resolve?"确认计时记录":add?"添加记录":"编辑记录",25,Ui.INK);WorkbuddyUi.strong(title);title.setPadding(0,activity.dp(8),0,activity.dp(16));shell.addView(title);
        ScrollView scroll=new ScrollView(activity);scroll.setFillViewport(false);scroll.setVerticalScrollBarEnabled(false);LinearLayout fields=activity.column();scroll.addView(fields);shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        date=WorkbuddyUi.field(activity,fields,"日期",baseline.date,InputType.TYPE_CLASS_TEXT);date.setFocusable(false);date.setClickable(true);date.setContentDescription(Language.ui("记录日期，点击选择完整年月日"));date.setOnClickListener(v->chooseDate());
        time=WorkbuddyUi.field(activity,fields,"时间",baseline.time,InputType.TYPE_CLASS_TEXT);time.setHint(Language.ui("未知"));time.setContentDescription(Language.ui("记录时间 HH:mm:ss，保留秒；留空为未知"));
        initialDuration=baseline.durationMillis==null?"":BigDecimal.valueOf(baseline.durationMillis).divide(BigDecimal.valueOf(60000),2,RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        duration=WorkbuddyUi.field(activity,fields,"时长（分钟）",initialDuration,InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);duration.setHint(Language.ui("未知"));duration.setContentDescription(Language.ui("时长，分钟，可小数；留空为未知"));
        place=WorkbuddyUi.field(activity,fields,"地点",baseline.place,InputType.TYPE_CLASS_TEXT);place.setHint(Language.ui("未记录"));
        Button location=WorkbuddyUi.button(activity,"定位",()->{activity.locationTarget=draft;capturedLocation=true;activity.captureLocation(place);});location.setContentDescription(Language.ui("获取一次当前位置，需要系统定位授权"));location.setBackground(Ui.stroke(Ui.PINK,Ui.PINK,16,activity));location.setTextColor(Ui.BERRY);location.setTextSize(12);LinearLayout placeRow=(LinearLayout)place.getParent();placeRow.getChildAt(0).setLayoutParams(new LinearLayout.LayoutParams(activity.dp(50),-2));LinearLayout.LayoutParams locationParams=new LinearLayout.LayoutParams(activity.dp(112),activity.dp(44));locationParams.setMargins(activity.dp(6),0,0,0);placeRow.addView(location,locationParams);
        method=WorkbuddyUi.field(activity,fields,"方式",baseline.method,InputType.TYPE_CLASS_TEXT);method.setHint(Language.ui("未记录"));
        partner=WorkbuddyUi.field(activity,fields,"伴侣",baseline.partner,InputType.TYPE_CLASS_TEXT);partner.setHint(Language.ui("未记录"));
        note=WorkbuddyUi.field(activity,fields,"备注",baseline.note,InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);note.setHint(Language.ui("添加备注…（可选）"));
        LinearLayout ratings=WorkbuddyUi.card(activity);ratings.addView(WorkbuddyUi.text(activity,"满意度",14,Ui.MUTED));ratingButtons=new LinearLayout(activity);ratings.addView(ratingButtons);fields.addView(ratings);renderRating();
        LinearLayout footer=activity.column();footer.setPadding(0,activity.dp(8),0,0);Button cancel=WorkbuddyUi.button(activity,"取消",()->dialog.dismiss());Button save=WorkbuddyUi.button(activity,"保存",()->activity.attempt(()->save()));footer.addView(cancel);footer.addView(save);shell.addView(footer);
        dialog=new LanguageDialog(activity).create();dialog.setView(shell,0,0,0,0);dialog.setOnDismissListener(v->{if(activity.locationTarget==draft)activity.locationTarget=null;});activity.dialogs.add(dialog);dialog.show();Window window=dialog.getWindow();if(window!=null){window.setBackgroundDrawableResource(android.R.color.transparent);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);window.setStatusBarColor(Ui.BG);window.setNavigationBarColor(Ui.BG);}
    }
    void chooseDate(){
        LocalDate current=LocalDate.parse(date.getText().toString());DatePickerDialog picker=new DatePickerDialog(activity,(v,y,m,d)->date.setText(LocalDate.of(y,m+1,d).toString()),current.getYear(),current.getMonthValue()-1,current.getDayOfMonth());activity.dialogs.add(picker);picker.show();WorkbuddyUi.decorate(picker);
    }
    void renderRating(){
        ratingButtons.removeAllViews();for(int i=1;i<=5;i++){final int value=i;Button dot=WorkbuddyUi.button(activity,"●",()->{rating=value;renderRating();});boolean selected=rating!=null&&rating>=i;dot.setTextColor(selected?Ui.BERRY:Ui.BORDER);dot.setBackgroundColor(android.graphics.Color.TRANSPARENT);dot.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);dot.setTextSize(25);dot.setContentDescription(Language.ui("满意度 "+i+" 分"+(Objects.equals(rating,i)?"，已选择":"")));dot.setSelected(Objects.equals(rating,i));ratingButtons.addView(dot,new LinearLayout.LayoutParams(0,activity.dp(48),1));}
        LinearLayout card=(LinearLayout)ratingButtons.getParent();while(card.getChildCount()>2)card.removeViewAt(2);
        LinearLayout options=new LinearLayout(activity);for(Integer value:new Integer[]{null,0}){Button button=WorkbuddyUi.button(activity,value==null?"未记录":"0 分",()->{rating=value;renderRating();});button.setSelected(Objects.equals(rating,value));button.setTextColor(Objects.equals(rating,value)?Ui.BERRY:Ui.MUTED);button.setContentDescription(Language.ui("满意度 "+(value==null?"未知":value+"分")+(Objects.equals(rating,value)?"，已选择":"")));options.addView(button,new LinearLayout.LayoutParams(0,activity.dp(44),1));}card.addView(options);
    }
    void save()throws Exception{
        if(activity.locked)throw new IllegalStateException("请先解锁");
        String pickedDate=Domain.validateDate(date.getText().toString().trim());String pickedTime=activity.empty(time.getText().toString());if(pickedTime!=null)LocalTime.parse(pickedTime);
        String durationText=duration.getText().toString().trim();Long pickedDuration=baseline.durationMillis;
        if(!durationText.equals(initialDuration)){pickedDuration=parseDuration(durationText);}
        String pickedPlace=activity.empty(place.getText().toString()),pickedMethod=activity.empty(method.getText().toString()),pickedPartner=activity.empty(partner.getText().toString()),pickedNote=activity.empty(note.getText().toString());
        JSONObject latest=activity.vault.read();JSONArray previous=latest.getJSONArray("events");Domain.Event current=null;for(int i=0;i<previous.length();i++){Domain.Event value=Vault.eventFromJson(previous.getJSONObject(i));if(value.id.equals(baseline.id))current=value;}
        if(!add&&current==null)throw new IllegalStateException("这次记录已删除");
        // Start with the latest full event, including unknown fields and concurrently added detail.
        Domain.Event saved=current==null?Vault.eventFromJson(Vault.eventToJson(draft)):current;
        if(current==null||!Objects.equals(pickedDate,baseline.date))saved.date=pickedDate;
        if(current==null||!Objects.equals(pickedTime,baseline.time))saved.time=pickedTime;
        if(current==null||!durationText.equals(initialDuration))saved.durationMillis=pickedDuration;
        if(current==null||changed(method,baseline.method))saved.method=pickedMethod;
        if(current==null||changed(partner,baseline.partner))saved.partner=pickedPartner;
        if(current==null||changed(note,baseline.note))saved.note=pickedNote;
        if(current==null||!Objects.equals(rating,baseline.rating))saved.rating=rating;
        boolean newFix=capturedLocation&&(!Objects.equals(draft.locationCapturedAt,baseline.locationCapturedAt)||!Objects.equals(draft.latitude,baseline.latitude)||!Objects.equals(draft.longitude,baseline.longitude));
        if(current==null||changed(place,baseline.place)||newFix){saved.place=pickedPlace;saved.latitude=draft.latitude;saved.longitude=draft.longitude;saved.locationAccuracy=draft.locationAccuracy;saved.locationCapturedAt=draft.locationCapturedAt;}
        if(resolve)verifyTimer(latest,initialTimer,baseline.id);
        JSONArray next=new JSONArray();for(int i=0;i<previous.length();i++){JSONObject value=previous.getJSONObject(i);if(!baseline.id.equals(value.getString("id")))next.put(value);}next.put(Vault.eventToJson(saved));latest.put("events",next);
        if(resolve)latest.put("timer",JSONObject.NULL);activity.vault.write(latest);activity.root=latest;
        if(resolve)TimerService.send(activity,TimerService.ACTION_CANCEL);dialog.dismiss();activity.show();if(add){activity.autoEventLocation(saved.id);activity.followup(saved.id);}
    }
    static boolean changed(EditText field,String initial){return !field.getText().toString().equals(initial==null?"":initial);}
    static Long parseDuration(String text){
        if(text.isEmpty())return null;try{BigDecimal minutes=new BigDecimal(text);if(minutes.signum()<0||minutes.compareTo(BigDecimal.valueOf(525600))>0)throw new IllegalArgumentException();return minutes.multiply(BigDecimal.valueOf(60000)).setScale(0,RoundingMode.HALF_UP).longValueExact();}catch(Exception invalid){throw new IllegalArgumentException("时长需为有效非负分钟数");}
    }
    static void verifyTimer(JSONObject latest,JSONObject expected,String eventId)throws Exception{
        if(expected==null||latest.isNull("timer"))throw new IllegalStateException("这次计时已结束，请重新查看记录");JSONObject now=latest.getJSONObject("timer");Domain.Timer before=Vault.timerFromJson(expected.getJSONObject("state")),current=Vault.timerFromJson(now.getJSONObject("state"));
        String expectedId="timer:"+before.startWall+":"+before.startElapsed;if(!expectedId.equals(eventId)||before.startWall!=current.startWall||before.startElapsed!=current.startElapsed)throw new IllegalStateException("当前计时已改变，请重新确认");
    }
}


