package com.afss.pslattendance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    ProgressBar progressBar;
    TextView noInternet;
    String TAG = "MainActivity";
    private ConnectionDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.attendence_web_view);
        progressBar = (ProgressBar) findViewById(R.id.webViewLoader);
        noInternet = (TextView) findViewById(R.id.noInternetTv);

        detector = new ConnectionDetector(MainActivity.this);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setVisibility(View.GONE);


        // allowing cookie to store in Devices so pore we Can use

        webView.setWebViewClient(new WebViewClient() {

                                     @Override
                                     public void onPageFinished(WebView view, String url) {
                                         CookieSyncManager.getInstance().sync();
                                     }
                                 }


        );
        if (savedInstanceState != null) {
            // Restore the previous URL and history stack
            webView.restoreState(savedInstanceState);
        }

        // check Internet
        if (detector.isInternetAvailable()) {
            Log.d(TAG, "Internet Present");
        } else {
            Log.d(TAG, "No Internet");
            this.registerReceiver(this.mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }


        webView.loadUrl("https://hr.surecash.net/web?#action=133&menu_id=96");

        webView.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.loadUrl(request.getUrl().toString());
                }
                return false;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                try {
                    String current_url = webView.getUrl();
                    if (current_url.contains("web?#action=133&menu_id=96")) {
                        try {
                            webView.loadUrl("javascript:(function() { " +
                                    "document.getElementsByTagName('header')[0].style.display='none'; " +
                                    "})()");
                        } finally {
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    //what you want to do
                                }
                            }, 2000);
                            progressBar.setVisibility(View.GONE);
                            webView.setVisibility(View.VISIBLE);
                            Log.d("WV", "H HIDDEN");
                        }

                    } else {
                        try {
                            webView.loadUrl("javascript:(function() { " +
                                    "document.getElementsByTagName('header')[0].style.display='none'; " +
                                    "document.getElementsByTagName('footer')[0].style.display='none'; " +
                                    "})()");
                        } finally {
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    //what you want to do
                                }
                            }, 2000);
                            progressBar.setVisibility(View.GONE);
                            webView.setVisibility(View.VISIBLE);
                            Log.d(TAG, "HF HIDDEN");
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "Header and Footer in UNHIDE state");
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                webView.loadUrl("file:///android_asset/error.html");
                webView.setVisibility(View.GONE);
                Log.d(TAG, "No internet");

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        IntentFilter intentFilter = new IntentFilter("com.afss.pslattendance.MainActivity");
        MainActivity.this.registerReceiver(mConnReceiver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        MainActivity.this.unregisterReceiver(mConnReceiver);
    }

    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

            NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

            if (currentNetworkInfo.isConnected()) {
                Log.d(TAG, "Connected");
                finish();
                startActivity(getIntent());
                Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "Not Connected");
                Toast.makeText(getApplicationContext(), "Not Connected",
                        Toast.LENGTH_LONG).show();
            }
        }
    };


}