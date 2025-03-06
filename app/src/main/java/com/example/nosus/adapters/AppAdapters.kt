package com.example.nosus.adapters

import android.content.pm.PackageManager
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nosus.R
import com.example.nosus.utils.SecurityUtils

class AppAdapter(
    private val appList: List<String>,
    private val pm: PackageManager, // Add this parameter
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.appNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val packageName = appList[position]

        holder.appName.text = packageName
        holder.itemView.setOnClickListener { onItemClick(packageName) }
    }

    override fun getItemCount(): Int = appList.size
}
