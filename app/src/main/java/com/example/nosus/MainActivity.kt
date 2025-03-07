package com.example.nosus

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nosus.adapters.AppAdapter

data class AppInfo(
    val appName: String,
    val packageName: String,
    val appIcon: Drawable,
    var isMalicious: Boolean = false
)

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewSafe: RecyclerView
    private lateinit var recyclerViewMalicious: RecyclerView
    private lateinit var toggleSafeApps: TextView
    private lateinit var toggleMaliciousApps: TextView

    private val safeApps = mutableListOf<AppInfo>()
    private val maliciousApps = mutableListOf<AppInfo>()

    private var showingSafeApps = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerViewSafe = findViewById(R.id.recyclerViewSafe)
        recyclerViewMalicious = findViewById(R.id.recyclerViewMalicious)
        toggleSafeApps = findViewById(R.id.toggleSafeApps)
        toggleMaliciousApps = findViewById(R.id.toggleMaliciousApps)

        recyclerViewSafe.layoutManager = LinearLayoutManager(this)
        recyclerViewMalicious.layoutManager = LinearLayoutManager(this)

        toggleSafeApps.setOnClickListener { toggleApps(true) }
        toggleMaliciousApps.setOnClickListener { toggleApps(false) }

        scanInstalledApps()
        showSafeAppsOnLaunch()
    }

    private fun scanInstalledApps() {
        val pm = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        val appList = packages.filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }.map {
            val appName = pm.getApplicationLabel(it).toString()
            val appIcon = pm.getApplicationIcon(it)
            AppInfo(appName, it.packageName, appIcon)
        }

        appList.forEach { app ->
            if (app.packageName.contains("malware")) {
                maliciousApps.add(app)
            } else {
                safeApps.add(app)
            }
        }

        recyclerViewSafe.adapter = AppAdapter(safeApps) { showToast(it) }
        recyclerViewMalicious.adapter = AppAdapter(maliciousApps) { showToast(it) }
    }

    private fun showSafeAppsOnLaunch() {
        recyclerViewSafe.visibility = View.VISIBLE
        recyclerViewMalicious.visibility = View.GONE

        toggleSafeApps.setBackgroundResource(R.color.green)
        toggleMaliciousApps.setBackgroundResource(R.color.gray)

        showingSafeApps = true
    }

    private fun toggleApps(showSafe: Boolean) {
        if (showingSafeApps == showSafe) return

        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        if (showSafe) {
            recyclerViewMalicious.startAnimation(fadeOut)
            recyclerViewMalicious.visibility = View.GONE
            recyclerViewSafe.visibility = View.VISIBLE
            recyclerViewSafe.startAnimation(fadeIn)

            toggleSafeApps.setBackgroundResource(R.color.green)
            toggleMaliciousApps.setBackgroundResource(R.color.gray)
        } else {
            recyclerViewSafe.startAnimation(fadeOut)
            recyclerViewSafe.visibility = View.GONE
            recyclerViewMalicious.visibility = View.VISIBLE
            recyclerViewMalicious.startAnimation(fadeIn)

            toggleMaliciousApps.setBackgroundResource(R.color.red)
            toggleSafeApps.setBackgroundResource(R.color.gray)
        }

        showingSafeApps = showSafe
    }

    private fun showToast(appName: String) {
        Toast.makeText(this, "Clicked: $appName", Toast.LENGTH_SHORT).show()
    }
}
