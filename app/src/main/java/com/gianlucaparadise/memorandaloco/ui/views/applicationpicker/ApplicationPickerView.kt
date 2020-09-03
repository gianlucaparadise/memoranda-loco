package com.gianlucaparadise.memorandaloco.ui.views.applicationpicker

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gianlucaparadise.memorandaloco.R
import kotlinx.android.synthetic.main.view_application_picker.view.*

class ApplicationPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val tag = "ApplicationPicker"

    init {
        inflate(context, R.layout.view_application_picker, this)
        initList()
    }

    private fun initList() {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val apps = packages.map {
            AppDescriptor(
                packageName = it.packageName,
                appName = packageManager.getApplicationLabel(it).toString()
            )
        }

        apv_application_list.adapter = Adapter(apps)
    }

    data class AppDescriptor(
        val packageName: String,
        val appName: String
    ) {
        fun getIcon(packageManager: PackageManager): Drawable {
            return packageManager.getApplicationIcon(packageName)
        }
    }

    class Adapter(private val apps: List<AppDescriptor>) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun getItemCount(): Int = apps.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.view_application_picker_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = apps[position]
            holder.bind(app)
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val appIcon = itemView.findViewById<ImageView>(R.id.img_application_icon)
            private val appName = itemView.findViewById<TextView>(R.id.txt_application_name)

            fun bind(app: AppDescriptor) {
                val packageManager = itemView.context.packageManager
                val icon = app.getIcon(packageManager)
                appIcon.setImageDrawable(icon)

                appName.text = app.appName
            }
        }
    }
}