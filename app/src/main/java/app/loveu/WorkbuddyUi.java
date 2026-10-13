package app.loveu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;

/** The optional WorkBuddy suite. Classic tokens and layouts remain separately available. */
final class WorkbuddyUi {
 static Typeface bodyFont,numberFont;
 static Typeface font(Context c,boolean number){if(!Language.code.equals("zh-Hans"))return Typeface.create("sans-serif",Typeface.NORMAL);try{if(bodyFont==null)bodyFont=new Typeface.Builder(c.getAssets(),"workbuddy-fonts/LoveU-UI-SC.ttf").setFontVariationSettings("'wght' 400").build();if(numberFont==null)numberFont=new Typeface.Builder(c.getAssets(),"workbuddy-fonts/DMSans.ttf").setFontVariationSettings("'wght' 400, 'opsz' 14").build();return number?numberFont:bodyFont;}catch(RuntimeException e){return Typeface.create("sans-serif",Typeface.NORMAL);}}
 static int SUBTLE,DANGER=0xffbd533e,SUCCESS=0xff5b9b71;
 static void apply(Context c,String palette,String mode){
  boolean dark="dark".equals(mode)||(!"light".equals(mode)&&(c.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES);
  Ui.DARK=dark;Ui.WORKBUDDY=true;
  if("rose".equals(palette)){
   Ui.BG=dark?0xff1a1218:0xfffbf3f6;Ui.SURFACE=dark?0xff291d26:Color.WHITE;Ui.BERRY=dark?0xffd07ea6:0xffb0537a;Ui.PINK=dark?0xff482b3c:0xfff6e1ea;Ui.INK=dark?0xfff6e7ee:0xff402735;Ui.MUTED=dark?0xffbda6b3:0xff927382;Ui.BORDER=dark?0xff4a3241:0xffeddae3;Ui.HERO_START=dark?0xffba668e:0xffc9799b;Ui.HERO_END=dark?0xff8a3c60:0xff8e3e64;
  }else if("teal".equals(palette)){
   Ui.BG=dark?0xff0e1917:0xffeff6f3;Ui.SURFACE=dark?0xff172823:Color.WHITE;Ui.BERRY=dark?0xff4fbd9f:0xff2e8c79;Ui.PINK=dark?0xff234237:0xffdceee7;Ui.INK=dark?0xffe3f3eb:0xff203b32;Ui.MUTED=dark?0xff9db8aa:0xff718b7e;Ui.BORDER=dark?0xff304b40:0xffd4e4db;Ui.HERO_START=dark?0xff3a987f:0xff4eac90;Ui.HERO_END=dark?0xff216b5e:0xff1f6659;
  }else{
   Ui.BG=dark?0xff1b1410:0xfffaf5eb;Ui.SURFACE=dark?0xff2a2019:Color.WHITE;Ui.BERRY=dark?0xffe0964a:0xffc9762b;Ui.PINK=dark?0xff493220:0xfff7e4cc;Ui.INK=dark?0xfff4e8d7:0xff3c2b17;Ui.MUTED=dark?0xffbca58a:0xff9e8770;Ui.BORDER=dark?0xff4a3829:0xffeee1cc;Ui.HERO_START=dark?0xffc07931:0xffc9762b;Ui.HERO_END=dark?0xff9a5219:0xffa2541a;
  }
  Ui.ON_ACCENT=dark?0xff241b15:Color.WHITE;Ui.TEAL=SUCCESS;SUBTLE=dark?0xff9b8978:0xffb4a28d;
 }
 static TextView text(MainActivity a,String value,int size,int color){TextView t=new LanguageViews.Text(a);t.setText(value);t.setTextColor(color);t.setTextSize(size);t.setFontFeatureSettings("tnum");t.setTypeface(font(a,value.matches("[0-9a-zA-Z .:%+—/]*")));t.setPadding(0,a.dp(2),0,a.dp(2));t.setIncludeFontPadding(false);return t;}
 static void strong(TextView t){t.setTypeface(Typeface.create(t.getTypeface(),Typeface.BOLD));}
 static Button button(MainActivity a,String label,Runnable action){Button b=new LanguageViews.Action(a);b.setText(label);b.setAllCaps(false);b.setTextSize(15);b.setIncludeFontPadding(false);b.setGravity(Gravity.CENTER);b.setAutoSizeTextTypeUniformWithConfiguration(10,15,1,android.util.TypedValue.COMPLEX_UNIT_SP);b.setTypeface(Typeface.create(font(a,false),Typeface.BOLD));b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(0);b.setMinimumHeight(0);b.setPadding(a.dp(12),a.dp(8),a.dp(12),a.dp(8));b.setSingleLine(false);b.setMaxLines(2);b.setElevation(0);b.setStateListAnimator(null);b.setBackgroundTintList(null);boolean primary=label.equals("保存")||label.startsWith("保存补充")||label.equals("开始计时")||label.equals("结束并保存")||label.equals("解锁")||label.equals("发送")||label.equals("确认分享");style(b,primary);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,a.dp(52));p.setMargins(0,a.dp(6),0,a.dp(6));b.setLayoutParams(p);b.setOnClickListener(v->action.run());return b;}
 static void style(Button b,boolean primary){Context c=b.getContext();b.setTextColor(primary?Ui.ON_ACCENT:Ui.INK);b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(Ui.PINK),Ui.stroke(primary?Ui.BERRY:Ui.SURFACE,primary?Ui.BERRY:Ui.BORDER,18,c),null));}
 static void flat(Button b){b.setBackgroundColor(Color.TRANSPARENT);b.setTextColor(Ui.MUTED);}
 static LinearLayout card(Context c){LinearLayout box=new LinearLayout(c);box.setOrientation(LinearLayout.VERTICAL);int p=Ui.dp(c,16);box.setPadding(p,p,p,p);box.setBackground(Ui.stroke(Ui.SURFACE,Ui.SURFACE,24,c));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,Ui.dp(c,14));box.setLayoutParams(lp);return box;}
 static EditText field(MainActivity a,LinearLayout box,String label,String value,int type){
  boolean multi=(type&android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE)!=0;
  boolean secret=(type==18||type==129);
  LinearLayout row=a.column();row.setOrientation(multi||secret?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(a.dp(16),a.dp(10),a.dp(12),a.dp(10));row.setBackground(Ui.stroke(Ui.SURFACE,Ui.BORDER,16,a));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.setMargins(0,a.dp(5),0,a.dp(7));box.addView(row,rp);
  TextView title=a.text(label.replace("（可留空）","").replace("（YYYY-MM-DD）",""),14,Ui.MUTED);title.setMaxLines(3);
  if(multi||secret)row.addView(title);else row.addView(title,new LinearLayout.LayoutParams(a.dp(102),-2));
  EditText input=new EditText(a);input.setInputType(type);input.setText(value==null?"":value);input.setTextColor(Ui.INK);input.setHintTextColor(Ui.MUTED);input.setTextSize(16);input.setPadding(a.dp(4),a.dp(8),a.dp(4),a.dp(8));input.setBackgroundColor(Color.TRANSPARENT);input.setMinHeight(a.dp(38));input.setContentDescription(Language.ui(label));if(multi){input.setMinLines(3);input.setGravity(Gravity.TOP);}else if(!secret){input.setSingleLine(true);input.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);}else input.setSingleLine(true);
  row.addView(input,multi||secret?new LinearLayout.LayoutParams(-1,-2):new LinearLayout.LayoutParams(0,-2,1));return input;
 }
 static void decorate(AlertDialog d){if(!Ui.WORKBUDDY)return;Window w=d.getWindow();if(w!=null){w.setBackgroundDrawable(Ui.stroke(Ui.BG,Ui.BORDER,24,d.getContext()));w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);w.setLayout(Math.min(d.getContext().getResources().getDisplayMetrics().widthPixels-Ui.dp(d.getContext(),24),Ui.dp(d.getContext(),480)),-2);}for(int id:new int[]{-1,-2,-3}){Button b=d.getButton(id);if(b!=null){b.setAllCaps(false);b.setTextColor(id==-1?Ui.BERRY:Ui.MUTED);b.setMinHeight(Ui.dp(d.getContext(),48));}}}
 static View line(Context c){View v=new View(c);v.setBackgroundColor(Ui.BORDER);v.setLayoutParams(new LinearLayout.LayoutParams(-1,Ui.dp(c,1)));return v;}
 static LinearLayout row(MainActivity a,String title,String value,Runnable action){LinearLayout row=new LinearLayout(a);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(a.dp(14),a.dp(12),a.dp(12),a.dp(12));row.setMinimumHeight(a.dp(54));TextView name=a.text(title,16,Ui.INK);row.addView(name,new LinearLayout.LayoutParams(0,-2,1));if(value!=null&&!value.isEmpty()){TextView detail=a.text(value,13,Ui.MUTED);detail.setMaxWidth(a.dp(130));detail.setMaxLines(2);row.addView(detail);}TextView arrow=a.text(" ›",24,Ui.MUTED);row.addView(arrow);row.setClickable(true);row.setContentDescription(Language.ui(title));row.setOnClickListener(v->action.run());return row;}
}
