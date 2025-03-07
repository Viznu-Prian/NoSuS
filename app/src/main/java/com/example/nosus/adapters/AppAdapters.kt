package com.example.nosus.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nosus.AppInfo
import com.example.nosus.R

class AppAdapter(
    private val apps: List<AppInfo>,
    private val onAppClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (apps[position].isMalicious) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutId = if (viewType == 1) R.layout.item_malicious_app else R.layout.item_safe_app
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return if (viewType == 1) MaliciousAppViewHolder(view) else SafeAppViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val app = apps[position]
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_in))

        if (holder is MaliciousAppViewHolder) {
            holder.appName.text = app.appName
            holder.appIcon.setImageDrawable(app.appIcon)

            holder.btnUninstall.setOnClickListener {
                val intent = Intent(Intent.ACTION_DELETE)
                intent.data = Uri.parse("package:${app.packageName}")
                holder.itemView.context.startActivity(intent)
            }
        } else if (holder is SafeAppViewHolder) {
            holder.appName.text = app.appName
            holder.appIcon.setImageDrawable(app.appIcon)
        }
    }

    override fun getItemCount(): Int = apps.size

    class SafeAppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.app_icon)
        val appName: TextView = view.findViewById(R.id.app_name)
    }

    class MaliciousAppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.app_icon)
        val appName: TextView = view.findViewById(R.id.app_name)
        val btnUninstall: ImageView = view.findViewById(R.id.uninstall_button)
    }
}
