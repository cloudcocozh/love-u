package app.loveu;
import java.util.*;
/** Read-only local search; terms must all match recorded textual fields. */
public final class RecordSearch {
 private RecordSearch(){}
 public static List<Domain.Event> find(List<Domain.Event> events,String query,boolean pendingOnly){
  String cleaned=query==null?"":query.trim().toLowerCase(Locale.ROOT);
  String[] terms=cleaned.isEmpty()?new String[0]:cleaned.split("\\s+");
  List<Domain.Event> matches=new ArrayList<>();
  for(Domain.Event e:events){
   if(pendingOnly&&!e.detailsPending)continue;
   StringBuilder text=new StringBuilder();
   for(String value:new String[]{e.date,e.time,e.place,e.partner,e.note,e.protection,e.method,e.source,e.vocalLevel})if(value!=null)text.append(value).append(' ');
   if(e.positions!=null)for(String value:e.positions)text.append(value).append(' ');
   if(e.outcomes!=null)for(String value:e.outcomes)text.append(value).append(' ');
   String haystack=text.toString().toLowerCase(Locale.ROOT);boolean found=true;
   for(String term:terms)if(!haystack.contains(term)){found=false;break;}
   if(found)matches.add(e);
  }
  matches.sort(Comparator.comparing((Domain.Event e)->e.date==null?"":e.date).thenComparing(e->e.time==null?"":e.time).reversed());
  return matches;
 }
}
