package com.braga.utils;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.braga.fastscrabblecracker.AnalyticsApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class ActivityBase extends AppCompatActivity {
    protected Tracker tracker;

    /* renamed from: braga.utils.ActivityBase.1 */
    class ShowMessageRunnable extends ParamedRunnable {
        ShowMessageRunnable(Object... params) {
            super(params);
        }

        public void run() {
            ActivityBase.this.messageBox(this.params[0].toString(), this.params[1].toString());
        }
    }

    /* renamed from: braga.utils.ActivityBase.2 */
    class ShowMessage2Runnable extends ParamedRunnable {
        ShowMessage2Runnable(Object... $anonymous0) {
            super($anonymous0);
        }

        public void run() {
            ActivityBase.this.messageBox((String) this.params[0], (String) this.params[1], (OnClickListener) this.params[2]);
        }
    }

    public ActivityBase() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.tracker = ((AnalyticsApplication) getApplication()).getDefaultTracker();
    }

    @Override
    public void onResume() {
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

    public void messageBoxOnUIThread(String message, String button) {
        runOnUiThread(new ShowMessageRunnable(message, button));
    }

    public void messageBoxOnUIThread(String message, String button, OnClickListener clickListener) {
        runOnUiThread(new ShowMessage2Runnable(message, button, clickListener));
    }

    public void messageBox(String message, String neutralButtonText) {
        messageBox(message, neutralButtonText, null);
    }

    public void messageBox(String message, String neutralButtonText, OnClickListener clickListener) {
        Builder dialog = new Builder(this);
        dialog.setMessage(message);
        if (neutralButtonText != null) {
            dialog.setNeutralButton(neutralButtonText, clickListener);
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
