package braga.scrabble;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import braga.utils.ActivityBase;
import braga.utils.ParamedRunnable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ScrabbleCrackerActivity extends ActivityBase {

    static ArrayList<String> Languages = new ArrayList<String>();
    static HashMap<String, String> LanguageTextToKey = new HashMap<String, String>();
    static HashMap<String, String> LanguageKeyToText = new HashMap<String, String>();

    static {
        Languages.add("English");
        Languages.add("Français");
        Languages.add("Deutsch");
        Languages.add("Español");
        LanguageTextToKey.put("Deutsch", "de");
        LanguageTextToKey.put("English", "en");
        LanguageTextToKey.put("Español", "es");
        LanguageTextToKey.put("Français", "fr");
        LanguageKeyToText.put("de", "Deutsch");
        LanguageKeyToText.put("en", "English");
        LanguageKeyToText.put("es", "Español");
        LanguageKeyToText.put("fr", "Français");
    }

    private ProgressBar progressBar;
    private View selectionLayout;
    Button buttonSolve;
    EditText editTextBoardLetters;
    EditText editTextLetters;
    ListView listViewResults;
    Scrabble scrabble;
    Spinner spinnerLang;
    private ProgressBar selectionProgressBar;
    private WebView selectionWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        this.buttonSolve.setOnClickListener(new ButtonClickListener());
        this.listViewResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClick(position);
            }
        });
        this.editTextLetters.setOnEditorActionListener(new EditTextLettersOnEditorActionListener());
        this.editTextBoardLetters.setOnEditorActionListener(new EditTextBoardLettersOnEditorActionListener());
        setLoading(false);
    }

    private void initLayout() {
        setContentView(R.layout.activity_scrabble_cracker);
        track("main", "open", "ScrabbleCrackerActivity", 1);
        this.buttonSolve = (Button) findViewById(R.id.buttonSolve);
        this.listViewResults = (ListView) findViewById(R.id.listViewResults);
        this.editTextBoardLetters = (EditText) findViewById(R.id.editTextBoardLetters);
        this.editTextLetters = (EditText) findViewById(R.id.editTextLetters);
        this.spinnerLang = (Spinner) findViewById(R.id.spinnerLang);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.selectionLayout = findViewById(R.id.selectionLayout);
        this.selectionProgressBar = (ProgressBar) findViewById(R.id.selectionProgressBar);
        this.selectionWebView = (WebView) findViewById(R.id.selectionWebView);
        setLoading(false);
        setLanguages();
        this.editTextLetters.requestFocus();
    }

    private void recieveData(String message) {
        Scrabble.Response response = null;
        try {
            response = this.scrabble.parseResponse(message);
            setValues(response.values);
        } catch (JSONException e) {
            messageBox("Error loading server response.", "OK");
            trackError("main", "search", "parse", e);
        }
        String descr = this.scrabble.getDescription();
        track("main", "searchTimeServer", descr, response.serverTime);
        track("main", "searched", descr, 1);
        track("main", "searchCount", descr, response.length);
        track("main", "searchTime", descr, response.time);
    }

    private void setLanguages() {
        ArrayAdapter<String> langAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, Languages);
        langAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        this.spinnerLang.setAdapter(langAdapter);
        String languageKey = Locale.getDefault().getLanguage();
        if (LanguageKeyToText.containsKey(languageKey)) {
            String language = LanguageKeyToText.get(languageKey);
            int langIndex = Languages.indexOf(language);
            if (langIndex == -1) {
                langIndex = 0;
            }
            this.spinnerLang.setSelection(langIndex);
        }
    }

    private void setValues(ArrayList<HashMap<String, String>> values) {
        ArrayList<HashMap<String, String>> arrayList = values;
        this.listViewResults.setAdapter(new SimpleAdapter(this, arrayList, 17367053, new String[]{"Word", "Value"}, new int[]{16908308, 16908309}));
    }

    private void setLoading(boolean loading) {
        this.buttonSolve.setEnabled(!loading);
        this.progressBar.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
    }

    private  void search() {
        try {
            setLoading(true);
            hideKeyboad();
            track("main", "click", "buttonSolve", 1);
            scrabble = new Scrabble(editTextLetters.getText().toString(), editTextBoardLetters.getText().toString(),  LanguageTextToKey.get(spinnerLang.getSelectedItem().toString()));
            tracker.setLanguage(scrabble.getLang());
            track("main", "searching", scrabble.getDescription(), 1);

            RequestQueue queue = Volley.newRequestQueue(ScrabbleCrackerActivity.this);
            StringRequest request = new StringRequest(Request.Method.POST, scrabble.getQueryUrl(),
                new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    setLoading(false);
                    recieveData(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    setLoading(false);
                    messageBox("An error occured while contacting the server ! ", "OK");
                    trackError("main", "search, onclick", e);
                }
            });

            queue.add(request);
        } catch (Exception e) {
            setLoading(false);
            messageBox("An error occured while contacting the server ! ", "OK");
            trackError("main", "search, onclick", e);
        } catch (Throwable th) {
            setLoading(false);
        }
    }

    private void hideKeyboad() {
        LinearLayout mainLayout;

        // Get your layout set up, this is just an example
        mainLayout = (LinearLayout)findViewById(R.id.activity_scrabble_cracker);

        // Then just use the following:
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);
    }

    class ButtonClickListener implements  AdapterView.OnClickListener {

        @Override
        public void onClick(View v) {
            search();
        }
    }

    private void itemClick(int position) {
        try {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            this.selectionLayout.setVisibility(View.VISIBLE);
            this.selectionWebView.loadData("<html><body></body></html>", "text/html", "utf8");
            dialog.setCancelable(true);
            dialog.setView(this.selectionLayout);
            dialog.show();
            String word = (String) ((HashMap) listViewResults.getAdapter().getItem(position)).get("Word");
            RequestQueue queue = Volley.newRequestQueue(ScrabbleCrackerActivity.this);
            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                "https://en.wiktionary.org/w/api.php?format=json&action=query&titles=" + URLEncoder.encode(word, "utf8") + "&rvprop=content&prop=revisions&redirects=1",
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject pages = response.getJSONObject("query").getJSONObject("pages");
                            String key = pages.keys().next();
                            JSONObject revision = (JSONObject) pages.getJSONObject(key).getJSONArray("revisions").get(0);
                            JsonObjectRequest request = new JsonObjectRequest(
                                Request.Method.GET,
                                "https://en.wiktionary.org/w/api.php?format=json&action=parse&text=" + URLEncoder.encode(revision.getString("*"), "utf8"),
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                String htmlContent = response.getJSONObject("parse").getJSONObject("text").getString("*");
                                                selectionWebView.loadData(htmlContent, "text/html", "utf8");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {

                                        }
                                    }
                            );
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                selectionWebView.loadData("<html><body>Could not find definition.</body></html>", "text/html", "utf8");
                trackError("main", "definition-revisions", error);
            }
        });

            queue.add(request);
        } catch (Exception e) {
            trackError("main", "item", e);
            messageBox("An error occured during copying to clipboard!", "OK");
        }
    }

    private void itemToClipboard(String word) {
        try {
            track("main", "copying", "item", 1);
            ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Scrabble Word Finder", word));
            track("main", "copied", "item", 1);
            Toast.makeText(getApplicationContext(), "\"" + word + "\" was copied to clipboard!", Toast.LENGTH_SHORT);
            track("main", "copyOKed", "item", 1);
        } catch (Exception e) {
            trackError("main", "item", e);
            messageBox("An error occured during copying to clipboard!", "OK");
        }
    }

    class EditTextLettersOnEditorActionListener implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId != EditorInfo.IME_ACTION_NEXT){
                return  false;
            }

            editTextBoardLetters.requestFocus();
            return true;
        }
    }

    class EditTextBoardLettersOnEditorActionListener implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId != EditorInfo.IME_ACTION_SEARCH){
                return  false;
            }

            search();
            return true;
        }
    }
}
