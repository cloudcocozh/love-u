package app.loveu;
import java.time.*;import java.time.temporal.ChronoUnit;import java.util.*;
final class PeriodAnalysis {
 static final class Entry {String start,end;Entry(String s,String e){start=s;end=e;}}
 static void validate(Entry candidate,List<Entry> others,LocalDate today){LocalDate s=LocalDate.parse(candidate.start),e=candidate.end==null?today:LocalDate.parse(candidate.end);if(s.isAfter(today)||e.isAfter(today)||e.isBefore(s))throw new IllegalArgumentException("日期需为今天或过去，结束不能早于开始");for(Entry item:others){LocalDate a=LocalDate.parse(item.start),b=item.end==null?today:LocalDate.parse(item.end);if(!s.isAfter(b)&&!e.isBefore(a))throw new IllegalArgumentException("与已有经期重叠，请编辑原记录");}}
 static String summary(List<Entry> source,LocalDate today){PeriodForecast.Result r=PeriodForecast.analyze(source,today);String result="已记录 "+r.records+" 段经期";if(r.completed>0)result+="\n平均经期 "+String.format(Locale.CHINA,"%.1f",r.averageLength)+" 天";if(r.samples>0)result+="\n周期中位数 "+String.format(Locale.CHINA,"%.1f",r.medianCycle)+" 天";if(r.expected!=null)result+="\n下次预计 "+r.earliest+" ～ "+r.latestExpected;else result+="\n至少三段记录后显示预计范围";return result;}
}
