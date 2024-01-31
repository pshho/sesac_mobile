package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity

class NewWebViewActivity : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = FrameLayout(this)
        setContentView(layout)

        val webView = WebView(this)
        val url = intent.getStringExtra("URL")
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url ?: "about:blank")
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.setSupportMultipleWindows(true)
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        layout.addView(webView)
    }
}