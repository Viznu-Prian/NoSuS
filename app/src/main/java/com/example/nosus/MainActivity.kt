package com.example.nosus

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nosus.adapters.AppAdapter
import com.example.nosus.utils.SecurityUtils

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        scanInstalledApps()
    }

    private fun scanInstalledApps() {
        val pm = packageManager
        val packages = pm.getInstalledApplications(0)
            .filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 } // Exclude system apps

        appList = packages.map { it.packageName }
        recyclerView.adapter = AppAdapter(appList) { packageName ->
            checkAppSafety(packageName, pm)
        }
    }

    private fun checkAppSafety(packageName: String, pm: PackageManager) {
        val hash = SecurityUtils.getAppSha256(packageName, pm)

        if (hash != null && SecurityUtils.checkAppSafety(hash)) {
            Toast.makeText(this, "$packageName is MALICIOUS!", Toast.LENGTH_LONG).show()
            promptUninstall(packageName)
        } else {
            Toast.makeText(this, "$packageName is SAFE!", Toast.LENGTH_LONG).show()
        }
    }

    private fun promptUninstall(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }
}
