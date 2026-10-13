package app.loveu;
import java.util.*;
/** Coverage of supplied records only; explicit false and zero are known values. */
public final class RecordCoverage {
 private RecordCoverage(){}
 private static boolean filled(String value){return value!=null&&!value.trim().isEmpty();}
 public static Map<String,Integer> count(List<Domain.Event> events){
  LinkedHashMap<String,Integer> counts=new LinkedHashMap<>();
  for(String label:new String[]{"具体时间","时长","地点","避孕措施","本人高潮","对方高潮"})counts.put(label,0);
  for(Domain.Event e:events){
   boolean[] values={filled(e.time),e.durationMillis!=null,filled(e.place),filled(e.protection),e.selfOrgasm!=null,e.partnerOrgasm!=null};
   int i=0;for(String label:counts.keySet()){if(values[i++])counts.put(label,counts.get(label)+1);}
  }
  return Collections.unmodifiableMap(counts);
 }
}
