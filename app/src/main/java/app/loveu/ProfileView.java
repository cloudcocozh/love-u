package app.loveu;

import android.widget.*;
import java.util.*;

/** Optional local preferences; changing these never rewrites past records. */
final class ProfileView extends LinearLayout {
    final MainActivity activity;
    String selfGender,partnerGender;
    ProfileView(MainActivity activity) {
        super(activity);this.activity=activity;setOrientation(VERTICAL);
        selfGender=activity.settings().optString("profileGender","不设置");
        partnerGender=activity.settings().optString("partnerGender","不设置");
        addView(activity.text("个人偏好",20,activity.ink));

        gender("我的性别",selfGender,true);
        gender("对方的性别",partnerGender,false);
        addView(activity.button("保存个人偏好",()->activity.attempt(()->{
            if(activity.locked)return;
            activity.settings().put("profileGender",selfGender).put("partnerGender",partnerGender);
            activity.vault.write(activity.root);
            Toast.makeText(activity,Language.ui("个人偏好已保存"),Toast.LENGTH_SHORT).show();activity.show();
        })));
    }
    void gender(String title,String selected,boolean self) {
        addView(activity.text(title,15,activity.ink));
        List<Button> buttons=new ArrayList<>();LinearLayout row=new LinearLayout(activity);addView(row);
        for(String value:new String[]{"男","女","自定义","不设置"}) {
            Button button=activity.button(value,()->{});buttons.add(button);
            button.setTextSize(13);button.setPadding(0,0,0,0);button.setMinWidth(0);button.setMinimumWidth(0);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,activity.dp(48),1);params.setMargins(activity.dp(2),activity.dp(6),activity.dp(2),activity.dp(6));row.addView(button,params);
            style(button,value.equals(selected));
            button.setOnClickListener(v->{if(self)selfGender=value;else partnerGender=value;for(Button b:buttons)style(b,Language.source(b).equals(value));});
        }
    }
    void style(Button button,boolean selected) {
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(selected?activity.berry:Ui.PINK);bg.setCornerRadius(activity.dp(12));button.setBackground(bg);button.setTextColor(selected?Ui.ON_ACCENT:activity.ink);button.setContentDescription(Language.ui(Language.source(button)+(selected?" 已选择":" 未选择")));
    }
}
