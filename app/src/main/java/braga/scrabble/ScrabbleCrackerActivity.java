package braga.scrabble;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.DebugUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
import android.widget.RelativeLayout;
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
import braga.utils.SingleHostWebViewClient;

import org.json.JSONException;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ScrabbleCrackerActivity extends ActivityBase implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "ScrabbleCrackerActivity";
    static BidirectionalHashMap<String, String> Languages = new BidirectionalHashMap<String, String>();
    static ArrayList<String> LanguageValues;

    static {
        System.loadLibrary("opencv_java3");
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
    private WebView selectionWebView;
    private RelativeLayout boardDetectionLayout;
    private JavaCameraView boardDetectionCameraView;
    private LinearLayout generalLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        initLayout();
    }

    private void initLayout() {
        setContentView(R.layout.activity_scrabble_cracker);

        track("main", "open", "ScrabbleCrackerActivity", 1);
        this.generalLayout = (LinearLayout) findViewById(R.id.generalLayout);
        this.boardDetectionCameraView = (JavaCameraView) findViewById(R.id.boardDetectionCameraView);
        this.boardDetectionLayout = (RelativeLayout) findViewById(R.id.boardDetectionLayout);
        this.buttonSolve = (Button) findViewById(R.id.buttonSolve);
        this.listViewResults = (ListView) findViewById(R.id.listViewResults);
        this.editTextBoardLetters = (EditText) findViewById(R.id.editTextBoardLetters);
        this.editTextLetters = (EditText) findViewById(R.id.editTextLetters);
        this.spinnerLang = (Spinner) findViewById(R.id.spinnerLang);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.selectionLayout = findViewById(R.id.selectionLayout);
        this.selectionWebView = (WebView) findViewById(R.id.selectionWebView);
        this.selectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectionLayout.setVisibility(View.INVISIBLE);
            }
        });
        this.selectionWebView.setWebViewClient(new SingleHostWebViewClient("wiktionary.org"));
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
        this.editTextLetters.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editTextLetters.getRight() - editTextLetters.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        launchBoardDetection();
                        return true;
                    }
                }
                return false;
            }
        });
        this.boardDetectionCameraView.setCvCameraViewListener(this);
        setLanguages();
        this.editTextLetters.requestFocus();
        setLoading(false);
    }

    @Override
    public void onBackPressed() {
        if (this.boardDetectionLayout.getVisibility() == View.VISIBLE) {
            this.searchMode(true);
            return;
        }

        if (this.selectionLayout.getVisibility() == View.VISIBLE) {
            if (this.selectionWebView.canGoBack()) {
                this.selectionWebView.goBack();
                return;
            }

            this.searchMode(true);
            return;
        }

        super.onBackPressed();
    }

    private void launchBoardDetection() {
        this.boardDetectionMode(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.boardDetectionCameraView != null)
            this.boardDetectionCameraView.disableView();
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

    private String getLangKey() {
        return Languages.getKey((String)this.spinnerLang.getSelectedItem());
    }

    private  void search() {
        try {
            this.selectionLayout.setVisibility(View.INVISIBLE);
            setLoading(true);
            hideKeyboad();
            track("main", "click", "buttonSolve", 1);
            scrabble = new Scrabble(editTextLetters.getText().toString(), editTextBoardLetters.getText().toString(), this.getLangKey());
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
        RelativeLayout mainLayout = (RelativeLayout)findViewById(R.id.activity_scrabble_cracker);

        // Then just use the following:
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);
    }

    private void itemClick(int position) {
        try {
            this.hideKeyboad();
            String word = (String) ((HashMap) listViewResults.getAdapter().getItem(position)).get("Word");
            this.itemToClipboard(word);
            this.selectionWebView.loadData("", "text/plain", "utf8");
            this.selectionWebView.clearHistory();
            this.selectionWebView.loadUrl("https://" + this.getLangKey() + ".m.wiktionary.org/wiki/" + URLEncoder.encode(word, "utf8"));
            this.selectionLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            error(e, "An error occurred loading wiktionary.");
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("editTextBoardLetters", this.editTextBoardLetters.getText().toString());
        outState.putString("editTextLetters", this.editTextLetters.getText().toString());
        outState.putInt("focus", this.editTextBoardLetters.hasFocus() ? R.id.editTextBoardLetters : R.id.editTextLetters);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        this.editTextLetters.setText(savedInstanceState.getString("editTextLetters"));
        this.editTextBoardLetters.setText(savedInstanceState.getString("editTextBoardLetters"));
        findViewById(savedInstanceState.getInt("focus")).requestFocus();
        this.search();
        super.onRestoreInstanceState(savedInstanceState);
    }

    private boolean editTextBoardLettersOnEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != EditorInfo.IME_ACTION_SEARCH){
            return  false;
        }

        search();
        return true;
    }

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    boardDetectionCameraView.enableView();
                    boardDetectionCameraView.setOnTouchListener(ScrabbleCrackerActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch invoked");

        // mCalibrator.addCorners();
        return false;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return null;
    }

    private void hideAll() {
        this.searchMode(false);
        this.selectionMode(false);
        this.boardDetectionMode(false);
    }

    private void boardDetectionMode(boolean enabled) {
        this.setMode(this.boardDetectionLayout);
    }

    private void searchMode(boolean enabled) {
        this.setMode(this.generalLayout);
    }

    private void selectionMode(boolean enabled) {
        this.setMode(this.selectionLayout);
    }

    private void setMode(View view) {
        this.hideAll();
        this.setVisibility(view, true);
    }

    private void setVisibility(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
}
