package com.berlin.homeradar.presentation.screen.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.berlin.homeradar.R
import com.berlin.homeradar.data.config.FeatureFlags

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppBrowserScreen(
    initialUrl: String,
    title: String,
    onBack: () -> Unit,
    onOpenExternal: (String) -> Unit,
) {
    var currentUrl by remember(initialUrl) { mutableStateOf(initialUrl) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.apply {
                stopLoading()
                clearHistory()
                loadUrl("about:blank")
                removeAllViews()
                destroy()
            }
            webViewRef = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title.ifBlank { currentUrl }) },
                navigationIcon = {
                    IconButton(onClick = {
                        val webView = webViewRef
                        if (webView?.canGoBack() == true) webView.goBack() else onBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { webViewRef?.reload() }) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.browser_refresh_action),
                        )
                    }
                    IconButton(onClick = { onOpenExternal(currentUrl) }) {
                        Icon(
                            Icons.Outlined.OpenInBrowser,
                            contentDescription = stringResource(R.string.browser_open_external_action),
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        webViewRef = this
                        configureSecureSettings(initialUrl)
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress / 100f
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val targetUrl = request?.url?.toString().orEmpty().ifBlank { currentUrl }
                                currentUrl = targetUrl
                                return when (WebViewNavigationPolicy.resolve(targetUrl)) {
                                    WebViewNavigationDecision.ALLOW_IN_WEBVIEW -> false
                                    WebViewNavigationDecision.OPEN_EXTERNALLY -> {
                                        onOpenExternal(targetUrl)
                                        true
                                    }
                                    WebViewNavigationDecision.BLOCK -> true
                                }
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                                currentUrl = url ?: currentUrl
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                currentUrl = url ?: currentUrl
                            }
                        }
                        loadUrl(initialUrl)
                    }
                },
                update = { webViewRef = it }
            )
            if (progress in 0f..0.99f) {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.align(Alignment.TopCenter))
            }
            if (isLoading && progress < 0.1f) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.configureSecureSettings(initialUrl: String) {
    val host = runCatching { android.net.Uri.parse(initialUrl).host.orEmpty() }.getOrDefault("")
    settings.apply {
        javaScriptEnabled = WebViewSecurityPolicy.shouldEnableJavaScript(host)
        domStorageEnabled = javaScriptEnabled
        loadsImagesAutomatically = true
        builtInZoomControls = false
        displayZoomControls = false
        allowFileAccess = false
        allowContentAccess = false
        javaScriptCanOpenWindowsAutomatically = false
        setSupportMultipleWindows(false)
        cacheMode = WebSettings.LOAD_DEFAULT
        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
    }
    CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
}

private object WebViewSecurityPolicy {
    fun shouldEnableJavaScript(host: String): Boolean {
        return host in FeatureFlags.webViewJavaScriptAllowedHosts
    }
}


private enum class WebViewNavigationDecision {
    ALLOW_IN_WEBVIEW,
    OPEN_EXTERNALLY,
    BLOCK,
}

private object WebViewNavigationPolicy {
    fun resolve(url: String): WebViewNavigationDecision {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return WebViewNavigationDecision.BLOCK
        return when (uri.scheme?.lowercase()) {
            "http", "https" -> WebViewNavigationDecision.ALLOW_IN_WEBVIEW
            "mailto", "tel", "sms", "smsto", "market", "geo", "intent" -> WebViewNavigationDecision.OPEN_EXTERNALLY
            "about" -> if (url == "about:blank") WebViewNavigationDecision.ALLOW_IN_WEBVIEW else WebViewNavigationDecision.BLOCK
            else -> WebViewNavigationDecision.BLOCK
        }
    }
}
