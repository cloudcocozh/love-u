package app.loveu;
import android.content.*;
public final class CoupleBackgroundReceiver extends BroadcastReceiver {
 public void onReceive(Context c,Intent i){if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())||Intent.ACTION_MY_PACKAGE_REPLACED.equals(i.getAction()))CoupleBackgroundJob.schedule(c);}
}
