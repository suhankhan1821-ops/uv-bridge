package com.uv.bridge

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {
    private const val TAG = "UvUpdateChecker"
    private const val API_URL = "https://api.github.com/repos/suhankhan1821-ops/uv-bridge/releases/latest"

    data class UpdateInfo(val version: String, val apkUrl: String)

    fun checkForUpdate(currentVersion: String, callback: (UpdateInfo) -> Unit) {
        Thread {
            try {
                val conn = URL(API_URL).openConnection() as HttpURLConnection
                conn.setRequestProperty("Accept", "application/vnd.github+json")
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                val code = conn.responseCode
                if (code != 200) {
                    Log.e(TAG, "GitHub API returned " + code)
                    return@Thread
                }
                val body = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(body)
                val tagName = json.getString("tag_name").removePrefix("v")
                if (tagName == currentVersion) {
                    return@Thread
                }
                val assets = json.getJSONArray("assets")
                var apkUrl = ""
                var found = false
                var i = 0
                while (i < assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.getString("name")
                    if (name.endsWith(".apk")) {
                        apkUrl = asset.getString("browser_download_url")
                        found = true
                        i = assets.length()
                    }
                    i = i + 1
                }
                if (found) {
                    callback(UpdateInfo(tagName, apkUrl))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Update check failed")
            }
        }.start()
    }

    fun downloadAndInstall(context: Context, apkUrl: String, version: String) {
        val request = DownloadManager.Request(Uri.parse(apkUrl))
        request.setTitle("UV Bridge Update " + version)
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "uv-bridge-update.apk")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
    }
}
