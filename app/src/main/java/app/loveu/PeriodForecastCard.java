package app.loveu;

import android.app.AlertDialog;
import android.widget.*;
import android.graphics.Typeface;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

final class PeriodForecastCard {
 static LinearLayout create(MainActivity a,boolean partner){
  PeriodForecast.Result r=PeriodForecast.analyze(PeriodRecords.entries(a.root,partner),LocalDate.now());LinearLayout card=Ui.card(a);card.setTag("period-forecast");
  TextView title=a.text("下次经期预测",17,Ui.INK);title.setTypeface(null,Typeface.BOLD);card.addView(title);
  TextView countdown=a.text(r.countdown(LocalDate.now()),23,Ui.BERRY);countdown.setTag("period-countdown");card.addView(countdown);
  if(r.expected!=null){card.addView(a.text("预计 "+Language.shortDate(r.expected),17,Ui.INK));card.addView(a.text("估计范围 "+r.earliest+" ～ "+r.latestExpected,12,Ui.MUTED));}
  else card.addView(a.text("至少需要 3 段不同的经期记录",12,Ui.MUTED));
  if(r.samples>0)card.addView(a.text(String.format(Locale.CHINA,"周期约 %.1f 天 · 依据最近 %d 个间隔",r.medianCycle,r.samples),12,Ui.MUTED));
  Button details=a.button("ⓘ 预测依据",()->{AlertDialog d=new LanguageDialog(a).setTitle("预测依据").setMessage("依据本机及已接收的同一人的经期记录；同一开始日期只计一次，本机记录优先。保留全部历史，预测采用最近最多6个开始日期间隔的中位数，范围取这些间隔的最短至最长。\n\n这是日期估计，不是确定时间，也不能用于避孕或诊断。超过预计日但没有新记录时，不会自动假设新经期已经发生。妊娠、哺乳、用药等可能影响周期。记录不足、重叠或无效日期不用于预测。").setPositiveButton("知道了",null).create();a.dialogs.add(d);d.show();if(a.workbuddy())WorkbuddyUi.decorate(d);});card.addView(details);return card;
 }
}
