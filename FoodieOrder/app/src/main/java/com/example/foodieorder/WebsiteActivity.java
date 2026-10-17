package com.example.foodieorder;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;

// 8. Restaurant Website (WebView)
public class WebsiteActivity extends BaseActivity {

    WebView webView;
    EditText etUrl;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_website);
        setupToolbar("Restaurant Website");

        webView = findViewById(R.id.webView);
        etUrl = findViewById(R.id.etUrl);
        progress = findViewById(R.id.progress);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
            }
        });

        String url = getIntent().getStringExtra("url");
        if (url == null) url = AppData.WEBSITES[0];
        etUrl.setText(url);
        webView.loadUrl(url);

        findViewById(R.id.btnOpen).setOnClickListener(v -> {
            String u = etUrl.getText().toString().trim();
            if (!u.startsWith("http")) u = "https://" + u;
            webView.loadUrl(u);
        });
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
