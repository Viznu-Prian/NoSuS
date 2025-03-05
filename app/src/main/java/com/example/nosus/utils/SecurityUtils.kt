package com.example.nosus.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object SecurityUtils {

    private const val API_KEY = "0716722028182014875d2c9a81d53f3e16ac1119ab5cf4b54838c78baa58cb77   "  // 🔴 Replace with your actual API key
    private const val API_URL = "https://www.virustotal.com/api/v3/files/"
    fun getAppSha256(packageName: String, pm: PackageManager): String? {
        return try {
            val packageInfo: PackageInfo = pm.getPackageInfo(packageName, 0)
            val apkFile = File(packageInfo.applicationInfo?.sourceDir ?: return null)

            val digest = MessageDigest.getInstance("SHA-256")
            val inputStream = FileInputStream(apkFile)
            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            inputStream.close()

            val hashBytes = digest.digest()
            hashBytes.joinToString("") { "%02x".format(it) } // Convert to Hex String
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun checkAppSafety(hash: String): Boolean {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$API_URL$hash")
            .addHeader("x-apikey", API_KEY)
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return false

            val jsonResponse = JSONObject(response.body?.string() ?: "")
            val maliciousCount = jsonResponse
                .getJSONObject("data")
                .getJSONObject("attributes")
                .getJSONObject("last_analysis_stats")
                .getInt("malicious")

            return maliciousCount > 0  // If malicious count > 0, it's a risky app.
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
