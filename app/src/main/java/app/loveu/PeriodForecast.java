package app.loveu;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Descriptive estimates from logged starts, never a diagnosis or contraceptive model. */
final class PeriodForecast {
 static final class Result {
  int records,completed,excluded,samples; double averageLength,medianCycle;
  LocalDate latest,ongoing,expected,earliest,latestExpected;
  final TreeMap<String,Integer> cycles=new TreeMap<>(),lengths=new TreeMap<>();
  String countdown(LocalDate today){if(expected==null)return "再记录几个周期后显示预测";long days=ChronoUnit.DAYS.between(today,expected);return days>0?"预计还有 "+days+" 天":days==0?"预计今天开始":"已超过预计日期 "+(-days)+" 天";}
 }
 static Result analyze(List<PeriodAnalysis.Entry> entries,LocalDate today){
  Result r=new Result();TreeMap<LocalDate,LocalDate> sorted=new TreeMap<>();
  for(PeriodAnalysis.Entry e:entries)try{LocalDate start=LocalDate.parse(e.start),end=e.end==null?null:LocalDate.parse(e.end);if(start.isAfter(today)||(end!=null&&(end.isBefore(start)||end.isAfter(today)))||sorted.containsKey(start)){r.excluded++;continue;}sorted.put(start,end);}catch(RuntimeException invalid){r.excluded++;}
  LocalDate previous=null,previousEnd=null;List<Long> gaps=new ArrayList<>();long sumLength=0;
  for(Map.Entry<LocalDate,LocalDate> row:sorted.entrySet()){
   LocalDate start=row.getKey(),end=row.getValue();if(previous!=null&&(previousEnd==null||!start.isAfter(previousEnd))){r.excluded++;continue;}
   r.records++;r.latest=start;if(end==null)r.ongoing=start;else{int length=(int)ChronoUnit.DAYS.between(start,end)+1;r.lengths.put(start.toString(),length);sumLength+=length;r.completed++;}
   if(previous!=null){long gap=ChronoUnit.DAYS.between(previous,start);gaps.add(gap);r.cycles.put(start.toString(),(int)gap);}previous=start;previousEnd=end;
  }
  if(r.completed>0)r.averageLength=(double)sumLength/r.completed;
  List<Long> recent=new ArrayList<>(gaps.subList(Math.max(0,gaps.size()-6),gaps.size()));Collections.sort(recent);r.samples=recent.size();
  if(!recent.isEmpty()){int n=recent.size();r.medianCycle=n%2==1?recent.get(n/2):(recent.get(n/2-1)+recent.get(n/2))/2.0;}
  if(recent.size()>=2){r.expected=r.latest.plusDays(Math.round(r.medianCycle));r.earliest=r.latest.plusDays(recent.get(0));r.latestExpected=r.latest.plusDays(recent.get(recent.size()-1));}
  return r;
 }
}
