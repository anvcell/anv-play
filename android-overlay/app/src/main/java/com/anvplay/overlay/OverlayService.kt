package com.anvplay.overlay

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.Timestamp
import java.util.Locale

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var panel: LinearLayout? = null
    private var label: TextView? = null
    private var registration: ListenerRegistration? = null
    private val handler = Handler(Looper.getMainLooper())
    private var endAt = 0L
    private var active = false

    private val tick = object : Runnable {
        override fun run() {
            if (active && endAt > 0L) {
                val remaining = ((endAt - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
                if (remaining <= 60L) {
                    showOverlay(remaining)
                } else {
                    hideOverlay()
                }
            } else {
                hideOverlay()
            }
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()
        registration = db.collection("unit_status").document("PS3 Unit 1")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    active = false
                    return@addSnapshotListener
                }
                val status = snapshot.getString("status") ?: ""
                val value = snapshot.get("timerEnd")
                endAt = when (value) {
                    is Timestamp -> value.toDate().time
                    is Number -> value.toLong()
                    else -> 0L
                }
                active = status == "Sedang Terpakai" && endAt > 0L
            }
        handler.post(tick)
    }

    private fun showOverlay(seconds: Long) {
        if (panel == null) {
            label = TextView(this).apply {
                setTextColor(Color.WHITE)
                textSize = 22f
                setPadding(18, 10, 18, 10)
                setBackgroundColor(Color.argb(150, 10, 15, 30))
                gravity = Gravity.CENTER
            }
            panel = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(2, 2, 2, 2)
                addView(label)
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = 24
                y = 24
            }
            try { windowManager.addView(panel, params) } catch (_: Exception) { panel = null }
        }
        val m = seconds / 60
        val s = seconds % 60
        label?.text = "AnV Play  %02d:%02d".format(Locale.US, m, s)
        label?.setTextColor(if (seconds <= 10) Color.RED else Color.WHITE)
    }

    private fun hideOverlay() {
        panel?.let { try { windowManager.removeView(it) } catch (_: Exception) {} }
        panel = null
        label = null
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        registration?.remove()
        hideOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}