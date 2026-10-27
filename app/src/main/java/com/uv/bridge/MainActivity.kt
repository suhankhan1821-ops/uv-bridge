package com.uv.bridge

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val currentVersion = "1.0"
    private lateinit var statusText: TextView
    private lateinit var updateBtn: Button
    private var pendingApkUrl: String? = null
    private var pendingVersion: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 100, 40, 40)

        val info = TextView(this)
        info.text = "UV Bridge v" + currentVersion + "\n\nStep 1: Enable Accessibility Service\nStep 2: Tap Run Cycle"
        info.textSize = 16f
        layout.addView(info)

        val enableBtn = Button(this)
        enableBtn.text = "Open Accessibility Settings"
        enableBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        layout.addView(enableBtn)

        val runBtn = Button(this)
        runBtn.text = "Run Cycle"
        runBtn.setOnClickListener {
            if (!UvAccessibilityService.isRunning) {
                Toast.makeText(this, "Enable Accessibility Service first", Toast.LENGTH_LONG).show()
            } else {
                UvAccessibilityService.instance?.startCycle()
                Toast.makeText(this, "Cycle started", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(runBtn)

        statusText = TextView(this)
        statusText.text = "Checking for updates..."
        layout.addView(statusText)

        updateBtn = Button(this)
        updateBtn.text = "Download Update"
        updateBtn.visibility = android.view.View.GONE
        updateBtn.setOnClickListener {
            val url = pendingApkUrl
            val ver = pendingVersion
            if (url != null && ver != null) {
                UpdateChecker.downloadAndInstall(this, url, ver)
                Toast.makeText(this, "Downloading update...", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(updateBtn)

        setContentView(layout)
        checkUpdate()
    }

    private fun checkUpdate() {
        UpdateChecker.checkForUpdate(currentVersion) { info ->
            runOnUiThread {
                pendingApkUrl = info.apkUrl
                pendingVersion = info.version
                statusText.text = "Update available: v" + info.version
                updateBtn.visibility = android.view.View.VISIBLE
            }
        }
    }
}
