package app.loveu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/** Neutral paired clothed posture symbols; no anatomy or sexual action is depicted. */
public final class PoseIconView extends View {
    public static final String[] CATEGORIES = {"面对面", "后侧", "侧卧", "坐姿", "站姿", "其他"};
    private final String category;
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shirt = new Paint(Paint.ANTI_ALIAS_FLAG);
    public PoseIconView(Context context, String category) {
        super(context);
        this.category = category;
        setContentDescription(category + "，双人姿态示意");
        setMinimumWidth(dp(82));
        setMinimumHeight(dp(84));
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(2.3f);
        line.setStrokeCap(Paint.Cap.ROUND);
        line.setStrokeJoin(Paint.Join.ROUND);
        shirt.setStyle(Paint.Style.FILL);
        shirt.setColor(0xFFF6E6EC);
    }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    @Override protected void onMeasure(int w, int h) {
        setMeasuredDimension(resolveSize(dp(100), w), resolveSize(dp(88), h));
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float scale = Math.min(getWidth() / 160f, getHeight() / 120f);
        canvas.save();
        canvas.translate((getWidth() - 160f * scale) / 2, (getHeight() - 120f * scale) / 2);
        canvas.scale(scale, scale);
        // A quiet baseline establishes orientation; figures remain fully clothed.
        line.setColor(0xFFE9DCE2);
        canvas.drawLine(18, 105, 142, 105, line);
        if ("侧卧".equals(category)) {
            lying(canvas, 30, 38, 0xFFA73560);
            lying(canvas, 30, 82, 0xFF82727B);
        } else if ("坐姿".equals(category)) {
            seated(canvas, 42, 22, 1, 0xFFA73560);
            seated(canvas, 118, 22, -1, 0xFF82727B);
        } else if ("面对面".equals(category)) {
            standing(canvas, 46, 22, 1, 0xFFA73560);
            standing(canvas, 114, 22, -1, 0xFF82727B);
            line.setColor(0xFFC69AAA);
            canvas.drawLine(62, 54, 78, 49, line);
            canvas.drawLine(98, 54, 82, 49, line);
        } else if ("后侧".equals(category)) {
            // Staggered pair distinguishes this category without sexual action.
            canvas.save();canvas.translate(24, 0);canvas.scale(.82f, .82f);
            standing(canvas, 49, 23, 1, 0xFF82727B);canvas.restore();
            standing(canvas, 106, 26, 1, 0xFFA73560);
        } else if ("站姿".equals(category)) {
            standing(canvas, 45, 22, -1, 0xFFA73560);
            standing(canvas, 115, 22, 1, 0xFF82727B);
        } else {
            // Neutral companion silhouettes and ellipsis for custom/unknown categories.
            bust(canvas, 46, 38, 0xFFA73560);
            bust(canvas, 114, 38, 0xFF82727B);
            line.setColor(0xFFC69AAA);
            for(int i=0;i<3;i++)canvas.drawCircle(74+i*6, 77, 1.5f, line);
        }
        canvas.restore();
    }
    private void bust(Canvas c,float x,float y,int color){
        line.setColor(color);c.drawCircle(x,y,9,line);
        Path shoulders=new Path();shoulders.moveTo(x-20,y+38);
        shoulders.cubicTo(x-20,y+12,x+20,y+12,x+20,y+38);
        shoulders.close();c.drawPath(shoulders,shirt);c.drawPath(shoulders,line);
        c.drawLine(x-6,y+20,x,y+27,line);c.drawLine(x,y+27,x+6,y+20,line);
    }
    private void head(Canvas c, float x, float y, int facing) {
        c.drawCircle(x, y, 8, line);
        c.drawLine(x + facing * 7, y, x + facing * 10, y + 2, line);
        c.drawLine(x - 3, y + 8, x - 3, y + 12, line);
        c.drawLine(x + 3, y + 8, x + 3, y + 12, line);
    }
    private void standing(Canvas c, float x, float y, int facing, int color) {
        line.setColor(color);
        head(c, x, y, facing);
        c.drawRoundRect(x - 10, y + 12, x + 10, y + 40, 3, 3, shirt);
        c.drawRoundRect(x - 10, y + 12, x + 10, y + 40, 3, 3, line);
        c.drawLine(x - 10, y + 15, x - 16, y + 32, line);
        c.drawLine(x - 16, y + 32, x - 13, y + 45, line);
        c.drawLine(x + 10, y + 15, x + 16, y + 32, line);
        c.drawLine(x + 16, y + 32, x + 13, y + 45, line);
        c.drawLine(x - 9, y + 40, x - 9, y + 70, line);
        c.drawLine(x, y + 42, x - 2, y + 70, line);
        c.drawLine(x + 9, y + 40, x + 9, y + 70, line);
        c.drawLine(x + 1, y + 42, x + 2, y + 70, line);
        c.drawLine(x - 10, y + 72, x - 2, y + 72, line);
        c.drawLine(x + 2, y + 72, x + 12, y + 72, line);
        c.drawLine(x - 20, y + 76, x + 20, y + 76, line);
    }
    private void seated(Canvas c, float x, float y, int facing, int color) {
        line.setColor(color);
        head(c, x, y, facing);
        c.drawRoundRect(x - 9, y + 12, x + 9, y + 38, 3, 3, shirt);
        c.drawRoundRect(x - 9, y + 12, x + 9, y + 38, 3, 3, line);
        c.drawLine(x + facing * 9, y + 16, x + facing * 17, y + 34, line);
        c.drawLine(x + facing * 17, y + 34, x + facing * 8, y + 38, line);
        c.drawLine(x - facing * 8, y + 40, x + facing * 22, y + 44, line);
        c.drawLine(x + facing * 22, y + 44, x + facing * 23, y + 65, line);
        c.drawLine(x + facing * 8, y + 39, x + facing * 29, y + 39, line);
        c.drawLine(x + facing * 29, y + 39, x + facing * 30, y + 64, line);
        c.drawLine(x + facing * 23, y + 67, x + facing * 33, y + 67, line);
        c.drawLine(x - 14, y + 43, x + 14, y + 43, line);
        c.drawLine(x - facing * 14, y + 17, x - facing * 14, y + 68, line);
        c.drawLine(x + facing * 12, y + 45, x + facing * 12, y + 68, line);
    }
    private void lying(Canvas c, float x, float y, int color) {
        c.save();
        c.translate(x, y);
        c.rotate(-90);
        standing(c, 0, 0, 1, color);
        c.restore();
    }
}
