package com.uv.bridge

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class UvAccessibilityService : AccessibilityService() {

    companion object {
        var instance: UvAccessibilityService? = null
        var isRunning = false
        const val TAG = "UvBridge"
        const val CHATGPT_PACKAGE = "com.openai.chatgpt"
        const val TERMUX_PACKAGE = "com.termux"
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isRunning = true
        Log.d(TAG, "UV Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Reserved for future: detect ChatGPT "send button enabled" / response complete
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        instance = null
    }

    fun startCycle() {
        openApp(TERMUX_PACKAGE)
        handler.postDelayed({
            pasteIntoFocusedField()
            handler.postDelayed({
                pressEnterOnFocusedField()
                handler.postDelayed({
                    openApp(CHATGPT_PACKAGE)
                    handler.postDelayed({
                        pasteIntoFocusedField()
                    }, 2000)
                }, 15000)
            }, 1000)
        }, 1500)
    }

    private fun openApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
            Log.d(TAG, "Opened $packageName")
        } else {
            Log.e(TAG, "App not found: $packageName")
        }
    }

    private fun findFocusedEditable(): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        return findEditableNode(root)
    }

    private fun findEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findEditableNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun pasteIntoFocusedField() {
        val node = findFocusedEditable()
        if (node == null) {
            Log.e(TAG, "No editable field found to paste into")
            return
        }
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        node.performAction(AccessibilityNodeInfo.ACTION_PASTE)
        Log.d(TAG, "Pasted into field")
    }

    private fun pressEnterOnFocusedField() {
        val node = findFocusedEditable()
        if (node == null) {
            Log.e(TAG, "No editable field found for enter")
            return
        }
        val args = android.os.Bundle()
        args.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            (node.text?.toString() ?: "") + "\n"
        )
        node.performAction(AccessibilityNodeInfo.ACTION_IME_ENTER)
        Log.d(TAG, "Pressed enter (best effort)")
    }

    private fun setClipboard(text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("uv", text))
    }

    private fun getClipboard(): String {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip
        return if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0).text?.toString() ?: ""
        } else ""
    }
}
