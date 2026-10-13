package app.loveu;
import java.time.*;
/** Resolves the recorded start instant using its captured zone, with legacy fallback. */
public final class SessionTime {
 private SessionTime(){}
 public static ZonedDateTime start(long epochMillis,String capturedZone,ZoneId legacyZone){
  ZoneId zone=legacyZone;
  if(capturedZone!=null&&!capturedZone.trim().isEmpty())zone=ZoneId.of(capturedZone);
  return Instant.ofEpochMilli(epochMillis).atZone(zone);
 }
}
