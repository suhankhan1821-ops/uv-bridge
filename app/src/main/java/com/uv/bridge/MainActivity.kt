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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 100, 40, 40)

        val info = TextView(this)
        info.text = "UV Bridge v1.0"
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

        setContentView(layout)
    }
}
