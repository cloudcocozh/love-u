package app.loveu;
import android.app.Application;import android.content.Context;
public final class LoveUApplication extends Application {
 protected void attachBaseContext(Context base){super.attachBaseContext(Language.wrap(base));}
}
