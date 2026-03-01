package com.hfad.digital_assistant

import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DocumentActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)

        val title = intent.getStringExtra("DOC_TITLE")
        val url = intent.getStringExtra("DOC_URL")

        supportActionBar?.title = title ?: "Документ"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        webView = findViewById(R.id.documentWebView)
        progressBar = findViewById(R.id.progressBar)

        if (url.isNullOrEmpty()) {
            Toast.makeText(this, "Ошибка загрузки документа", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupWebView()
        loadDocument(url)

        // Кнопка "три точки"
        val moreButton = findViewById<ImageView>(R.id.moreButton)
        moreButton.setOnClickListener {
            showPopupMenu(it, url, title ?: "document")
        }
    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
            }
        }

        webView.webChromeClient = WebChromeClient()
    }

    private fun loadDocument(url: String) {
        progressBar.visibility = View.VISIBLE

        when {
            url.endsWith(".pdf") -> {
                webView.loadUrl("https://docs.google.com/gview?embedded=true&url=$url")
            }
            url.endsWith(".jpg") || url.endsWith(".png") -> {
                webView.loadUrl(url)
            }
            else -> {
                webView.loadUrl(url)
            }
        }
    }

    private fun showPopupMenu(view: View, url: String, title: String) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menu.add("Скачать файл").setOnMenuItemClickListener {
            downloadFile(url, title)
            true
        }
        popupMenu.show()
    }

    private fun downloadFile(url: String, title: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(title)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "$title${getFileExtension(url)}"
            )

        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)

        Toast.makeText(this, "Скачивание началось", Toast.LENGTH_SHORT).show()
    }

    private fun getFileExtension(url: String): String {
        return "." + url.substringAfterLast('.', "pdf")
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}