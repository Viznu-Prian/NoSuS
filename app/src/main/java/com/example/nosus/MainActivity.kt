package com.example.nosus

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nosus.adapters.AppAdapter
import com.example.nosus.utils.SecurityUtils

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

    private var uninstallingPackage: String? = null
    private var isUninstallMalicious: Boolean = false

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
        val userApps = packages.filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }

        userApps.forEach { app ->
            val appName = pm.getApplicationLabel(app).toString()
            val appIcon = pm.getApplicationIcon(app)
            val packageName = app.packageName
            val appInfo = AppInfo(appName, packageName, appIcon)

            val hash = SecurityUtils.getAppSha256(packageName, pm)
            if (hash != null) {
                SecurityUtils.checkAppSafety(hash) { isMalicious ->
                    runOnUiThread {
                        appInfo.isMalicious = isMalicious
                        if (isMalicious) {
                            maliciousApps.add(appInfo)
                        } else {
                            safeApps.add(appInfo)
                        }
                        refreshAdapters()
                    }
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Failed to get hash for $packageName", Toast.LENGTH_SHORT).show()
                    safeApps.add(appInfo) // Optional: assume safe
                    refreshAdapters()
                }
            }
        }
    }

    private fun refreshAdapters() {
        recyclerViewSafe.adapter = AppAdapter(safeApps.toMutableList(),
            onAppClick = { showToast(it) },
            onAppUninstall = { packageName ->
                uninstallingPackage = packageName
                isUninstallMalicious = false
                promptUninstall(packageName)
            })

        recyclerViewMalicious.adapter = AppAdapter(maliciousApps.toMutableList(),
            onAppClick = { showToast(it) },
            onAppUninstall = { packageName ->
                uninstallingPackage = packageName
                isUninstallMalicious = true
                promptUninstall(packageName)
            })
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

    private fun promptUninstall(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivityForResult(intent, UNINSTALL_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UNINSTALL_REQUEST_CODE && uninstallingPackage != null) {
            val pm = packageManager
            val isUninstalled = try {
                pm.getPackageInfo(uninstallingPackage!!, 0)
                false
            } catch (e: PackageManager.NameNotFoundException) {
                true
            }

            if (isUninstalled) {
                if (isUninstallMalicious) {
                    maliciousApps.removeAll { it.packageName == uninstallingPackage }
                } else {
                    safeApps.removeAll { it.packageName == uninstallingPackage }
                }
                refreshAdapters()
            }
            uninstallingPackage = null
        }
    }

    private fun showToast(appName: String) {
        Toast.makeText(this, "Clicked: $appName", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val UNINSTALL_REQUEST_CODE = 1001
    }
}
