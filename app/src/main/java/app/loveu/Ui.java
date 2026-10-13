package app.loveu;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;

final class Ui {
 static int BERRY,INK,MUTED,BG,PINK,TEAL,BORDER,SURFACE,ON_ACCENT,HERO_START,HERO_END;
 static boolean DARK,WORKBUDDY;
 static { install(ThemePalette.resolve("rose",false)); }
 static void applyTheme(Context context,String palette,String mode){
  WORKBUDDY=false;
  boolean night="dark".equals(mode)||(!"light".equals(mode)&&(context.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES);
  install(ThemePalette.resolve(palette,night));
 }
 private static void install(ThemePalette theme){
  BERRY=theme.accent;INK=theme.ink;MUTED=theme.muted;BG=theme.background;PINK=theme.soft;TEAL=theme.secondary;BORDER=theme.border;SURFACE=theme.surface;ON_ACCENT=theme.onAccent;HERO_START=theme.heroStart;HERO_END=theme.heroEnd;DARK=theme.dark;
 }
 static void styleToggle(android.widget.CompoundButton b){b.setTextColor(INK);android.content.res.ColorStateList tint=new android.content.res.ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{BERRY,MUTED});if(b instanceof android.widget.Switch){((android.widget.Switch)b).setThumbTintList(tint);((android.widget.Switch)b).setTrackTintList(android.content.res.ColorStateList.valueOf(BORDER));}else b.setButtonTintList(tint);}
 static int dp(Context c,int n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
 static GradientDrawable stroke(int fill,int border,int radius,Context c){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(c,radius));if(fill!=border)d.setStroke(dp(c,1),border);return d;}
 static GradientDrawable gradient(int start,int end,int radius,Context c){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{start,blend(start,end),end});d.setCornerRadius(dp(c,radius));return d;}
 static int blend(int a,int b){return android.graphics.Color.rgb((android.graphics.Color.red(a)+android.graphics.Color.red(b))/2,(android.graphics.Color.green(a)+android.graphics.Color.green(b))/2,(android.graphics.Color.blue(a)+android.graphics.Color.blue(b))/2);}
 static android.graphics.drawable.Drawable hero(Context c){return WORKBUDDY?gradient(HERO_START,HERO_END,24,c):new HeroSurface(c);}
 static final class HeroSurface extends android.graphics.drawable.Drawable {
  final Paint paint=new Paint(3);final float radius;final int first=HERO_START,last=HERO_END;final boolean night=DARK;
  HeroSurface(Context c){radius=dp(c,18);}
  public void draw(Canvas canvas){android.graphics.Rect b=getBounds();android.graphics.RectF r=new android.graphics.RectF(b);canvas.save();Path clip=new Path();clip.addRoundRect(r,radius,radius,Path.Direction.CW);canvas.clipPath(clip);paint.setShader(new android.graphics.LinearGradient(b.left,b.top,b.right,b.bottom,new int[]{first,last},null,android.graphics.Shader.TileMode.CLAMP));canvas.drawRect(r,paint);paint.setShader(null);paint.setColor(night?0x226B537D:0x44FFFFFF);canvas.drawCircle(b.right-b.width()*.1f,b.top+b.height()*.2f,b.width()*.32f,paint);paint.setColor(night?0x225F376C:0x35DDA0C9);canvas.drawCircle(b.right-b.width()*.08f,b.bottom,b.width()*.46f,paint);paint.setColor(night?0x128CCFD0:0x32FFE9E3);canvas.drawCircle(b.left,b.bottom,b.width()*.38f,paint);canvas.restore();}
  public void setAlpha(int alpha){} public void setColorFilter(android.graphics.ColorFilter filter){} public int getOpacity(){return android.graphics.PixelFormat.OPAQUE;}
 }

 static LinearLayout card(Context c){if(WORKBUDDY)return WorkbuddyUi.card(c);LinearLayout box=new LinearLayout(c);box.setOrientation(LinearLayout.VERTICAL);int p=dp(c,16);box.setPadding(p,p,p,p);box.setBackground(stroke(SURFACE,SURFACE,18,c));box.setElevation(dp(c,1));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(c,12));box.setLayoutParams(lp);return box;}
 static final class NavIcon extends View {
  final String kind;final boolean selected;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  NavIcon(Context c,String kind,boolean selected){super(c);this.kind=kind;this.selected=selected;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
  protected void onDraw(Canvas c){super.onDraw(c);float s=Math.min(getWidth(),getHeight())/28f;c.save();c.translate((getWidth()-28*s)/2,(getHeight()-28*s)/2);c.scale(s,s);p.setColor(selected?(WORKBUDDY&&kind.equals("今天")?ON_ACCENT:BERRY):MUTED);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
   if(kind.equals("今天")||kind.equals("双人")||kind.equals("爱人")){Path h=new Path();h.moveTo(14,23);h.cubicTo(10,20,4,15.4f,4,9.2f);h.cubicTo(4,3.7f,10.7f,2.7f,14,7.7f);h.cubicTo(17.3f,2.7f,24,3.7f,24,9.2f);h.cubicTo(24,15.4f,18,20,14,23);h.close();p.setStyle(selected?Paint.Style.FILL:Paint.Style.STROKE);c.drawPath(h,p);}
   else if(kind.equals("日历")){if(selected){p.setStyle(Paint.Style.FILL);c.drawRoundRect(4,6,24,24,3,3,p);p.setColor(ON_ACCENT);}else c.drawRoundRect(4,6,24,24,3,3,p);p.setStyle(Paint.Style.STROKE);c.drawLine(4,11,24,11,p);if(selected)p.setColor(BERRY);c.drawLine(9,3,9,8,p);c.drawLine(19,3,19,8,p);if(selected)p.setColor(ON_ACCENT);p.setStyle(Paint.Style.FILL);for(int row=0;row<2;row++)for(int col=0;col<3;col++)c.drawCircle(9+col*5,16+row*4,0.8f,p);}
   else if(kind.equals("洞察")){p.setStyle(selected?Paint.Style.FILL:Paint.Style.STROKE);c.drawRoundRect(5,13,8,24,1,1,p);c.drawRoundRect(12.5f,5,15.5f,24,1,1,p);c.drawRoundRect(20,9,23,24,1,1,p);}
   else {p.setStyle(selected?Paint.Style.FILL:Paint.Style.STROKE);c.drawCircle(14,8,4.4f,p);Path body=new Path();body.moveTo(5.5f,24);body.cubicTo(5.5f,13,22.5f,13,22.5f,24);body.close();c.drawPath(body,p);}
   c.restore();
  }
 }
}
