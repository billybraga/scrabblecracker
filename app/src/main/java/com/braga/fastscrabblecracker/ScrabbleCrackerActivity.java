package com.braga.fastscrabblecracker;

import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.braga.utils.ActivityBase;
import com.braga.utils.ParamedRunnable;
import com.braga.utils.Tools;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ScrabbleCrackerActivity extends ActivityBase {
    static ArrayList<String> LANGUAGES;
    Button button;
    EditText editTextBoardLetters;
    EditText editTextLetters;
    ListView listView;
    ProgressBar progressBar;
    Scrabble scrabble;
    Spinner spinnerLang;

    class ButtonClickListener implements  AdapterView.OnClickListener {

        @Override
        public void onClick(View v) {
            try {
                ScrabbleCrackerActivity.this.setLoadingOnUiThread(true);
                ScrabbleCrackerActivity.this.track("main", "click", "button", 1);
                ScrabbleCrackerActivity.this.scrabble = new Scrabble(ScrabbleCrackerActivity.this.editTextLetters.getText().toString(), ScrabbleCrackerActivity.this.editTextBoardLetters.getText().toString(), ScrabbleCrackerActivity.this.spinnerLang.getSelectedItem().toString());
                ScrabbleCrackerActivity.this.tracker.setLanguage(ScrabbleCrackerActivity.this.scrabble.getLang());
                ScrabbleCrackerActivity.this.track("main", "searching", ScrabbleCrackerActivity.this.scrabble.getDescription(), 1);

                RequestQueue queue = Volley.newRequestQueue(ScrabbleCrackerActivity.this);
                StringRequest request = new StringRequest(Request.Method.POST, ScrabbleCrackerActivity.this.scrabble.getQueryUrl(),
                        new Response.Listener<String>(){
                            @Override
                            public void onResponse(String response) {
                                ScrabbleCrackerActivity.this.setLoadingOnUiThread(false);
                                try {
                                    ScrabbleCrackerActivity.this.recieveData(response);
                                } catch (JSONException e) {
                                    ScrabbleCrackerActivity.this.setLoadingOnUiThread(false);
                                    ScrabbleCrackerActivity.this.messageBoxOnUIThread("An error occured while contacting the server ! ", "OK");
                                    ScrabbleCrackerActivity.this.trackError("main", "search, onclick", e);
                                }
                            }
                        }, new Response.ErrorListener(){

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ScrabbleCrackerActivity.this.setLoadingOnUiThread(false);
                    }
                });

                queue.add(request);

                ScrabbleCrackerActivity.this.setLoadingOnUiThread(false);
            } catch (Exception e) {
                ScrabbleCrackerActivity.this.setLoadingOnUiThread(false);
                ScrabbleCrackerActivity.this.messageBoxOnUIThread("An error occured while contacting the server ! ", "OK");
                ScrabbleCrackerActivity.this.trackError("main", "search, onclick", e);
            } catch (Throwable th) {
                ScrabbleCrackerActivity.this.setLoadingOnUiThread(false);
            }
        }
    }

    /* renamed from: braga.scrabble.ScrabbleCrackerActivity.3 */
    class ItemClickListener implements AdapterView.OnItemClickListener {
        ItemClickListener() {
        }

        public void onItemClick(AdapterView<?> a, View v, int i, long l) {
            try {
                ScrabbleCrackerActivity.this.track("main", "copying", "item", 1);
                String m = (String) ((HashMap) a.getAdapter().getItem(i)).get("Word");
                ((ClipboardManager) ScrabbleCrackerActivity.this.getSystemService(CLIPBOARD_SERVICE)).setText(m);
                ScrabbleCrackerActivity.this.track("main", "copied", "item", 1);
                ScrabbleCrackerActivity.this.messageBox("\"" + m + "\" was copied to clipboard!", "OK");
                ScrabbleCrackerActivity.this.track("main", "copyOKed", "item", 1);
            } catch (Exception e) {
                ScrabbleCrackerActivity.this.trackError("main", "item", e);
                ScrabbleCrackerActivity.this.messageBox("An error occured during copying to clipboard!", "OK");
            }
        }
    }

    /* renamed from: braga.scrabble.ScrabbleCrackerActivity.5 */
    class SetLoadingRunnable implements Runnable {
        private final /* synthetic */ boolean val$loading;

        SetLoadingRunnable(boolean z) {
            this.val$loading = z;
        }

        public void run() {
            ScrabbleCrackerActivity.this.setLoading(this.val$loading);
        }
    }

    /* renamed from: braga.scrabble.ScrabbleCrackerActivity.4 */
    class SetValuesRunnable extends ParamedRunnable {
        SetValuesRunnable(Object... params) {
            super(params);
        }

        public void run() {
            ScrabbleCrackerActivity.this.setValues((ArrayList) this.params[0]);
        }
    }

    static {
        LANGUAGES = new ArrayList();
        LANGUAGES.add("de");
        LANGUAGES.add("en");
        LANGUAGES.add("es");
        LANGUAGES.add("fr");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        this.button.setOnClickListener(new ButtonClickListener());
        this.listView.setOnItemClickListener(new ItemClickListener());
    }

    private void initLayout() {
        setContentView(R.layout.activity_scrabble_cracker);
        track("main", "open", "ScrabbleCrackerActivity", 1);
        this.button = (Button) findViewById(R.id.button1);
        this.listView = (ListView) findViewById(R.id.listView1);
        this.editTextBoardLetters = (EditText) findViewById(R.id.editTextBoardLetters);
        this.editTextLetters = (EditText) findViewById(R.id.editTextLetters);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.spinnerLang = (Spinner) findViewById(R.id.spinnerLang);
        setLoading(true);
        setLanguages();
        this.editTextLetters.requestFocus();
    }

    private void recieveData(String message) throws JSONException {
        ArrayList<HashMap<String, String>> values = new ArrayList();
        Scrabble.Response response = this.scrabble.parseResponse(message);
        runOnUiThread(new SetValuesRunnable(response.values));
        String descr = this.scrabble.getDescription();
        track("main", "searchTimeServer", descr, response.serverTime);
        track("main", "searched", descr, 1);
        track("main", "searchCount", descr, response.length);
        track("main", "searchTime", descr, response.time);
    }

    private void setLanguages() {
        ArrayAdapter<String> langAdapter = new ArrayAdapter(this, R.id.spinnerLang, LANGUAGES);
        langAdapter.setDropDownViewResource(R.id.spinnerLang);
        this.spinnerLang.setAdapter(langAdapter);
        int langIndex = LANGUAGES.indexOf(Locale.getDefault().getLanguage());
        if (langIndex == -1) {
            langIndex = LANGUAGES.indexOf("en");
        }
        this.spinnerLang.setSelection(langIndex);
    }

    private void setValues(ArrayList<HashMap<String, String>> values) {
        ArrayList<HashMap<String, String>> arrayList = values;
        this.listView.setAdapter(new SimpleAdapter(this, arrayList, 17367053, new String[]{"Word", "Value"}, new int[]{16908308, 16908309}));
    }

    private void setLoading(boolean loading) {
        if (loading) {
            this.progressBar.setVisibility(View.VISIBLE);
            this.button.setEnabled(false);
            return;
        }
        this.progressBar.setVisibility(View.INVISIBLE);
        this.button.setEnabled(true);
    }

    private void setLoadingOnUiThread(boolean loading) {
        runOnUiThread(new SetLoadingRunnable(loading));
    }
}
