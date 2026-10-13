package app.loveu;
import java.util.*;
public class RecordSearchTest {
 static void check(boolean value){if(!value)throw new AssertionError();}
 public static void main(String[] args){
  Domain.Event a=new Domain.Event();a.date="2020-09-01";a.time="21:30";a.place="家";a.protection="安全套";a.note="Quiet evening";a.positions.add("侧卧");
  Domain.Event b=new Domain.Event();b.date="2026-01-01";b.detailsPending=true;
  List<Domain.Event> original=Arrays.asList(a,b);
  check(RecordSearch.find(original,"2020 安全套",false).get(0)==a);
  check(RecordSearch.find(original,"QUIET 侧卧",false).size()==1);
  check(RecordSearch.find(original,"安全套",true).isEmpty());
  check(RecordSearch.find(original,null,true).get(0)==b);
  check(RecordSearch.find(original,"   ",false).get(0)==b);
  check(RecordSearch.find(original,"不存在",false).isEmpty());
  check(original.get(0)==a&&a.time.equals("21:30")&&b.protection==null);
  System.out.println("RecordSearchTest passed");
 }
}
