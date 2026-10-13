package app.loveu;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.widget.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Charts draw recorded counts only; empty values never become invented observations. */
final class WorkbuddyInsightsView extends LinearLayout {
    final MainActivity activity;
    List<Domain.Event> events=new ArrayList<>();
    String range="全部",partner=null,place=null,source=null,monthlyMetric="次数"; int totalSamples;
    int year=LocalDate.now().getYear(); Integer heatmapYear,extremaYear; int firstYear=year-1,secondYear=year; boolean alignedYears=true,allYears=true; LinearLayout section;
    String page="总览",renderedPage="";
    boolean jumpToMonths=false; View monthSection;
    YearMonth month=YearMonth.now();
    LocalDate customStart=LocalDate.now().minusMonths(1),customEnd=LocalDate.now();
    WorkbuddyInsightsView(MainActivity activity){super(activity);this.activity=activity;setOrientation(VERTICAL);}
    void bind(List<Domain.Event> values){events=new ArrayList<>(values);render();}
    TextView text(String value,int size){return activity.text(value,size,activity.ink);}
    void note(String value){
        boolean explanation=value.contains("不代表")||value.contains("不推断")||value.contains("不等于")||value.contains("不参与")||value.contains("每柱")||value.contains("按当前伴侣")||value.contains("起止日期")||value.contains("同日截止：")||value.contains("部分月份")||value.contains("同日多次")||value.contains("未填写不按零")||value.contains("未记录月份不连接")||value.contains("全部历史没有")||value.contains("以下仅描述");
        if(explanation){
            if(value.startsWith("本人舒适度")||value.startsWith("舒适度样本")||value.startsWith("已知样本")){TextView data=activity.text(value.split("；")[0],12,activity.muted);data.setPadding(0,activity.dp(4),0,activity.dp(4));addView(data);}
            TextView info=activity.text("ⓘ 统计说明",11,activity.muted);info.setPadding(0,activity.dp(4),0,activity.dp(6));info.setMinHeight(activity.dp(32));info.setGravity(android.view.Gravity.CENTER_VERTICAL);info.setOnClickListener(v->{android.app.AlertDialog dialog=new android.app.AlertDialog.Builder(activity).setTitle("统计说明").setMessage(value).setPositiveButton("知道了",null).create();activity.dialogs.add(dialog);dialog.show();});addView(info);return;
        }
        TextView t=activity.text(value,12,activity.muted);t.setPadding(0,activity.dp(4),0,activity.dp(6));addView(t);
    }
        @Override public void addView(View child){if(section==null)super.addView(child);else section.addView(child);}
    @Override public void addView(View child,android.view.ViewGroup.LayoutParams params){if(section==null)super.addView(child,params);else section.addView(child,params);}
    void heading(String value){section=Ui.card(activity);super.addView(section);TextView t=text(value,17);t.setTypeface(null,Typeface.BOLD);section.addView(t);}
    Button compact(String value,Runnable action){Button b=activity.button(value,action);b.setTextSize(12);b.setMinHeight(0);b.setMinimumHeight(0);return b;}
    LinearLayout.LayoutParams chip(int height){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,activity.dp(height),1);p.setMargins(activity.dp(3),activity.dp(3),activity.dp(3),activity.dp(3));return p;}
    boolean back(){if(page.equals("总览"))return false;page="总览";render();return true;}
    void openModule(String name){page=name;if(name.equals("全部年份"))allYears=true;if(name.equals("两年对比"))allYears=false;render();}
    void openYear(int selected){year=selected;month=month.withYear(selected);range="年";page="总览";jumpToMonths=true;render();}
    void render(){
        android.view.ViewParent parent=getParent();while(parent!=null&&!(parent instanceof ScrollView))parent=parent.getParent();
        final ScrollView scroll=parent instanceof ScrollView?(ScrollView)parent:null;
        final int offset=scroll!=null&&page.equals(renderedPage)?scroll.getScrollY():0;
        boolean jumping=jumpToMonths;renderedPage=page;
        renderContent();
        if(scroll!=null&&!jumping)post(()->scroll.scrollTo(0,offset));
    }
    void renderContent(){
        removeAllViews();section=null;if(page.equals("总览")){TextView title=text("洞察",26);WorkbuddyUi.strong(title);title.setPadding(0,activity.dp(8),0,activity.dp(16));addView(title);}
        if(!page.equals("总览")){Button back=compact("‹  洞察总览",()->back());back.setContentDescription(Language.ui("返回洞察总览"));addView(back);TextView title=text(page,26);title.setTypeface(null,Typeface.BOLD);title.setPadding(0,activity.dp(12),0,activity.dp(16));addView(title);}
        LinearLayout selectors=new LinearLayout(activity);
        for(String option:new String[]{"全部","年","月","自定义"}){Button choice=compact(option,()->{range=option;render();});choice.setBackground(Ui.stroke(option.equals(range)?Ui.BERRY:Ui.SURFACE,option.equals(range)?Ui.BERRY:Ui.BORDER,22,activity));choice.setTextColor(option.equals(range)?Ui.ON_ACCENT:Ui.MUTED);selectors.addView(choice,chip(40));}
        if(page.equals("总览")||page.equals("时间与维度"))addView(selectors);LocalDate start,end;
        if(range.equals("月")){start=month.atDay(1);end=month.equals(YearMonth.now())?LocalDate.now():month.atEndOfMonth();}
        else if(range.equals("年")){start=LocalDate.of(year,1,1);end=year==LocalDate.now().getYear()?LocalDate.now():LocalDate.of(year,12,31);}
        else if(range.equals("自定义")){start=customStart;end=customEnd;}
        else {start=null;end=null;}
        if((page.equals("总览")||page.equals("时间与维度")||page.equals("月度趋势"))&&(range.equals("月")||range.equals("年"))){
            LinearLayout step=new LinearLayout(activity);
            step.addView(activity.button("上一"+range,()->{if(range.equals("月")){month=month.minusMonths(1);year=month.getYear();}else{year--;month=month.withYear(year);}render();}),chip(44));
            TextView label=text(range.equals("月")?month.toString():year+"年",18);label.setGravity(android.view.Gravity.CENTER);step.addView(label,chip(44));
            step.addView(activity.button("下一"+range,()->{if(range.equals("月")){month=month.plusMonths(1);year=month.getYear();}else{year++;month=month.withYear(year);}render();}),chip(44));addView(step);
        }
        if((page.equals("总览")||page.equals("时间与维度"))&&range.equals("自定义"))addView(activity.button(customStart+" 至 "+customEnd,()->custom()));
        List<Domain.Event> filtered=new ArrayList<>();for(Domain.Event e:events)if((partner==null||partner.equals(e.partner))&&(place==null||place.equals(e.place))&&(source==null||source.equals(e.source)))filtered.add(e);
        if(page.equals("时间与维度")){dimensionChoices("伴侣");dimensionChoices("地点");dimensionChoices("来源");note("筛选即时应用于洞察模块。");return;}
        if(page.equals("总览"))addView(compact("时间与维度  ›",()->openModule("时间与维度")));
        Analytics.Summary summary=Analytics.analyze(filtered,start,end);totalSamples=summary.base.events;
        List<Domain.Event> scoped=new ArrayList<>();for(Domain.Event e:filtered){LocalDate date=LocalDate.parse(e.date);if((start==null||!date.isBefore(start))&&(end==null||!date.isAfter(end)))scoped.add(e);}
        if(page.equals("总览")){
            dashboard(summary,scoped,start,end);
            for(String module:new String[]{"全部年份","两年对比","月度趋势","同比 / 环比","月度最高 / 最低","年度热力图","时长分析","时间习惯","记录分类","高潮分析","身体感受","满意度","记录完整度"})
                analysis(module,filtered,scoped,summary,start,end);
        }else analysis(page,filtered,scoped,summary,start,end);
    }
    void analysis(String module,List<Domain.Event> filtered,List<Domain.Event> scoped,Analytics.Summary summary,LocalDate start,LocalDate end){
        if(module.equals("全部年份")||module.equals("两年对比")){crossYears(filtered,module.equals("全部年份"));return;}
        if(module.equals("年度热力图")){monthHeatmap(summary.dateCounts,start,end);heatmap(scoped,summary.dateCounts,start,end);return;}
        if(module.equals("月度最高 / 最低")){extrema(filtered);return;}
        if(module.equals("记录完整度")){completeness(scoped);return;}
        if(module.equals("同比 / 环比")){comparison(filtered,start,end);return;}
        if(module.equals("时长分析")){
        heading("时长分析");durationMetrics(summary);note("已知时长 "+summary.base.knownDurations+" / "+summary.base.events+"；未知时长不按 0 计入。");
        chart("时长分布（分钟）",map(new String[]{"<5","5–15","15–30","30–60","60+"},summary.durationBins),summary.base.knownDurations,false,false);return;}
        if(module.equals("月度趋势")){
        TreeMap<String,Integer> months=new TreeMap<>();if(start!=null&&end!=null){for(YearMonth m=YearMonth.from(start);!m.isAfter(YearMonth.from(end));m=m.plusMonths(1))months.put(m.toString(),summary.monthly.getOrDefault(m.toString(),0));}else months.putAll(summary.monthly);
        monthlyChart(scoped,filtered,months.keySet(),start,end);monthSection=section;if(jumpToMonths){jumpToMonths=false;post(()->{if(monthSection!=null)monthSection.requestRectangleOnScreen(new android.graphics.Rect(0,0,monthSection.getWidth(),Math.min(monthSection.getHeight(),activity.dp(300))),false);});}return;}
        if(module.equals("时间习惯")){
        chart("星期分布",map(new String[]{"一","二","三","四","五","六","日"},summary.weekdays),summary.base.events,false,false);
        String[] hours=new String[24];for(int i=0;i<24;i++)hours[i]=String.valueOf(i);chart("小时分布",map(hours,summary.hours),summary.knownTimes,false,false);note("未知时间 "+(summary.base.events-summary.knownTimes)+" 次不参与小时分布。");
        chart("记录日间隔（天）",map(new String[]{"1","2–6","7–13","14–29","30+"},summary.intervalBins),sum(summary.intervalBins),false,false);note("同日多次合并为一个日期计算间隔。");
        return;}
        if(module.equals("记录分类")){
        chart("姿势",summary.positionCounts,known(scoped,e->!e.positions.isEmpty()),true,false);
        chart("保护措施",summary.protectionCounts,known(scoped,e->e.protection!=null),false,false);
        chart("射精情况",summary.outcomeCounts,known(scoped,e->!e.outcomes.isEmpty()),true,false);
        chart("地点",summary.places,known(scoped,e->e.place!=null),false,false);
        chart("爱浪声",summary.vocalCounts,known(scoped,e->e.vocalLevel!=null),false,false);
        return;}
        if(module.equals("高潮分析")){
        heading("高潮记录");note("本人 "+rate(summary.selfOrgasmRate)+"（已知 "+summary.selfKnown+" / "+summary.base.events+"）；对方 "+rate(summary.partnerOrgasmRate)+"（已知 "+summary.partnerKnown+" / "+summary.base.events+"）");
        Map<String,Integer> selfCounts=new TreeMap<>(),partnerCounts=new TreeMap<>();for(Domain.Event e:scoped){if(e.selfOrgasmCount!=null)inc(selfCounts,e.selfOrgasmCount+"次");if(e.partnerOrgasmCount!=null)inc(partnerCounts,e.partnerOrgasmCount+"次");}
        addView(new RateRing(activity,summary.selfOrgasmRate,"本人 · 已知 "+summary.selfKnown),new LinearLayout.LayoutParams(-1,activity.dp(152)));
        chart("本人高潮次数",selfCounts,summary.selfCountKnown,true,false);heading("对方高潮记录");addView(new RateRing(activity,summary.partnerOrgasmRate,"对方 · 已知 "+summary.partnerKnown),new LinearLayout.LayoutParams(-1,activity.dp(152)));chart("对方高潮次数",partnerCounts,summary.partnerCountKnown,true,false);
        return;}
        if(module.equals("身体感受")){
        bodyMetrics(scoped);heading("身体感受样本");note("本人舒适度 "+mean(summary.selfComfortMean)+"（样本 "+summary.selfComfortKnown+" / "+summary.base.events+"）；对方 "+mean(summary.partnerComfortMean)+"（样本 "+summary.partnerComfortKnown+" / "+summary.base.events+"）；分值 1–5。以下仅描述记录，不推断原因或诊断。");
        conditionTrend(scoped,true);conditionTrend(scoped,false);
        chart("本人身体标签",summary.selfBodyTags,known(scoped,e->!e.selfCondition.tags.isEmpty()),true,false);
        chart("对方身体标签",summary.partnerBodyTags,known(scoped,e->!e.partnerCondition.tags.isEmpty()),true,false);
        return;}
        if(module.equals("满意度")){
        Map<String,Integer> ratings=new TreeMap<>();for(int score=0;score<=5;score++)ratings.put(score+"分",0);for(Domain.Event e:scoped)if(e.rating!=null)inc(ratings,e.rating+"分");chart("满意度分布",ratings,known(scoped,e->e.rating!=null),false,false);
        TreeMap<String,double[]> ratingMonths=new TreeMap<>();for(Domain.Event e:scoped)if(e.rating!=null){double[] values=ratingMonths.computeIfAbsent(e.date.substring(0,7),key->new double[4]);values[0]+=e.rating;values[1]++;}if(!ratingMonths.isEmpty()){fillMissingMonths(ratingMonths);heading("满意度月度趋势");note("已知样本 "+known(scoped,e->e.rating!=null)+" / "+summary.base.events+"；均值 0–5，未记录月份不连接。");trend(ratingMonths,"满意度",null);}
        }
    }


    void durationMetrics(Analytics.Summary s){
        String[] labels={"最短","最长","累计","平均","中位"};Double[] values={s.shortestDurationMillis==null?null:s.shortestDurationMillis.doubleValue(),s.longestDurationMillis==null?null:s.longestDurationMillis.doubleValue(),s.totalDurationMillis==null?null:s.totalDurationMillis.doubleValue(),s.base.meanDuration,s.base.medianDuration};
        for(int offset=0;offset<5;offset+=2){LinearLayout row=new LinearLayout(activity);for(int i=offset;i<Math.min(offset+2,5);i++){LinearLayout cell=metricCard(values[i]==null?"—":String.format(Locale.CHINA,"%.1f",values[i]/60000),labels[i]+" · 分钟");cell.setTag("duration-metric-"+i);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(activity.dp(3),activity.dp(4),activity.dp(3),activity.dp(4));row.addView(cell,lp);}addView(row);}
    }
    void bodyMetrics(List<Domain.Event> scoped){
        heading("双方舒适度 / 疲劳摘要（1–5）");for(boolean self:new boolean[]{true,false})for(boolean comfort:new boolean[]{true,false}){double sum=0;int n=0;for(Domain.Event e:scoped){Domain.Condition c=self?e.selfCondition:e.partnerCondition;Integer value=comfort?c.comfort:c.fatigue;if(value!=null){sum+=value;n++;}}LinearLayout row=new LinearLayout(activity);TextView label=activity.text((self?"我":"对方")+" · "+(comfort?"舒适度":"疲劳"),12,Ui.MUTED);row.addView(label,new LinearLayout.LayoutParams(0,activity.dp(36),1));TextView value=activity.text(n==0?"暂无样本":String.format(Locale.CHINA,"%.2f · %d 个样本",sum/n,n),14,Ui.BERRY);row.addView(value);addView(row);}
    }
    static class MonthlyTrend extends ChartCanvas {
        final LinkedHashMap<String,double[]> data;final Paint p=new Paint(3);MonthlyTrend(Context c,LinkedHashMap<String,double[]> data){super(c);this.data=data;setContentDescription("当前范围与去年相同日期双线趋势，未知样本断线");}
        protected void onDraw(Canvas c){float d=getResources().getDisplayMetrics().density,bottom=getHeight()-32*d,top=22*d,unit=(float)getWidth()/Math.max(1,data.size());double max=1;for(double[] values:data.values())for(double value:values)if(Double.isFinite(value))max=Math.max(max,value);p.setTextSize(10*d);p.setTextAlign(Paint.Align.CENTER);p.setColor(Ui.BORDER);p.setStrokeWidth(d);c.drawLine(0,bottom,getWidth(),bottom,p);int i=0;for(String key:data.keySet()){p.setColor(Ui.MUTED);c.drawText(Language.ui(key.substring(2)),unit*(i+++.5f),bottom+21*d,p);}for(int series=0;series<2;series++){Path line=new Path();boolean connected=false;i=0;for(double[] values:data.values()){float x=unit*(i+++.5f);double value=values[series];if(!Double.isFinite(value)){connected=false;continue;}float y=bottom-(float)(value/max)*(bottom-top);if(connected)line.lineTo(x,y);else line.moveTo(x,y);connected=true;}p.setColor(series==0?Ui.BERRY:Ui.TEAL);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f*d);p.setPathEffect(series==1?new DashPathEffect(new float[]{6*d,4*d},0):null);c.drawPath(line,p);p.setPathEffect(null);p.setStyle(Paint.Style.FILL);}p.setTextAlign(Paint.Align.LEFT);p.setColor(Ui.BERRY);c.drawText(Language.ui("当前"),8*d,13*d,p);p.setColor(chartTone(1));c.drawText(Language.ui("去年同期"),60*d,13*d,p);}
    }
    void comparisonCard(String label,Analytics.Comparison c,LocalDate start,LocalDate end,LocalDate from,LocalDate to){heading(label+" · 当前 "+c.current+" 次");TextView change=activity.text(comparisonText(c),21,c.delta>=0?Ui.TEAL:Ui.BERRY);change.setTypeface(null,Typeface.BOLD);addView(change);note("当前 "+start+" 至 "+end);note("对照 "+from+" 至 "+to);}
    void dimensionChoices(String kind){
        heading(kind);TreeSet<String> values=new TreeSet<>();for(Domain.Event e:events){String v=kind.equals("伴侣")?e.partner:kind.equals("地点")?e.place:e.source;if(v!=null&&!v.isEmpty())values.add(v);}
        String selected=kind.equals("伴侣")?partner:kind.equals("地点")?place:source;
        HorizontalScrollView scroll=new HorizontalScrollView(activity);scroll.setHorizontalScrollBarEnabled(false);LinearLayout row=new LinearLayout(activity);
        List<String> options=new ArrayList<>();options.add(null);options.addAll(values);
        for(String value:options){boolean checked=Objects.equals(selected,value);Button b=compact(value==null?"全部":Language.raw(value),()->{if(kind.equals("伴侣"))partner=value;else if(kind.equals("地点"))place=value;else source=value;render();});b.setBackground(Ui.stroke(checked?Ui.BERRY:Ui.SURFACE,checked?Ui.BERRY:Ui.BORDER,22,activity));b.setTextColor(checked?Ui.ON_ACCENT:Ui.INK);b.setContentDescription(Language.ui(kind+"："+(value==null?"全部":value)+(checked?"，已选择":"")));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,activity.dp(44));lp.setMargins(0,activity.dp(8),activity.dp(8),0);row.addView(b,lp);}
        scroll.addView(row);addView(scroll);
    }
    void dashboard(Analytics.Summary summary,List<Domain.Event> scoped,LocalDate start,LocalDate end){
        LinearLayout metrics=new LinearLayout(activity);metrics.addView(metricCard(String.valueOf(summary.base.events),"总次数"),chip(98));metrics.addView(metricCard(String.valueOf(summary.base.days),"记录天数"),chip(98));addView(metrics);
        heading("次数趋势");if(scoped.isEmpty())note("当前范围暂无记录，记录后可查看实际趋势。");else{TreeMap<String,Integer> data=new TreeMap<>(summary.monthly);if(start!=null&&end!=null)for(YearMonth m=YearMonth.from(start);!m.isAfter(YearMonth.from(end));m=m.plusMonths(1))data.putIfAbsent(m.toString(),0);HorizontalScrollView scroll=new HorizontalScrollView(activity);scroll.setHorizontalScrollBarEnabled(false);scroll.addView(new CountTrend(activity,data),new android.view.ViewGroup.LayoutParams(Math.max(activity.getResources().getDisplayMetrics().widthPixels-activity.dp(68),activity.dp(data.size()*48)),activity.dp(170)));addView(scroll);}
        note((start==null?"全部历史":start+" 至 "+end)+" · "+(partner==null?"全部伴侣":partner)+" / "+(place==null?"全部地点":place)+" / "+(source==null?"全部来源":source));

    }
    LinearLayout metricCard(String value,String label){LinearLayout box=Ui.card(activity);box.setPadding(activity.dp(12),activity.dp(12),activity.dp(12),activity.dp(12));box.setMinimumHeight(activity.dp(90));TextView number=activity.text(value,28,Ui.BERRY);number.setTypeface(null,Typeface.BOLD);number.setSingleLine(true);number.setAutoSizeTextTypeUniformWithConfiguration(12,28,1,android.util.TypedValue.COMPLEX_UNIT_SP);box.addView(number,new LinearLayout.LayoutParams(-1,activity.dp(44)));TextView caption=activity.text(label,12,activity.muted);box.addView(caption,new LinearLayout.LayoutParams(-1,-2));box.setContentDescription(Language.ui(label+" "+value));return box;}
    void monthHeatmap(Map<String,Integer> counts,LocalDate start,LocalDate end){
        heading("月度概况");if(heatmapYear==null)heatmapYear=year;LinearLayout picker=new LinearLayout(activity);picker.addView(compact("‹",()->{heatmapYear--;render();}),chip(44));picker.addView(compact(heatmapYear+"年",()->yearPicker("热力图年份",heatmapYear,y->heatmapYear=y)),chip(44));picker.addView(compact("›",()->{heatmapYear++;render();}),chip(44));addView(picker);
        int selected=heatmapYear;int maximum=1;for(int monthIndex=1;monthIndex<=12;monthIndex++){int n=0;String prefix=YearMonth.of(selected,monthIndex).toString();for(Map.Entry<String,Integer> e:counts.entrySet())if(e.getKey().startsWith(prefix)&&!LocalDate.parse(e.getKey()).isAfter(LocalDate.now()))n+=e.getValue();maximum=Math.max(maximum,n);}final int peak=maximum;for(int row=0;row<3;row++){LinearLayout tiles=new LinearLayout(activity);for(int col=0;col<4;col++){YearMonth m=YearMonth.of(selected,row*4+col+1);boolean future=m.isAfter(YearMonth.now());int count=0;for(Map.Entry<String,Integer> entry:counts.entrySet()){LocalDate d=LocalDate.parse(entry.getKey());if(YearMonth.from(d).equals(m)&&!d.isAfter(LocalDate.now()))count+=entry.getValue();}final int n=count;Button tile=compact(m.getMonthValue()+"月\n"+(future?"未来":n+"次"),()->{activity.month=m;activity.filter=null;activity.tab="日历";activity.show();});tile.setTag("month-overview-"+m);tile.setTextColor(future?Ui.MUTED:n==0?Ui.INK:0xff3b2d1d);tile.setBackground(Ui.stroke(future?Ui.BORDER:n==0?Ui.SURFACE:chartTone(Math.min(4,(int)Math.ceil(n*4.0/peak))),Ui.SURFACE,16,activity));tile.setContentDescription(Language.ui(selected+"年"+m.getMonthValue()+"月，"+(future?"未来，不统计":n+"次，查看月历")));tiles.addView(tile,chip(76));}addView(tiles);}note("点击月份查看该月日历；未来月份不统计。");
    }
    static int chartTone(int level){int[] colors={0xffF7D9A8,0xffF0B96E,0xffE08A3C,0xffC96A2E,0xffC96A2E};return colors[Math.max(0,Math.min(4,level))];}
    static class CountTrend extends ChartCanvas {
        final TreeMap<String,Integer> data;final Paint p=new Paint(3);CountTrend(Context c,TreeMap<String,Integer> values){super(c);data=values;setContentDescription("月度实际次数趋势 "+data);}
        protected void onDraw(Canvas c){float d=getResources().getDisplayMetrics().density,w=getWidth(),bottom=getHeight()-28*d,top=18*d,unit=w/Math.max(1,data.size());int max=1;for(int n:data.values())max=Math.max(max,n);p.setColor(Ui.BORDER);p.setStrokeWidth(d);c.drawLine(0,bottom,w,bottom,p);Path path=new Path();int i=0;float lastX=0,lastY=bottom;p.setTextSize(10*d);p.setTextAlign(Paint.Align.CENTER);for(Map.Entry<String,Integer> e:data.entrySet()){float x=unit*(i+.5f),y=bottom-(bottom-top)*e.getValue()/max;if(i++==0)path.moveTo(x,y);else path.lineTo(x,y);p.setColor(Ui.MUTED);c.drawText(Language.ui(e.getKey().substring(2)),x,bottom+19*d,p);lastX=x;lastY=y;}p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f*d);p.setColor(chartTone(3));c.drawPath(path,p);p.setStyle(Paint.Style.FILL);c.drawCircle(lastX,lastY,4*d,p);}
    }
    static class RateRing extends ChartCanvas {
        final Double rate;final String label;final Paint p=new Paint(3);RateRing(Context c,Double value,String label){super(c);rate=value;this.label=label;setContentDescription(label+"，"+(value==null?"暂无样本":String.format(Locale.CHINA,"%.1f%%",value)));}
        protected void onDraw(Canvas c){float d=getResources().getDisplayMetrics().density,cx=getWidth()/2f,cy=65*d,r=44*d;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(11*d);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(Ui.BORDER);c.drawCircle(cx,cy,r,p);if(rate!=null){p.setColor(chartTone(3));c.drawArc(cx-r,cy-r,cx+r,cy+r,-90,(float)(rate*3.6),false,p);}p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(23*d);p.setColor(chartTone(3));c.drawText(Language.ui(rate==null?"—":String.format(Locale.CHINA,"%.1f%%",rate)),cx,cy+8*d,p);p.setTextSize(12*d);p.setColor(Ui.MUTED);c.drawText(Language.ui(label),cx,133*d,p);}
    }
    static class CoverageChart extends ChartCanvas {
        final Map<String,Integer> counts;final int total;final Paint p=new Paint(3);CoverageChart(Context c,Map<String,Integer> counts,int total){super(c);this.counts=counts;this.total=total;setContentDescription("各字段已记录 / 总数 "+counts+" / "+total);}
        protected void onDraw(Canvas c){float d=getResources().getDisplayMetrics().density,w=getWidth(),left=w*.32f,right=w-65*d;p.setTextSize(11*d);int i=0;for(Map.Entry<String,Integer> row:counts.entrySet()){float y=(i++*34+9)*d;p.setColor(Ui.MUTED);String label=row.getKey();while(p.measureText(label)>left-8*d&&label.length()>1)label=label.substring(0,label.length()-1);c.drawText(Language.ui(label),0,y+13*d,p);p.setColor(Ui.BORDER);c.drawRoundRect(left,y,right,y+18*d,5*d,5*d,p);p.setColor(chartTone(3));c.drawRoundRect(left,y,left+(right-left)*row.getValue()/Math.max(1,total),y+18*d,5*d,5*d,p);p.setColor(Ui.MUTED);c.drawText(Language.ui(row.getValue()+"/"+total),right+8*d,y+13*d,p);}}
    }
    void trend(TreeMap<String,double[]> values,String first,String second){HorizontalScrollView scroll=new HorizontalScrollView(activity);scroll.setHorizontalScrollBarEnabled(false);scroll.addView(new Trend(activity,values,first,second),new android.view.ViewGroup.LayoutParams(Math.max(activity.getResources().getDisplayMetrics().widthPixels-activity.dp(68),activity.dp(values.size()*48)),activity.dp(190)));addView(scroll);}
    void yearPicker(String title,int selected,java.util.function.IntConsumer callback){TreeSet<Integer> years=new TreeSet<>();years.add(LocalDate.now().getYear());for(Domain.Event e:events)years.add(LocalDate.parse(e.date).getYear());years.add(selected);int earliest=years.first(),latest=years.last();for(int y=earliest;y<=latest;y++)years.add(y);List<Integer> choices=new ArrayList<>(years);String[] labels=new String[choices.size()];for(int i=0;i<labels.length;i++)labels[i]=choices.get(i)+"年";android.app.AlertDialog dialog=new android.app.AlertDialog.Builder(activity).setTitle(title).setItems(labels,(d,i)->{callback.accept(choices.get(i));render();}).create();activity.dialogs.add(dialog);dialog.show();}
    void completeness(List<Domain.Event> values){
        heading("记录完整度");
        if(values.isEmpty()){note("当前范围暂无记录，添加后可查看各项填写情况。");return;}
        Map<String,Integer> counts=RecordCoverage.count(values);
        addView(new CoverageChart(activity,counts,values.size()),new LinearLayout.LayoutParams(-1,activity.dp(counts.size()*34+16)));
        note("未记录表示信息未知，不代表没有发生。历史记录可以保留空白；分析只使用对应的已知样本。");
    }
    void crossYears(List<Domain.Event> filtered,boolean annual){
        heading(annual?"跨年记录 · 全部年份":"两年对比");
        if(annual){TreeMap<String,Integer> data=new TreeMap<>();for(Domain.Event e:filtered){LocalDate d=LocalDate.parse(e.date);if(!d.isAfter(LocalDate.now()))inc(data,String.valueOf(d.getYear()));}if(data.isEmpty()){note("暂无记录");return;}int first=Integer.parseInt(data.firstKey()),last=Integer.parseInt(data.lastKey());for(int y=first;y<=last;y++)data.putIfAbsent(String.valueOf(y),0);note("每柱为该年全部已记录次数；本年截至 "+LocalDate.now()+"；0 表示筛选后未记录。");Bars bars=new Bars(activity,data,false,false);HorizontalScrollView scroll=new HorizontalScrollView(activity);scroll.setHorizontalScrollBarEnabled(false);scroll.addView(bars,new android.view.ViewGroup.LayoutParams(Math.max(activity.getResources().getDisplayMetrics().widthPixels-activity.dp(68),activity.dp(data.size()*40)),activity.dp(184)));addView(scroll);final List<String> keys=new ArrayList<>(data.keySet());final TapGesture tap=new TapGesture(android.view.ViewConfiguration.get(activity).getScaledTouchSlop());bars.setOnTouchListener((v,event)->{int action=event.getActionMasked();if(event.getPointerCount()>1){tap.cancel();return true;}if(action==android.view.MotionEvent.ACTION_DOWN)tap.start(event.getX(),event.getY());else if(action==android.view.MotionEvent.ACTION_MOVE)tap.move(event.getX(),event.getY());else if(action==android.view.MotionEvent.ACTION_CANCEL)tap.cancel();else if(action==android.view.MotionEvent.ACTION_UP){if(!tap.finish(event.getX(),event.getY())||event.getX()<0||event.getX()>=v.getWidth()||event.getY()<0||event.getY()>=v.getHeight())return true;v.performClick();int index=Math.min(keys.size()-1,Math.max(0,(int)(event.getX()/v.getWidth()*keys.size())));openYear(Integer.parseInt(keys.get(index)));return true;}return true;});addView(compact("选择年度查看月度明细",()->yearPicker("年度月度明细",year,y->{year=y;month=month.withYear(y);range="年";page="总览";jumpToMonths=true;})));note("也可点选年度柱查看月度明细。");return;}
        LinearLayout choose=new LinearLayout(activity);choose.addView(compact(firstYear+"年",()->yearPicker("第一年",firstYear,y->firstYear=y)),chip(48));choose.addView(compact(secondYear+"年",()->yearPicker("第二年",secondYear,y->secondYear=y)),chip(48));addView(choose);addView(compact(alignedYears?"同日截止 · 切换全年已记录":"全年已记录 · 切换同日截止",()->{alignedYears=!alignedYears;render();}));
        Analytics.YearComparison result=Analytics.compareYears(filtered,firstYear,secondYear,alignedYears,LocalDate.now());note("当前 "+firstYear+"年 "+result.firstTotal+"次 / 对照 "+secondYear+"年 "+result.secondTotal+"次");note(result.aligned?"同日截止：分别至 "+result.firstEnd+" / "+result.secondEnd+"；截止后的月份不参与。":"全年已记录总数；当前年尚未完整，不代表完整年度活动。");LinkedHashMap<String,double[]> values=new LinkedHashMap<>();for(int i=0;i<12;i++)values.put((i+1)+"月",new double[]{result.aligned&&i>=result.firstEnd.getMonthValue()?Double.NaN:result.firstMonths[i],result.aligned&&i>=result.secondEnd.getMonthValue()?Double.NaN:result.secondMonths[i]});HorizontalScrollView scroll=new HorizontalScrollView(activity);scroll.setHorizontalScrollBarEnabled(false);scroll.addView(new MonthlyChart(activity,values),new android.view.ViewGroup.LayoutParams(activity.dp(12*58),activity.dp(200)));addView(scroll);
    }
    void extrema(List<Domain.Event> filtered){heading("月度最高 / 最低");LinearLayout choose=new LinearLayout(activity);choose.addView(compact(extremaYear==null?"✓ 全部历史":"全部历史",()->{extremaYear=null;render();}),chip(48));choose.addView(compact(extremaYear==null?"选择年份":extremaYear+"年",()->yearPicker("最高 / 最低范围",year,y->extremaYear=y)),chip(48));addView(choose);Analytics.MonthExtrema result=Analytics.monthExtrema(filtered,extremaYear,LocalDate.now());note("按当前伴侣、地点、来源筛选；首个至最后记录月之间的完整月份，包含零记录月；本月及未来月份排除。");if(result.minimum==null){note("暂无可比较的完整月份");return;}heading("最高 "+result.maximum+" 次");note(String.join("、",result.highest));heading("最低 "+result.minimum+" 次");note(String.join("、",result.lowest));note("0 仅表示没有匹配记录；所有并列月份均列出。");}    interface Test{boolean known(Domain.Event e);}int known(List<Domain.Event> es,Test t){int n=0;for(Domain.Event e:es)if(t.known(e))n++;return n;}
    int sum(int[] a){int n=0;for(int v:a)n+=v;return n;}
    void inc(Map<String,Integer> m,String key){m.put(key,m.getOrDefault(key,0)+1);}
    String duration(Double ms){return ms==null?"暂无样本":String.format(Locale.CHINA,"%.1f 分钟",ms/60000);}
    String rate(Double r){return r==null?"暂无样本":String.format(Locale.CHINA,"%.1f%%",r);}
    String mean(Double n){return n==null?"暂无样本":String.format(Locale.CHINA,"%.2f",n);}
    Map<String,Integer> map(String[] labels,int[] values){Map<String,Integer> m=new LinkedHashMap<>();for(int i=0;i<labels.length;i++)m.put(labels[i],values[i]);return m;}
    void chart(String title,Map<String,Integer> data,int samples,boolean multi,boolean line){
        heading(title);note("已知样本 "+samples+" / "+(title.startsWith("记录日间隔")?samples:totalSamples)+"；未知 "+(title.startsWith("记录日间隔")?0:Math.max(0,totalSamples-samples))+(multi?"；可多选，标签计数可重复":"")+"；0 表示未记录");
        if(data.isEmpty()||samples==0){note("暂无已知样本");return;}
        // Keep every category readable, including long addresses and dense hours.
        if(title.equals("小时分布")){HorizontalScrollView scroll=new HorizontalScrollView(activity);scroll.setHorizontalScrollBarEnabled(false);scroll.addView(new Bars(activity,data,false,line),new android.view.ViewGroup.LayoutParams(activity.dp(data.size()*38),activity.dp(184)));addView(scroll);}
        else if(data.size()>12||title.equals("地点")||multi||title.equals("保护措施")||title.equals("爱浪声")){addView(new Bars(activity,data,true,line),new LinearLayout.LayoutParams(-1,activity.dp(Math.max(100,data.size()*38+20))));}
        else addView(new Bars(activity,data,false,line),new LinearLayout.LayoutParams(-1,activity.dp(184)));
    }
    void monthlyChart(List<Domain.Event> scoped,List<Domain.Event> filtered,Set<String> months,LocalDate selectedStart,LocalDate selectedEnd){
        heading("月度趋势 · "+monthlyMetric);
        if(range.equals("年"))addView(compact("选择月份查看具体记录",()->{String[] labels=new String[12];for(int i=0;i<12;i++){YearMonth m=YearMonth.of(year,i+1);labels[i]=(i+1)+"月 · "+Analytics.analyze(filtered,m.atDay(1),m.atEndOfMonth()).base.events+" 次";}android.app.AlertDialog d=new android.app.AlertDialog.Builder(activity).setTitle(year+"年 · 月度记录").setItems(labels,(dialog,index)->{activity.month=YearMonth.of(year,index+1);activity.filter=null;activity.tab="日历";activity.show();}).create();activity.dialogs.add(d);d.show();}));
        for(String[] group:new String[][]{{"次数","天数","总时长"},{"平均","中位"}}){
            LinearLayout options=new LinearLayout(activity);
            for(String name:group){boolean selected=name.equals(monthlyMetric);Button button=compact((selected?"✓ ":"")+name,()->{monthlyMetric=name;render();});button.setSelected(selected);button.setContentDescription(Language.ui(name+(selected?"，已选择":"")));options.addView(button,chip(48));}
            if(group.length==2)options.addView(new View(activity),chip(48));
            addView(options);
        }
        LinkedHashMap<String,double[]> values=new LinkedHashMap<>();int samples=0;
        for(String key:months){
            YearMonth m=YearMonth.parse(key);LocalDate from=m.atDay(1),to=m.atEndOfMonth();
            if(selectedStart!=null&&selectedStart.isAfter(from))from=selectedStart;
            if(selectedEnd!=null&&selectedEnd.isBefore(to))to=selectedEnd;
            Analytics.Summary current=Analytics.analyze(scoped,from,to);
            Analytics.Summary previous=Analytics.analyze(filtered,from.minusYears(1),to.minusYears(1));
            samples+=monthlyMetric.equals("次数")||monthlyMetric.equals("天数")?current.base.events:current.base.knownDurations;
            values.put(key,new double[]{metric(current,monthlyMetric),metric(previous,monthlyMetric)});
        }
        note("当前 当前选定日期 / 对照 去年相同日期；部分月份按同日起止对齐；"+(monthlyMetric.equals("次数")||monthlyMetric.equals("天数")?"按事件统计":"单位分钟，未知时长排除")+"；当前有效样本 "+samples+" / "+totalSamples);
        if(values.isEmpty()){note("暂无记录");return;}HorizontalScrollView scroll=new HorizontalScrollView(activity);scroll.setHorizontalScrollBarEnabled(false);MonthlyTrend chart=new MonthlyTrend(activity,values);chart.setContentDescription(Language.ui("当前与去年同日期范围月度趋势 "+monthlyMetric+"，点选月份查看日历"));scroll.addView(chart,new android.view.ViewGroup.LayoutParams(Math.max(activity.getResources().getDisplayMetrics().widthPixels-activity.dp(40),activity.dp(values.size()*58)),activity.dp(200)));addView(scroll);
        final List<String> monthKeys=new ArrayList<>(values.keySet());final TapGesture monthTap=new TapGesture(android.view.ViewConfiguration.get(activity).getScaledTouchSlop());
        chart.setOnTouchListener((v,event)->{int action=event.getActionMasked();if(event.getPointerCount()>1){monthTap.cancel();return true;}if(action==android.view.MotionEvent.ACTION_DOWN)monthTap.start(event.getX(),event.getY());else if(action==android.view.MotionEvent.ACTION_MOVE)monthTap.move(event.getX(),event.getY());else if(action==android.view.MotionEvent.ACTION_CANCEL)monthTap.cancel();else if(action==android.view.MotionEvent.ACTION_UP){if(!monthTap.finish(event.getX(),event.getY())||event.getX()<0||event.getX()>=v.getWidth()||event.getY()<0||event.getY()>=v.getHeight())return true;v.performClick();int index=Math.min(monthKeys.size()-1,(int)(event.getX()/v.getWidth()*monthKeys.size()));activity.month=YearMonth.parse(monthKeys.get(index));activity.filter=null;activity.tab="日历";activity.show();}return true;});
        note("点选月份查看每日记录。");
    }
    double metric(Analytics.Summary s,String key){if(key.equals("次数"))return s.base.events;if(key.equals("天数"))return s.base.days;if(key.equals("平均"))return s.base.meanDuration==null?Double.NaN:s.base.meanDuration/60000;if(key.equals("中位"))return s.base.medianDuration==null?Double.NaN:s.base.medianDuration/60000;if(s.base.knownDurations==0)return Double.NaN;return s.base.meanDuration*s.base.knownDurations/60000;}
    void conditionTrend(List<Domain.Event> es,boolean self){
        TreeMap<String,double[]> grouped=new TreeMap<>();int comfortN=0,fatigueN=0;
        for(Domain.Event e:es){Domain.Condition c=self?e.selfCondition:e.partnerCondition;double[] v=grouped.computeIfAbsent(e.date.substring(0,7),k->new double[4]);if(c.comfort!=null){v[0]+=c.comfort;v[1]++;comfortN++;}if(c.fatigue!=null){v[2]+=c.fatigue;v[3]++;fatigueN++;}}
        heading((self?"本人":"对方")+"舒适度 / 疲劳趋势");note("舒适度样本 "+comfortN+"；疲劳样本 "+fatigueN+"；未填写不按零计算。");
        if(comfortN+fatigueN==0){note("暂无已知样本");return;}fillMissingMonths(grouped);trend(grouped,"舒适度","疲劳");
    }
    void fillMissingMonths(TreeMap<String,double[]> data){
        if(data.isEmpty())return;YearMonth first=YearMonth.parse(data.firstKey()),last=YearMonth.parse(data.lastKey());
        for(YearMonth m=first;!m.isAfter(last);m=m.plusMonths(1))data.putIfAbsent(m.toString(),new double[4]);
    }
    void comparison(List<Domain.Event> filtered,LocalDate start,LocalDate end){
        heading("选定范围比较");
        if(start==null||end==null){note("全部历史没有固定对照期间；选择月、年或自定义日期后查看同比与环比。");return;}
        LocalDate previousYearStart=start.minusYears(1),previousYearEnd=end.minusYears(1);
        LocalDate previousMonthStart=start.minusMonths(1),previousMonthEnd=end.minusMonths(1);
        Analytics.Comparison yoy=Analytics.compare(filtered,start,end,previousYearStart,previousYearEnd);
        Analytics.Comparison mom=Analytics.compare(filtered,start,end,previousMonthStart,previousMonthEnd);
        comparisonCard("同比",yoy,start,end,previousYearStart,previousYearEnd);
        comparisonCard("环比",mom,start,end,previousMonthStart,previousMonthEnd);
        note("起止日期分别平移一年 / 一月；不存在的日期取该月最后一天。");
    }
    void heatmap(List<Domain.Event> scoped,Map<String,Integer> counts,LocalDate start,LocalDate end){
        final TreeSet<Integer> years=new TreeSet<>();
        if(start!=null&&end!=null){for(int y=start.getYear();y<=end.getYear();y++)years.add(y);}else for(Domain.Event e:scoped)years.add(LocalDate.parse(e.date).getYear());
        if(years.isEmpty())years.add(LocalDate.now().getYear());
        if(heatmapYear==null)heatmapYear=years.last();
        if(years.size()>1)addView(activity.button("热力图年份 · "+heatmapYear,()->{
            List<Integer> options=new ArrayList<>(years);String[] labels=new String[options.size()];for(int i=0;i<labels.length;i++)labels[i]=options.get(i)+"年";
            android.app.AlertDialog dialog=new android.app.AlertDialog.Builder(activity).setTitle("范围内的年份").setItems(labels,(d,i)->{heatmapYear=options.get(i);render();}).create();activity.dialogs.add(dialog);dialog.show();
        }));
        int selectedYear=heatmapYear;LocalDate from=LocalDate.of(selectedYear,1,1),to=LocalDate.of(selectedYear,12,31);
        if(start!=null&&start.isAfter(from))from=start;if(end!=null&&end.isBefore(to))to=end;
        Map<String,Integer> selected=new TreeMap<>();int samples=0;for(Map.Entry<String,Integer> entry:counts.entrySet())if(entry.getKey().startsWith(String.valueOf(selectedYear)+"-")){selected.put(entry.getKey(),entry.getValue());samples+=entry.getValue();}
        heading(selectedYear+" 年日历热力图");addView(new Heatmap(activity,selected,selectedYear,from,to),new LinearLayout.LayoutParams(-1,activity.dp(472)));
        note(from+" 至 "+to+"；样本 "+samples+" 次。每格一天；未记录不等于没有发生。范围外仅显示轮廓，未来不统计。");
    }
    String comparisonText(Analytics.Comparison c){return c.percent==null?"新增 "+c.current+" 次（对照为 0）":(c.delta>=0?"+":"")+c.delta+" 次 / "+String.format(Locale.CHINA,"%+.1f%%",c.percent);}
    void custom(){LinearLayout box=activity.column();EditText s=activity.field(box,"起始日期 YYYY-MM-DD",customStart.toString(),1),e=activity.field(box,"结束日期 YYYY-MM-DD",customEnd.toString(),1);android.app.AlertDialog d=new android.app.AlertDialog.Builder(activity).setTitle("自定义范围").setView(box).setNegativeButton("取消",null).setPositiveButton("应用",null).create();d.setOnShowListener(v->d.getButton(-1).setOnClickListener(w->activity.attempt(()->{LocalDate start=LocalDate.parse(Domain.validateDate(s.getText().toString().trim())),end=LocalDate.parse(Domain.validateDate(e.getText().toString().trim()));if(end.isBefore(start))throw new IllegalArgumentException("结束日期不能早于起始日期");customStart=start;customEnd=end;d.dismiss();render();})));activity.dialogs.add(d);d.show();}
    void filter(String kind){TreeSet<String> values=new TreeSet<>();for(Domain.Event e:events){String v=kind.equals("伴侣")?e.partner:kind.equals("地点")?e.place:e.source;if(v!=null&&!v.isEmpty())values.add(v);}List<String> options=new ArrayList<>();options.add("全部");options.addAll(values);android.app.AlertDialog d=new android.app.AlertDialog.Builder(activity).setTitle(kind).setItems(options.toArray(new String[0]),(dialog,i)->{String value=i==0?null:options.get(i);if(kind.equals("伴侣"))partner=value;else if(kind.equals("地点"))place=value;else source=value;render();}).create();activity.dialogs.add(d);d.show();}
    /** HorizontalScrollView supplies UNSPECIFIED width; a plain View measures to zero. */
    static abstract class ChartCanvas extends View {
        ChartCanvas(Context context){super(context);}
        @Override protected void onMeasure(int widthSpec,int heightSpec){
            android.view.ViewGroup.LayoutParams params=getLayoutParams();
            int width=params!=null&&params.width>0?params.width:MeasureSpec.getSize(widthSpec);
            if(width<=0)width=(int)(320*getResources().getDisplayMetrics().density);
            int height=params!=null&&params.height>0?params.height:(int)(184*getResources().getDisplayMetrics().density);
            setMeasuredDimension(resolveSize(width,widthSpec),resolveSize(height,heightSpec));
        }
    }
    static class Bars extends ChartCanvas {
        final List<Map.Entry<String,Integer>> entries;final boolean horizontal,line;final Paint paint=new Paint(3);final int berry=chartTone(3),muted=Ui.MUTED;
        Bars(Context c,Map<String,Integer> values,boolean horizontal,boolean line){super(c);entries=new ArrayList<>(values.entrySet());this.horizontal=horizontal;this.line=line;setContentDescription("实际记录图表 "+values.toString());}
        float dp(float n){return n*getResources().getDisplayMetrics().density;}
        protected void onDraw(Canvas canvas){super.onDraw(canvas);float w=getWidth(),h=getHeight();int max=1;for(Map.Entry<String,Integer> e:entries)max=Math.max(max,e.getValue());max=(int)Math.ceil(max*1.15);paint.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));paint.setTextSize(dp(11));
            if(horizontal){float row=(h-dp(10))/entries.size(),label=Math.min(w*.45f,dp(180));for(int i=0;i<entries.size();i++){Map.Entry<String,Integer> e=entries.get(i);float y=i*row+dp(8);paint.setColor(muted);String key=e.getKey();while(paint.measureText(key)>label-dp(12)&&key.length()>1)key=key.substring(0,key.length()-1);canvas.drawText(Language.ui(key),0,y+dp(15),paint);paint.setColor(chartTone((int)Math.ceil(e.getValue()*3.0/max)));canvas.drawRoundRect(label,y,label+(w-label-dp(35))*e.getValue()/max,y+dp(17),dp(3),dp(3),paint);paint.setColor(muted);canvas.drawText(Language.ui(e.getValue().toString()),w-dp(28),y+dp(15),paint);}}
            else {float bottom=h-dp(32),top=dp(20),unit=w/entries.size();paint.setColor(Ui.BORDER);paint.setStrokeWidth(dp(1));for(int grid=0;grid<4;grid++){float gy=bottom-(bottom-top)*grid/3f;canvas.drawLine(0,gy,w,gy,paint);}Path trend=new Path();for(int i=0;i<entries.size();i++){Map.Entry<String,Integer> e=entries.get(i);float x=i*unit+unit/2,y=bottom-(bottom-top)*e.getValue()/max;paint.setColor(berry);int tone=chartTone((int)Math.ceil(e.getValue()*3.0/max));paint.setShader(new LinearGradient(x,y,x,bottom,tone,Ui.blend(tone,Ui.SURFACE),Shader.TileMode.CLAMP));canvas.drawRoundRect(x-unit*.22f,y,x+unit*.22f,bottom,dp(7),dp(7),paint);paint.setShader(null);paint.setColor(muted);paint.setTextAlign(Paint.Align.CENTER);canvas.drawText(Language.ui(e.getValue().toString()),x,y-dp(5),paint);String k=e.getKey();if(k.matches("\\d{4}-\\d{2}"))k=k.substring(5);canvas.drawText(Language.ui(k),x,bottom+dp(20),paint);if(i==0)trend.moveTo(x,y);else trend.lineTo(x,y);}if(line){paint.setColor(berry);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(2));canvas.drawPath(trend,paint);paint.setStyle(Paint.Style.FILL);}paint.setTextAlign(Paint.Align.LEFT);}
        }
    }
    static class MonthlyChart extends ChartCanvas {
        final LinkedHashMap<String,double[]> data;final Paint p=new Paint(3);
        MonthlyChart(Context c,LinkedHashMap<String,double[]> data){super(c);this.data=data;setContentDescription("当前范围与去年同月实际记录比较图");}
        protected void onDraw(Canvas c){float d=getResources().getDisplayMetrics().density,bottom=getHeight()-30*d,top=24*d,unit=(float)getWidth()/data.size();p.setColor(Ui.BORDER);p.setStrokeWidth(d);for(int grid=0;grid<4;grid++){float y=bottom-(bottom-top)*grid/3f;c.drawLine(0,y,getWidth(),y,p);}double max=1;for(double[] v:data.values())for(double n:v)if(Double.isFinite(n))max=Math.max(max,n);max*=1.15;Path line=new Path();boolean begun=false;int i=0;p.setTextSize(10*d);p.setTextAlign(Paint.Align.CENTER);for(Map.Entry<String,double[]> entry:data.entrySet()){float x=unit*(i+++.5f);double[] values=entry.getValue();for(int series=0;series<2;series++){double n=values[series];float bx=x+(series==0?-unit*.18f:unit*.18f);p.setColor(series==0?chartTone(3):chartTone(1));if(Double.isFinite(n)){float y=bottom-(float)(n/max)*(bottom-top);p.setShader(new LinearGradient(bx,y,bx,bottom,series==0?chartTone(3):chartTone(1),Ui.blend(series==0?chartTone(3):chartTone(1),Ui.SURFACE),Shader.TileMode.CLAMP));c.drawRoundRect(bx-unit*.12f,y,bx+unit*.12f,bottom,6*d,6*d,p);p.setShader(null);p.setColor(Ui.MUTED);c.drawText(Language.ui(n==Math.rint(n)?String.valueOf((int)n):String.format(Locale.CHINA,"%.1f",n)),bx,y-5*d,p);if(series==0){if(!begun){line.moveTo(bx,y);begun=true;}else line.lineTo(bx,y);}}else {p.setColor(Ui.MUTED);c.drawText(Language.ui("—"),bx,bottom-6*d,p);if(series==0)begun=false;}}p.setColor(Ui.MUTED);c.drawText(Language.ui(entry.getKey().matches("\\d{4}-\\d{2}")?entry.getKey().substring(2):entry.getKey()),x,bottom+20*d,p);}p.setStyle(Paint.Style.STROKE);p.setColor(chartTone(3));p.setStrokeWidth(2*d);c.drawPath(line,p);p.setStyle(Paint.Style.FILL);}
    }
    static class Heatmap extends ChartCanvas {
        final Map<String,Integer> counts;final int year;final LocalDate start,end;final Paint p=new Paint(3);
        Heatmap(Context c,Map<String,Integer> counts,int year,LocalDate start,LocalDate end){super(c);this.counts=counts;this.year=year;this.start=start;this.end=end;setContentDescription(start+" 至 "+end+" 十二个月每日实际记录次数热力图；周一为每行起点，未来不统计");}
        int tint(float t){return Color.rgb(Math.round(Color.red(Ui.BORDER)+(Color.red(Ui.BERRY)-Color.red(Ui.BORDER))*t),Math.round(Color.green(Ui.BORDER)+(Color.green(Ui.BERRY)-Color.green(Ui.BORDER))*t),Math.round(Color.blue(Ui.BORDER)+(Color.blue(Ui.BERRY)-Color.blue(Ui.BORDER))*t));}
        protected void onDraw(Canvas c){
            float d=getResources().getDisplayMetrics().density,gap=12*d,block=(getWidth()-gap*2)/3f,cell=block/7f;LocalDate today=LocalDate.now();p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
            for(int m=1;m<=12;m++){
                float left=((m-1)%3)*(block+gap),top=((m-1)/3)*105*d;YearMonth month=YearMonth.of(year,m);int offset=month.atDay(1).getDayOfWeek().getValue()-1;
                p.setTextAlign(Paint.Align.LEFT);p.setTextSize(11*d);p.setColor(Ui.INK);c.drawText(Language.ui(m+"月"),left,top+13*d,p);
                p.setTextAlign(Paint.Align.CENTER);p.setTextSize(6*d);p.setColor(Ui.MUTED);String[] labels={"一","二","三","四","五","六","日"};for(int col=0;col<7;col++)c.drawText(Language.ui(labels[col]),left+cell*(col+.5f),top+25*d,p);
                for(int day=1;day<=month.lengthOfMonth();day++){
                    LocalDate date=month.atDay(day);int index=offset+day-1,n=counts.getOrDefault(date.toString(),0);float x=left+(index%7)*cell,y=top+30*d+(index/7)*10*d;
                    boolean outside=date.isBefore(start)||date.isAfter(end),future=date.isAfter(today);
                    p.setStyle(Paint.Style.FILL);p.setColor(outside||future?Ui.SURFACE:n==0?Ui.BORDER:tint(n==1?.35f:n==2?.65f:1f));c.drawRoundRect(x+d,y,x+cell-d,y+8*d,1.5f*d,1.5f*d,p);
                    if(outside||future){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.65f*d);p.setColor(future?Ui.MUTED:Ui.BORDER);c.drawRoundRect(x+d,y,x+cell-d,y+8*d,1.5f*d,1.5f*d,p);p.setStyle(Paint.Style.FILL);}
                }
            }
            String[] labels={"未记录","1次","2次","3次+","未来"};float unit=getWidth()/5f,y=436*d;
            for(int i=0;i<5;i++){float x=i*unit;p.setStyle(i==4?Paint.Style.STROKE:Paint.Style.FILL);p.setStrokeWidth(d);p.setColor(i==4?Ui.MUTED:i==0?Ui.BORDER:tint(i==1?.35f:i==2?.65f:1f));c.drawRoundRect(x,y,x+8*d,y+8*d,2*d,2*d,p);p.setStyle(Paint.Style.FILL);p.setColor(Ui.MUTED);p.setTextAlign(Paint.Align.LEFT);p.setTextSize(9*d);c.drawText(Language.ui(labels[i]),x+11*d,y+8*d,p);}
        }
    }
    static class Trend extends ChartCanvas {
        final TreeMap<String,double[]> values;final Paint p=new Paint(3);final String firstLabel,secondLabel;
        Trend(Context c,TreeMap<String,double[]> values){this(c,values,"舒适度","疲劳");}
        Trend(Context c,TreeMap<String,double[]> values,String firstLabel,String secondLabel){super(c);this.values=values;this.firstLabel=firstLabel;this.secondLabel=secondLabel;setContentDescription("月度已知样本"+firstLabel+(secondLabel==null?"":"与"+secondLabel)+"均值趋势");}
        protected void onDraw(Canvas c){float density=getResources().getDisplayMetrics().density,w=getWidth(),h=getHeight()-30*density,unit=w/Math.max(1,values.size());p.setColor(Ui.BORDER);p.setStrokeWidth(density);for(int grid=0;grid<=5;grid++){float gy=h-grid/5f*(h-20*density);c.drawLine(0,gy,w,gy,p);}p.setTextSize(11*density);int i=0;for(Map.Entry<String,double[]> entry:values.entrySet()){float x=unit*(i+++.5f);double[] v=entry.getValue();p.setColor(Ui.MUTED);p.setTextAlign(Paint.Align.CENTER);c.drawText(Language.ui(entry.getKey().substring(2)),x,h+20*density,p);}for(int series=0;series<(secondLabel==null?1:2);series++){Path path=new Path();boolean started=false;i=0;p.setColor(series==0?chartTone(3):chartTone(1));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2*density);for(double[] v:values.values()){float x=unit*(i+++.5f);int index=series*2;if(v[index+1]==0){started=false;continue;}float y=h-(float)(v[index]/v[index+1])/5*(h-20*density);if(!started){path.moveTo(x,y);started=true;}else path.lineTo(x,y);c.drawCircle(x,y,3*density,p);}c.drawPath(path,p);p.setStyle(Paint.Style.FILL);p.setColor(Ui.MUTED);c.drawText(Language.ui(series==0?firstLabel:secondLabel),w-(series==0?90:28)*density,12*density,p);}}
    }
}
