package app.loveu;
import java.util.*;import java.time.*;
public class AnalyticsTest {
 static void check(boolean b){if(!b)throw new AssertionError();}
 static Domain.Event e(String date){Domain.Event e=new Domain.Event();e.date=date;return e;}
 public static void main(String[] args){
  Domain.Event a=e("2024-01-01"),b=e("2024-01-01"),c=e("2024-01-08");a.durationMillis=300000L;a.time="14:30";a.partnerOrgasm=true;a.partnerOrgasmCount=2;a.positions.add("a|b");a.positions.add("c\\d");a.selfCondition.comfort=4;b.partnerOrgasm=false;b.partnerOrgasmCount=0;
  List<Domain.Event> events=Arrays.asList(a,b,c);Analytics.Summary s=Analytics.analyze(events,null,null);check(s.base.events==3&&s.base.days==2);check(s.partnerKnown==2&&s.partnerOrgasmRate==50.0&&s.selfOrgasmRate==null);check(s.knownTimes==1&&s.hours[14]==1&&s.durationBins[1]==1&&s.intervalBins[2]==1);check(s.selfComfortMean==4.0);check(Analytics.compare(events,LocalDate.parse("2024-01-01"),LocalDate.parse("2024-01-08"),LocalDate.parse("2023-01-01"),LocalDate.parse("2023-01-08")).percent==null);
  Domain.Event r=Domain.parseCsv(Domain.toCsv(events)).get(0);check(r.positions.equals(a.positions)&&r.partnerOrgasmCount==2&&r.selfCondition.comfort==4);check(!r.detailsPending);
  try{Domain.parseCsv("date,partnerOrgasm,partnerOrgasmCount\n2024-01-01,false,2\n");throw new AssertionError();}catch(IllegalArgumentException expected){}
  try{Domain.parseCsv("date,selfEnergy\n2024-01-01,0\n");throw new AssertionError();}catch(IllegalArgumentException expected){}
  check(!Domain.parseCsv("date,protection\n2024-01-01,a\n").get(0).id.equals(Domain.parseCsv("date,protection\n2024-01-01,b\n").get(0).id));
  check(Analytics.analyze(events,LocalDate.parse("2024-01-02"),LocalDate.parse("2024-01-08")).base.events==1);
  List<Domain.Event> bins=new ArrayList<>();String[] dates={"2024-01-01","2024-01-02","2024-01-04","2024-01-11","2024-01-25","2024-02-24"};long[] lengths={0,299999,300000,900000,1800000,3600000};for(int i=0;i<dates.length;i++){Domain.Event v=e(dates[i]);v.durationMillis=lengths[i];bins.add(v);}Analytics.Summary bs=Analytics.analyze(bins,null,null);check(Arrays.equals(bs.durationBins,new int[]{2,1,1,1,1}));check(Arrays.equals(bs.intervalBins,new int[]{1,1,1,1,1}));
  Analytics.Comparison comparison=Analytics.compare(events,LocalDate.parse("2024-01-08"),LocalDate.parse("2024-01-08"),LocalDate.parse("2024-01-01"),LocalDate.parse("2024-01-01"));check(comparison.current==1&&comparison.previous==2&&comparison.delta==-1&&comparison.percent==-50.0);
  Domain.Event full=e("2024-12-31");full.detailsPending=true;full.locationAccuracy=12.5;full.locationCapturedAt=123L;full.protection="x";full.vocalLevel="安静";full.outcomes.add("其他");full.partnerCondition.tags.add("a,b\\c|d");full.partnerCondition.energy=1;full.partnerCondition.fatigue=2;full.partnerCondition.mood=3;full.partnerCondition.comfort=5;Domain.Event rt=Domain.parseCsv(Domain.toCsv(Arrays.asList(full))).get(0);check(rt.id.equals(full.id)&&rt.detailsPending&&rt.locationAccuracy==12.5&&rt.locationCapturedAt==123L&&rt.partnerCondition.tags.equals(full.partnerCondition.tags)&&rt.partnerCondition.energy==1&&rt.partnerCondition.comfort==5);
  Domain.Event comfort=e("2024-03-01"),other=e("2024-03-02"),unknownCondition=e("2024-03-03");comfort.selfCondition.comfort=2;comfort.partnerCondition.comfort=5;other.selfCondition.energy=4;other.partnerCondition.tags.add("fatigue");Analytics.Summary comfortSummary=Analytics.analyze(Arrays.asList(comfort,other,unknownCondition),null,null);check(comfortSummary.selfConditionKnown==2&&comfortSummary.partnerConditionKnown==2);check(comfortSummary.selfComfortKnown==1&&comfortSummary.partnerComfortKnown==1);check(comfortSummary.selfComfortMean==2.0&&comfortSummary.partnerComfortMean==5.0);Analytics.Summary noComfort=Analytics.analyze(Arrays.asList(other,unknownCondition),null,null);check(noComfort.selfComfortKnown==0&&noComfort.partnerComfortKnown==0&&noComfort.selfComfortMean==null&&noComfort.partnerComfortMean==null);
  List<Domain.Event> history=Arrays.asList(e("2023-01-04"),e("2023-03-04"),e("2024-03-04"),e("2024-03-05"),e("2024-05-01"));
  Analytics.MonthExtrema extrema=Analytics.monthExtrema(history,null,LocalDate.of(2024,5,10));check(extrema.minimum==0&&extrema.maximum==2&&extrema.highest.equals(Arrays.asList("2024-03"))&&extrema.lowest.contains("2023-02")&&!extrema.months.containsKey("2024-05"));
  Analytics.MonthExtrema scopedExtrema=Analytics.monthExtrema(history,2023,LocalDate.of(2024,5,10));check(scopedExtrema.highest.equals(Arrays.asList("2023-01","2023-03"))&&scopedExtrema.months.size()==12);
  check(Analytics.monthExtrema(Collections.emptyList(),null,LocalDate.of(2024,5,10)).minimum==null);
  Analytics.YearComparison aligned=Analytics.compareYears(history,2023,2024,true,LocalDate.of(2024,3,4));check(aligned.firstTotal==2&&aligned.secondTotal==1&&aligned.firstMonths[2]==1&&aligned.secondMonths[2]==1&&aligned.secondMonths[4]==0);
  Analytics.YearComparison fullYears=Analytics.compareYears(history,2023,2024,false,LocalDate.of(2025,3,4));check(fullYears.secondTotal==3);
  Analytics.YearComparison leap=Analytics.compareYears(Arrays.asList(e("2023-02-28"),e("2023-03-01"),e("2024-02-29")),2023,2024,true,LocalDate.of(2024,2,29));check(leap.firstTotal==1&&leap.secondTotal==1);
  check(Analytics.monthExtrema(Arrays.asList(e("2024-05-01"),e("2025-01-01")),null,LocalDate.of(2024,5,10)).minimum==null);
  Analytics.MonthExtrema zeros=Analytics.monthExtrema(Arrays.asList(e("2024-01-01"),e("2024-04-01")),2024,LocalDate.of(2024,5,10));check(zeros.lowest.equals(Arrays.asList("2024-02","2024-03"))&&zeros.highest.equals(Arrays.asList("2024-01","2024-04")));
  try{Analytics.analyze(events,LocalDate.of(2024,2,1),LocalDate.of(2024,1,1));throw new AssertionError();}catch(IllegalArgumentException expected){}
  check(Analytics.compareYears(history,2023,2024,false,LocalDate.of(2024,3,4)).secondTotal==1);
    check(bs.shortestDurationMillis==0L&&bs.longestDurationMillis==3600000L&&bs.totalDurationMillis==6899999L);
  Analytics.Summary unknownDuration=Analytics.analyze(Arrays.asList(e("2024-01-01")),null,null);check(unknownDuration.shortestDurationMillis==null&&unknownDuration.longestDurationMillis==null&&unknownDuration.totalDurationMillis==null);
  Analytics.Summary filteredDuration=Analytics.analyze(bins,LocalDate.parse("2024-01-02"),LocalDate.parse("2024-01-02"));check(filteredDuration.shortestDurationMillis==299999L&&filteredDuration.longestDurationMillis==299999L&&filteredDuration.totalDurationMillis==299999L);
  System.out.println("AnalyticsTest passed");
 }
}
