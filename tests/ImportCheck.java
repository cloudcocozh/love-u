import app.loveu.Domain;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
/** Read-only CSV summary; no historical fixture or expected private counts. */
public class ImportCheck { public static void main(String[] a)throws Exception { if(a.length!=1)throw new IllegalArgumentException("Usage: ImportCheck <csv-path>"); Domain.Stats s=Domain.stats(Domain.parseCsv(new String(Files.readAllBytes(Paths.get(a[0])),StandardCharsets.UTF_8))); System.out.println("Events: "+s.events+", days: "+s.days+", known durations: "+s.knownDurations); } }
