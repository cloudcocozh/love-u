package app.loveu;
import java.util.*;
public class CoreTest {
 static void check(boolean b){if(!b)throw new AssertionError();}
 static void rejects(String s){try{Domain.parseCsv(s);throw new AssertionError();}catch(IllegalArgumentException expected){}}
 public static void main(String[] args){
  List<Domain.Event> e=Domain.parseCsv("\ufeff日期,次数\r\n2024-02-29,2\r\n2024-03-01,3\r\n");
  check(e.size()==5);check(Domain.stats(e).days==2);check(Domain.mergeImported(e,Domain.parseCsv("日期,次数\n2024-02-29,2\n2024-03-01,3")).size()==5);
  rejects("日期,次数\n2023-02-29,1"); rejects("日期,次数\n2024-01-01,-1"); rejects("日期,次数\n2024-01-01,1.5");
  check(Domain.stats(e).knownDurations==0);check(Domain.stats(e).meanDuration==null);
  e.get(0).durationMillis=1000L;e.get(1).durationMillis=3000L;check(Domain.stats(e).meanDuration==2000.0);check(Domain.stats(e).medianDuration==2000.0);
  Domain.Timer t=new Domain.Timer();t.start(10000,100,7);t.pause(1100);check(t.duration(2100)==1000);t.resume(3100);check(t.duration(4100)==2000);check(t.rebooted(8));check(!t.rebooted(7));check(Domain.Timer.fromMap(t.toMap()).duration(4100)==2000);
  List<Domain.Event> r=Domain.parseCsv("id,date,time,durationMillis,place,latitude,longitude,method,partner,note,source,rating\nx,2024-02-29,,1000,\"a,b\",,,,,,csv,\n");check(r.size()==1);check(r.get(0).place.equals("a,b"));check(r.get(0).time==null);
  String noIds="date,method,partner\n2024-01-01,\"a, b\",c\n2024-01-01,a,\"b, c\"\n2024-01-01,a,\"b, c\"\n"; List<Domain.Event> distinct=Domain.parseCsv(noIds);check(!distinct.get(0).id.equals(distinct.get(1).id));check(!distinct.get(1).id.equals(distinct.get(2).id));check(Domain.mergeImported(distinct,Domain.parseCsv(noIds)).size()==3);
  Domain.Event method=Domain.parseCsv("date,method\n2024-01-01,x\n").get(0),partner=Domain.parseCsv("date,partner\n2024-01-01,x\n").get(0);check(!method.id.equals(partner.id));check(method.id.equals(Domain.parseCsv("method,date\nx,2024-01-01\n").get(0).id));
  check(method.id.equals("csv:"+java.util.UUID.nameUUIDFromBytes("4:date10:2024-01-014:time-1:14:durationMillis-1:5:place-1:8:latitude-1:9:longitude-1:6:method1:x7:partner-1:4:note-1:6:source-1:6:rating-1:".getBytes(java.nio.charset.StandardCharsets.UTF_8))+":0"));
  rejects("date,detailsPending\n2024-01-01,yes\n");rejects("date,locationAccuracy\n2024-01-01,-1\n");rejects("date,outcomes\n2024-01-01,没射精|其他\n");rejects("date,selfOrgasm,selfOrgasmCount\n2024-01-01,true,0\n");
  Domain.Event unknown=Domain.parseCsv("date\n2024-01-01\n").get(0);check(unknown.partnerOrgasm==null&&unknown.partnerOrgasmCount==null&&!unknown.detailsPending&&unknown.positions.isEmpty());
  check(Domain.parseCsv("date,selfOrgasmCount\n2024-01-01,2\n").get(0).selfOrgasm);check(Domain.parseCsv("date,selfOrgasm\n2024-01-01,false\n").get(0).selfOrgasmCount==0);
  System.out.println("CoreTest passed");
 }
}



