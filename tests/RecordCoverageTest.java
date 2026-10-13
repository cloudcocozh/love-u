package app.loveu;
import java.util.*;
public class RecordCoverageTest {
 static void check(boolean value){if(!value)throw new AssertionError();}
 public static void main(String[] args){
  Domain.Event unknown=new Domain.Event();unknown.place=" \t ";unknown.protection="";
  Domain.Event known=new Domain.Event();known.time="00:00";known.durationMillis=0L;known.place="家";known.protection="无措施";known.selfOrgasm=false;known.partnerOrgasm=true;
  List<Domain.Event> input=Arrays.asList(unknown,known);Map<String,Integer> c=RecordCoverage.count(input);
  check(c.size()==6);for(int value:c.values())check(value==1);
  check(known.selfOrgasm==Boolean.FALSE&&known.durationMillis==0L&&unknown.partnerOrgasm==null&&unknown.place.equals(" \t "));
  for(int value:RecordCoverage.count(Collections.emptyList()).values())check(value==0);
  Map<String,Integer> only=RecordCoverage.count(Collections.singletonList(unknown));for(int value:only.values())check(value==0);
  try{c.put("地点",99);throw new AssertionError();}catch(UnsupportedOperationException expected){}
  System.out.println("RecordCoverageTest passed");
 }
}
