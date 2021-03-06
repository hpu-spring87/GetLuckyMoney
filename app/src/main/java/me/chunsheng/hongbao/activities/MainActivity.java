package me.chunsheng.hongbao.activities;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import me.chunsheng.hongbao.R;
import me.chunsheng.hongbao.utils.UpdateTask;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

//http://www.easyicon.net/1088260-red_envelope_icon.html
public class MainActivity extends Activity {
    private final Intent mAccessibleIntent =
            new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

    private Button switchPlugin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switchPlugin = (Button) findViewById(R.id.button_accessible);
        //me.chunsheng.hongbao.utils.ZoomImageView test = (me.chunsheng.hongbao.utils.ZoomImageView) findViewById(R.id.test);
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.ic_launcher);
//        test.setImage(bitmap);
//        test.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MainActivity.this,"点击了我呀",Toast.LENGTH_SHORT).show();
//            }
//        });
        handleMIUIStatusBar();
        updateServiceStatus();

        explicitlyLoadPreferences();
    }

    private void explicitlyLoadPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    /**
     * 适配MIUI沉浸状态栏
     */
    private void handleMIUIStatusBar() {
        Window window = getWindow();

        Class clazz = window.getClass();
        try {
            int tranceFlag = 0;
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");

            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_TRANSPARENT");
            tranceFlag = field.getInt(layoutParams);

            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(window, tranceFlag, tranceFlag);

            ImageView placeholder = (ImageView) findViewById(R.id.main_actiob_bar_placeholder);
            int placeholderHeight = getStatusBarHeight();
            placeholder.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, placeholderHeight));
        } catch (Exception e) {
            // Do nothing
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();

        // Check for update
        new UpdateTask(this, false).update();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void updateServiceStatus() {
        boolean serviceEnabled = false;

        AccessibilityManager accessibilityManager =
                (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(getPackageName() + "/.services.HongbaoService")) {
                serviceEnabled = true;
                break;
            }
        }

        if (serviceEnabled) {
            switchPlugin.setText(R.string.service_off);
        } else {
            switchPlugin.setText(R.string.service_on);
        }
    }

    public void onButtonClicked(View view) {
        startActivity(mAccessibleIntent);
    }

    public void openGithub(View view) {
        Intent browserIntent = new Intent(this, GallaryActivity.class);
        startActivity(browserIntent);
    }
    public void openSettings(View view) {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    public void openLuckyMoney(View view) {
        showIntegralCircle(view);
    }

    public int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) return getResources().getDimensionPixelSize(resourceId);
        return 0;
    }

    @TargetApi(11)
    public void showIntegralCircle(final View view) {
        ObjectAnimator//
                .ofFloat(view, "rotationY", 0.0F, 90.0F)//
                .setDuration(500)//
                .start();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent settingsIntent = new Intent(MainActivity.this, LuckyMoneyActivity.class);
                startActivity(settingsIntent);
                ObjectAnimator//
                        .ofFloat(view, "rotationY", 90.0F, 360.0F)//
                        .setDuration(500)//
                        .start();
            }
        }, 700);

    }
}
