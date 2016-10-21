package braga.scrabble;

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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import braga.utils.ActivityBase;
import braga.utils.BidirectionalHashMap;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ScrabbleCrackerActivity extends ActivityBase {

    static BidirectionalHashMap<String, String> Languages = new BidirectionalHashMap<String, String>();
    static ArrayList<String> LanguageValues;

    static {
        Languages.put("de", "Deutsch");
        Languages.put("en", "English");
        Languages.put("es", "Español");
        Languages.put("fr", "Français");
        LanguageValues = new ArrayList<String>(Languages.values());
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
        this.buttonSolve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
        this.listViewResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { itemClick(position); }
        });
        this.editTextLetters.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return editTextHandLettersOnEditorAction(v, actionId, event);
            }
        });
        this.editTextBoardLetters.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return editTextBoardLettersOnEditorAction(v, actionId, event);
            }
        });
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
            error(e, "Error loading server response.");
        }
        String descr = this.scrabble.getDescription();
        track("main", "searchTimeServer", descr, response.serverTime);
        track("main", "searched", descr, 1);
        track("main", "searchCount", descr, response.length);
        track("main", "searchTime", descr, response.time);
    }

    private void setLanguages() {
        ArrayAdapter langAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, LanguageValues);
        langAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        this.spinnerLang.setAdapter(langAdapter);
        String languageKey = Locale.getDefault().getLanguage();
        if (Languages.containsKey(languageKey)) {
            this.spinnerLang.setSelection(LanguageValues.indexOf(Languages.get(languageKey)));
        }
    }

    private void setValues(ArrayList<HashMap<String, String>> values) {
        this.listViewResults.setAdapter(
                new SimpleAdapter(
                        this,
                        values,
                        android.R.layout.simple_list_item_2,
                        new String[] { "Word", "Value" },
                        new int[] { android.R.id.text1, android.R.id.text2 }));
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
            scrabble = new Scrabble(editTextLetters.getText().toString(), editTextBoardLetters.getText().toString(),  Languages.getKey((String)spinnerLang.getSelectedItem()));
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
                    error(error, "An error occured while contacting the server.");
                }
            });

            queue.add(request);
        } catch (Exception e) {
            setLoading(false);
            error(e, "An error occured while contacting the server.");
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

    private void itemClick(int position) {
        try {
            //AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            //this.selectionLayout.setVisibility(View.VISIBLE);
            //this.selectionWebView.loadData("<html><body></body></html>", "text/html", "utf8");
            //dialog.setCancelable(true);
            //dialog.setView(this.selectionLayout);
            //dialog.show();
            String word = (String) ((HashMap) listViewResults.getAdapter().getItem(position)).get("Word");
            itemToClipboard(word);
        } catch (Exception e) {
            error(e, "An error occured during copying to clipboard.");
        }
    }

    private void itemToClipboard(String word) {
        try {
            track("main", "copying", "item", 1);
            ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Scrabble Word Finder", word));
            track("main", "copied", "item", 1);
            Toast
                    .makeText(getApplicationContext(), "\"" + word + "\" was copied to clipboard.", Toast.LENGTH_SHORT)
                    .show();
            track("main", "copyOKed", "item", 1);
        } catch (Exception e) {
            error(e, "An error occured during copying to clipboard!");
        }
    }

    private void error(Exception e, String userMessage) {
        trackError("main", "item", e);
        Toast
                .makeText(getApplicationContext(), userMessage, Toast.LENGTH_SHORT)
                .show();
    }

    private boolean editTextHandLettersOnEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != EditorInfo.IME_ACTION_NEXT){
            return  false;
        }

        editTextBoardLetters.requestFocus();
        return true;
    }

    private boolean editTextBoardLettersOnEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != EditorInfo.IME_ACTION_SEARCH){
            return  false;
        }

        search();
        return true;
    }
}
