package app.loveu;
import android.app.job.*;import android.content.*;import android.os.*;import java.util.concurrent.*;
/** OS-scheduled fallback; timing is deliberately controlled by Android. */
public final class CoupleBackgroundJob extends JobService {
 static final int JOB_ID=2301;private final Handler handler=new Handler(Looper.getMainLooper());private ExecutorService worker;private Future<?> task;private CoupleApi api;private JobParameters current;
 public static void schedule(Context c){JobScheduler s=c.getSystemService(JobScheduler.class);if(s==null)return;if(!CoupleBackgroundSync.enabled(c)){s.cancel(JOB_ID);return;}if(s.getPendingJob(JOB_ID)==null)s.schedule(new JobInfo.Builder(JOB_ID,new ComponentName(c,CoupleBackgroundJob.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true).setPeriodic(15*60*1000L).build());}
 public static void cancel(Context c){JobScheduler s=c.getSystemService(JobScheduler.class);if(s!=null)s.cancel(JOB_ID);}
 public boolean onStartJob(JobParameters p){if(!CoupleBackgroundSync.enabled(this))return false;current=p;api=CoupleBackgroundService.createApi(this);worker=Executors.newSingleThreadExecutor();handler.postDelayed(deadline,20000);task=worker.submit(()->{int count=0;try{count=CoupleBackgroundSync.run(this,api);}catch(Exception ignored){}final int result=count;handler.post(()->{if(current!=p)return;CoupleBackgroundService.received(this,result);complete();});});return true;}
 private final Runnable deadline=()->complete();
 private void cancelWork(){handler.removeCallbacks(deadline);if(api!=null)api.cancel();if(task!=null)task.cancel(true);if(worker!=null)worker.shutdownNow();}
 private void complete(){CoupleBackgroundService.received(this,0);JobParameters p=current;current=null;cancelWork();if(p!=null)jobFinished(p,false);}
 public boolean onStopJob(JobParameters p){current=null;cancelWork();return CoupleBackgroundSync.enabled(this);}
 public void onDestroy(){current=null;cancelWork();super.onDestroy();}
}
