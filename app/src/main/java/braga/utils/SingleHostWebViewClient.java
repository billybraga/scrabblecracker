package braga.utils;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Billy on 10/21/2016.
 */

public class SingleHostWebViewClient extends WebViewClient {
    private final String host;

    public SingleHostWebViewClient(String host) {
        this.host = host;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (Uri.parse(url).getHost().endsWith(this.host)) {
            return false;
        }

        return true;
    }
}
