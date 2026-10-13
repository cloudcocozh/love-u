package app.loveu;
import android.content.Context;import android.graphics.*;import android.view.View;
final class WorkbuddyDrop extends View {
 final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);WorkbuddyDrop(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
 protected void onDraw(Canvas c){float r=Math.min(getWidth(),getHeight())/2f;c.save();c.translate(getWidth()/2f,getHeight()/2f);p.setColor(Ui.BERRY);c.drawCircle(0,0,r,p);p.setColor(Ui.ON_ACCENT);Path path=new Path();path.moveTo(0,-r*.48f);path.cubicTo(-r*.11f,-r*.22f,-r*.34f,-r*.03f,-r*.27f,r*.19f);path.cubicTo(-r*.17f,r*.48f,r*.2f,r*.48f,r*.28f,r*.18f);path.cubicTo(r*.34f,-r*.04f,r*.1f,-r*.25f,0,-r*.48f);path.close();c.drawPath(path,p);c.restore();}
}
