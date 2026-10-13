package app.loveu;
import java.time.*;
public class SessionTimeTest {
 static void check(boolean value){if(!value)throw new AssertionError();}
 public static void main(String[] args){
  long instant=Instant.parse("2026-10-09T16:30:00Z").toEpochMilli();
  ZonedDateTime captured=SessionTime.start(instant,"Asia/Shanghai",ZoneId.of("America/Los_Angeles"));
  check(captured.toLocalDate().toString().equals("2026-10-10"));check(captured.toLocalTime().toString().equals("00:30"));
  check(SessionTime.start(instant,null,ZoneId.of("UTC")).toLocalTime().toString().equals("16:30"));
  check(SessionTime.start(instant,"",ZoneId.of("UTC")).toInstant().toEpochMilli()==instant);
  long dst=Instant.parse("2026-11-01T06:30:00Z").toEpochMilli();check(SessionTime.start(dst,"America/New_York",ZoneId.of("UTC")).getOffset().getTotalSeconds()==-18000);
  try{SessionTime.start(instant,"Invalid/Zone",ZoneId.of("UTC"));throw new AssertionError();}catch(DateTimeException expected){}
  System.out.println("SessionTimeTest passed");
 }
}
