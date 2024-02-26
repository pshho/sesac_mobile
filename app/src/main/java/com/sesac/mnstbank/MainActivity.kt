package com.sesac.mnstbank

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.sesac.mnstbank.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private var myWebView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val checkRoot = CheckRoot(this)
        if (checkRoot.isRooted()) {
            showRootingAlertDialog()
            return
        }
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AndroidView({context ->
                        myWebView = WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.javaScriptCanOpenWindowsAutomatically = true
                            settings.setSupportMultipleWindows(true) // 새 창 띄우기
                            settings.loadsImagesAutomatically = true
                            settings.useWideViewPort = true // viewport 설정
                            settings.loadWithOverviewMode = true // viewport와 같이 설정
                            settings.setSupportZoom(true)
                            settings.allowContentAccess = true // Content URL 접근 사용 여부
                            settings.domStorageEnabled = true
                            settings.userAgentString = "app"
                            settings.defaultTextEncodingName = "UTF-8" // 인코딩
                            settings.databaseEnabled = true // 데이터베이스 연결 허용
                            settings.allowFileAccess = true // 파일 액세스 허용
                            settings.blockNetworkImage = false // 네트워크 통해 이미지 리소스 받기



                            webViewClient = WebViewClient()
                            webChromeClient = object : WebChromeClient() {
                                override fun onCreateWindow(
                                    view: WebView?,
                                    isDialog: Boolean,
                                    isUserGesture: Boolean,
                                    resultMsg: Message?
                                ): Boolean {
                                    val transport = resultMsg?.obj as? WebView.WebViewTransport
                                    val newWebView = WebView(view!!.context).apply {
                                        settings.javaScriptEnabled = true
                                        settings.javaScriptCanOpenWindowsAutomatically = true
                                        settings.setSupportMultipleWindows(true)
                                        webViewClient = WebViewClient()
                                        webChromeClient = WebChromeClient()
                                    }

                                    // Create a dialog and set the WebView as its content
                                    val dialog = Dialog(view.context)
                                    dialog.setContentView(newWebView)

                                    // Set up the dialog properties (e.g., size, position)
                                    // Here, you can customize the dialog according to your requirements

                                    // Extract the URL from the original WebView and load it into the new WebView
                                    val url = resultMsg?.data?.getString("url")
                                    if (!url.isNullOrBlank()) {
                                        newWebView.loadUrl(url)
                                    }

                                    // Show the dialog
                                    dialog.show()
                                    transport?.webView = newWebView
                                    resultMsg?.sendToTarget()
                                    return true
                                }
                            }
                            setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                                val request = DownloadManager.Request(Uri.parse(url))
                                val filename = URLUtil.guessFileName(url, contentDisposition, mimetype)
                                val cookies = CookieManager.getInstance().getCookie(url)
                                request.addRequestHeader("cookie", cookies)
                                request.addRequestHeader("User-Agent", userAgent)
                                request.setDescription("Downloading file..")
                                request.setTitle(filename)
                                request.allowScanningByMediaScanner()
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
                                val dManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                                dManager.enqueue(request)
                            })

                            loadUrl("https://mnstbank.com/")
                        }
                        myWebView!!
                    })
                }
            }
        }
    }
    override fun onBackPressed() {
        if (myWebView!!.canGoBack()) {
            myWebView?.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun showRootingAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle("루팅 감지")
            .setMessage("루팅이 감지되었습니다. 어플을 종료합니다.")
            .setPositiveButton("확인") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name!",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}