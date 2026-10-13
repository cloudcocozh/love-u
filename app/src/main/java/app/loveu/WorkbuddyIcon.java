package app.loveu;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Consistent vector icons for the optional suite, independent of classic navigation. */
final class WorkbuddyIcon extends Drawable {
    final String kind;
    final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    WorkbuddyIcon(String kind,int color){this.kind=kind;paint.setColor(color);}
    @Override public void draw(Canvas canvas){
        int save=canvas.save();Rect b=getBounds();canvas.translate(b.left,b.top);canvas.scale(b.width()/24f,b.height()/24f);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.8f);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);
        if(kind.equals("今天")||kind.equals("爱人")){
            Path p=new Path();p.moveTo(12,21);p.cubicTo(9,18,2,13,2,7.8f);p.cubicTo(2,2.2f,8,1.3f,12,6);p.cubicTo(16,1.3f,22,2.2f,22,7.8f);p.cubicTo(22,13,15,18,12,21);canvas.drawPath(p,paint);
        }else if(kind.equals("日历")||kind.equals("回到今天")){
            canvas.drawRoundRect(3,5,21,21,4,4,paint);canvas.drawLine(3,10,21,10,paint);canvas.drawLine(8,2.5f,8,7,paint);canvas.drawLine(16,2.5f,16,7,paint);
            if(kind.equals("回到今天")){canvas.drawCircle(12,15.5f,2.3f,paint);}else{paint.setStyle(Paint.Style.FILL);for(int x=8;x<=16;x+=4)for(int y=14;y<=18;y+=4)canvas.drawCircle(x,y,.8f,paint);}
        }else if(kind.equals("洞察")){
            canvas.drawRoundRect(3,13,6,21,1.5f,1.5f,paint);canvas.drawRoundRect(10.5f,8,13.5f,21,1.5f,1.5f,paint);canvas.drawRoundRect(18,3,21,21,1.5f,1.5f,paint);
        }else if(kind.equals("我的")){
            canvas.drawCircle(12,7,4,paint);Path p=new Path();p.moveTo(3,21);p.cubicTo(3,11,21,11,21,21);canvas.drawPath(p,paint);
        }else if(kind.equals("搜索")){
            canvas.drawCircle(10,10,6.5f,paint);canvas.drawLine(15,15,21,21,paint);
        }else if(kind.equals("补记")){
            canvas.drawRoundRect(3,3,21,21,5,5,paint);canvas.drawLine(7.5f,12,16.5f,12,paint);canvas.drawLine(12,7.5f,12,16.5f,paint);
        }
        canvas.restoreToCount(save);
    }
    @Override public void setAlpha(int alpha){paint.setAlpha(alpha);invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
