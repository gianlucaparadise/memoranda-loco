package com.gianlucaparadise.memorandaloco.ui.views.applicationpicker

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.RecyclerView
import com.gianlucaparadise.memorandaloco.R
import kotlinx.android.synthetic.main.view_application_picker.view.*
import kotlin.properties.Delegates

typealias ApplicationPickerViewSelectionChange = (packageName: String?) -> Unit

class ApplicationPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    companion object {
        private const val TAG = "ApplicationPicker"

        @BindingAdapter("selectedApp")
        @JvmStatic
        fun setSelectedApp(view: ApplicationPickerView, newValue: String?) {
            // Important to break potential infinite loops.
            if (view.selectedAppPackageName == newValue) return

            view.selectedAppPackageName = newValue
        }

        @InverseBindingAdapter(attribute = "selectedApp")
        @JvmStatic
        fun getSelectedApp(view: ApplicationPickerView): String {
            return view.selectedAppPackageName ?: ""
        }

        @BindingAdapter("app:selectedAppAttrChanged")
        @JvmStatic
        fun setSelectedAppListener(
            view: ApplicationPickerView,
            attrChange: InverseBindingListener
        ) {
            view.onSelectionChanged.add {
                attrChange.onChange()
            }
        }
    }

    val onSelectionChanged = mutableListOf<ApplicationPickerViewSelectionChange>()

    var selectedAppPackageName: String?
        get() = _selectedAppPackageName
        set(value) {
            if (_selectedAppPackageName == value) return

            if (value.isNullOrBlank()) selectionTracker?.clearSelection()
            else selectionTracker?.select(value)
            // The SelectionTrakcer should then set _selectedAppPackageName and trigger onSelectionChanged

            scrollToElement(value)
        }

    private var _selectedAppPackageName: String? by Delegates.observable<String?>(null) { _, _, newValue ->
        onSelectionChanged.forEach { it(newValue) }
    }

    private val _installedApps = mutableListOf<String>()

    /**
     * List of installed apps package names
     */
    val installedApps: List<String> = _installedApps

    init {
        inflate(context, R.layout.view_application_picker, this)
        initList()

        //val attributes = context.obtainStyledAttributes(attrs, R.styleable.ApplicationPickerView)
        // read attributes here...
        //attributes.recycle()
    }

    private fun initList() {
        val packageManager = context.packageManager
        val packages = packageManager
            .getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0 } // excluding system apps

        val installedApps = packages.map {
            AppDescriptor(
                packageName = it.packageName,
                appName = packageManager.getApplicationLabel(it).toString()
            )
        }

        val installedAppsPackageNames = installedApps.map { it.packageName }
        _installedApps.clear()
        _installedApps.addAll(installedAppsPackageNames)

        val adapter = Adapter(installedApps)
        apv_application_list.adapter = adapter

        selectionTracker = SelectionTracker.Builder(
            "appSelection",
            apv_application_list,
            MyItemKeyProvider(adapter),
            MyItemDetailsLookup(apv_application_list),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectSingleAnything()
        ).build()

        selectionTracker?.addObserver(
            object : SelectionTracker.SelectionObserver<String>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    _selectedAppPackageName = selectionTracker?.selection?.firstOrNull()
                }
            })
    }

    private fun scrollToElement(value: String?) {
        if (value.isNullOrBlank()) return

        val index = installedApps.indexOf(value)
        if (index < 0) return
        apv_application_list.smoothScrollToPosition(index)
    }

    data class AppDescriptor(
        val packageName: String,
        val appName: String
    ) {
        fun getIcon(packageManager: PackageManager): Drawable {
            return packageManager.getApplicationIcon(packageName)
        }
    }

    inner class Adapter(private val apps: List<AppDescriptor>) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        init {
            setHasStableIds(true)
        }

        override fun getItemId(position: Int): Long = position.toLong()
        override fun getItemCount(): Int = apps.size
        fun getItem(position: Int) = apps[position]
        fun getPosition(key: String) = apps.indexOfFirst { it.packageName == key }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.view_application_picker_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = apps[position]
            selectionTracker?.let {
                holder.bind(app, it.isSelected(app.packageName))
            }
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val appIcon = itemView.findViewById<ImageView>(R.id.img_application_icon)
            private val appName = itemView.findViewById<TextView>(R.id.txt_application_name)

            private var app: AppDescriptor? = null

            fun bind(app: AppDescriptor, isActivated: Boolean) {
                this.app = app
                val packageManager = itemView.context.packageManager
                val icon = app.getIcon(packageManager)
                appIcon.setImageDrawable(icon)

                appName.text = app.appName

                itemView.isActivated = isActivated
            }

            fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> =
                object : ItemDetailsLookup.ItemDetails<String>() {
                    override fun getPosition(): Int = adapterPosition
                    override fun getSelectionKey(): String? = app?.packageName
                    override fun inSelectionHotspot(e: MotionEvent) = true
                }
        }
    }

    private var selectionTracker: SelectionTracker<String>? = null

    class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<String>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y) ?: return null
            val holder = recyclerView.getChildViewHolder(view) as Adapter.ViewHolder
            return holder.getItemDetails()
        }
    }

    class MyItemKeyProvider(private val adapter: Adapter) : ItemKeyProvider<String>(SCOPE_CACHED) {
        override fun getKey(position: Int): String = adapter.getItem(position).packageName
        override fun getPosition(key: String): Int = adapter.getPosition(key)
    }
}