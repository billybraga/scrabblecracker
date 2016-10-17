package braga.utils;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import braga.scrabble.AnalyticsApplication;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class ActivityBase extends AppCompatActivity {
    protected Tracker tracker;

    public ActivityBase() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        final AnalyticsApplication app = (AnalyticsApplication) getApplication();
        this.tracker = app.getDefaultTracker();

        SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        userPrefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener () {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(app.TRACKING_PREF_KEY)) {
                    GoogleAnalytics.getInstance(getApplicationContext()).setAppOptOut(sharedPreferences.getBoolean(key, false));
                }
            }
        });

        this.tracker.setScreenName("Image~" + this.getTitle());
        try {
            this.tracker.setAppVersion(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception e) {
        }
        this.tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public boolean isOnline() {
        NetworkInfo netInfo = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            return false;
        }
        return true;
    }

    public void messageBox(String message, String buttonText) {
        messageBox(message, buttonText, null);
    }

    public void messageBox(String message, String buttonText, OnClickListener clickListener) {
        Builder dialog = new Builder(this);
        dialog.setMessage(message);
        if (buttonText != null) {
            dialog.setPositiveButton(buttonText, clickListener);
        }
        dialog.show();
    }

    protected void track(String cat, String action, String label, int value) {
        this
            .tracker
            .send(new HitBuilders
                .EventBuilder()
                .setCategory(cat)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }

    protected void trackError(String cat, String location, Exception e) {
        track(cat, "error", String.format("%s - %s - %s", new Object[]{location, e.getMessage(), Tools.join(", ", e.getStackTrace())}), 1);
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
