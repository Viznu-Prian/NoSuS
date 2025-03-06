package com.example.nosus.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.MessageDigest


object SecurityUtils {
    private const val API_URL = "https://www.virustotal.com/api/v3/files/" // Adjust API URL if needed
    private const val API_KEY = "0716722028182014875d2c9a81d53f3e16ac1119ab5cf4b54838c78baa58cb77" // Replace with your actual API key

    fun checkAppSafety(hash: String, callback: (Boolean) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$API_URL$hash")
            .header("x-apikey", API_KEY)
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val maliciousCount = jsonResponse
                        .getJSONObject("data")
                        .getJSONObject("attributes")
                        .getJSONObject("last_analysis_stats")
                        .optInt("malicious", 0)

                    callback(maliciousCount > 0) // If malicious count > 0, mark as malicious
                } else {
                    callback(false) // Treat API failure as safe (fallback)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                callback(false) // Network error or exception, assume safe
            }
        }.start()
    }
    fun getAppSha256(packageName: String, pm: PackageManager): String? {
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val file = File(appInfo.sourceDir)
            val digest = MessageDigest.getInstance("SHA-256")
            val inputStream = FileInputStream(file)
            val buffer = ByteArray(8192)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            inputStream.close()

            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
