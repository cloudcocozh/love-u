package app.loveu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import java.util.*;

/** Actual completed-period durations, with date and value on every bar. */
final class WorkbuddyPeriodBars extends View {
    final TreeMap<String,Integer> values;
    final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    WorkbuddyPeriodBars(Context context,TreeMap<String,Integer> source){super(context);values=new TreeMap<>(source);StringBuilder description=new StringBuilder("经期天数趋势");for(Map.Entry<String,Integer> value:values.entrySet())description.append("，").append(value.getKey()).append("，").append(value.getValue()).append("天");setContentDescription(description.toString());}
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        android.view.ViewGroup.LayoutParams params=getLayoutParams();
        float density=getResources().getDisplayMetrics().density;
        int width=params!=null&&params.width>0?params.width:MeasureSpec.getSize(widthSpec);
        if(width<=0)width=Math.round(Math.max(280,values.size()*64)*density);
        int height=params!=null&&params.height>0?params.height:Math.round(190*density);
        setMeasuredDimension(resolveSize(width,widthSpec),resolveSize(height,heightSpec));
    }
    protected void onDraw(Canvas canvas){super.onDraw(canvas);if(values.isEmpty())return;float density=getResources().getDisplayMetrics().density;int max=Collections.max(values.values());float width=(float)getWidth()/values.size(),bottom=getHeight()-32*density,top=30*density;paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(11*density);int index=0;for(Map.Entry<String,Integer> entry:values.entrySet()){float center=width*(index+.5f),barWidth=Math.min(26*density,width*.56f),barTop=bottom-(bottom-top)*entry.getValue()/Math.max(1,max);paint.setColor(Ui.BERRY);canvas.drawRoundRect(center-barWidth/2,barTop,center+barWidth/2,bottom,7*density,7*density,paint);paint.setColor(Ui.INK);canvas.drawText(Language.ui(entry.getValue()+" 天"),center,barTop-8*density,paint);paint.setColor(Ui.MUTED);String date=entry.getKey().length()>=10?entry.getKey().substring(5):entry.getKey();canvas.drawText(Language.ui(date),center,bottom+20*density,paint);index++;}}
}
