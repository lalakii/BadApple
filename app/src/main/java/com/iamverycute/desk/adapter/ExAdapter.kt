package com.iamverycute.desk.adapter

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.catchingnow.icebox.sdk_client.IceBox
import com.iamverycute.desk.BaseContext
import com.iamverycute.desk.HomeActivity
import com.iamverycute.desk.model.AppDetails

class ExAdapter(val appContext: BaseContext, private val activity: Any, private val layoutId: Int, val list: MutableList<AppDetails>) : RecyclerView.Adapter<ExHolder>() {

    private val helper = ExTouchHelper(this)
    var state = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ExHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutId, parent, false), activity)
    override fun onBindViewHolder(holder: ExHolder, position: Int) = holder.bind(list[position])
    override fun getItemId(position: Int) = position.toLong()
    override fun getItemCount() = list.size
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        helper.attachToView(recyclerView)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onViewAttachedToWindow(holder: ExHolder) {
        super.onViewAttachedToWindow(holder)
        if (activity is HomeActivity) holder.util.setAnimationState(!state)
    }

    fun click(view: View, m: AppDetails) {
        if (!state) {
            if (view is ImageView) {
                appContext.startNewActivity(Intent.ACTION_DELETE, Uri.fromParts("package", m.info.packageName, null))
            }
            return
        }
        iceStart(m.info.packageName)
    }

    fun swap(source: Int, target: Int) = run { list[source] = list[target].also { list[target] = list[source] } }

    fun removeItemByPackageName(packageName: String) {
        list.removeIf { item: AppDetails ->
            if (item.info.packageName == packageName) {
                notifyItemRemoved(list.indexOf(item))
                return@removeIf true
            }
            false
        }
    }

    fun itemAdd(info: AppDetails) {
        list.add(info)
        (activity as? HomeActivity)?.initIcons(info)
        notifyItemInserted(list.indexOf(info))
    }

    fun iceStart(packageName: String) {
        val getLaunchIntent = appContext.packageManager.getLaunchIntentForPackage(packageName)
        if (getLaunchIntent == null && appContext.isGrantIce) {
            if (appContext.checkAppExists(packageName)) {
                appContext.mHandler.post {
                    IceBox.setAppEnabledSettings(appContext, true, packageName)
                    appContext.startNewActivity(appContext.packageManager.getLaunchIntentForPackage(packageName))
                }
            } else removeItemByPackageName(packageName)
        } else appContext.startNewActivity(getLaunchIntent)
    }

    fun toggleDragMode(): Boolean {
        helper.setDefaultDragDirs(if (state) (ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN) else 0)
        list.forEach { it.util.setAnimationState(state) }
        state = !state
        return state
    }

    fun longClick(m: AppDetails): Boolean {
        if (state) {
            appContext.startNewActivity(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", m.info.packageName, null))
        }
        return true
    }

    fun itemCheck(m: AppDetails) {
        if (m.themeModel != null) {
            list.withIndex().forEach {
                if (m == it.value) {
                    it.value.appShown = true
                    appContext.clockIndex = it.index
                } else it.value.appShown = false
            }
            return
        }
        m.appShown = m.appShown != true
        list.find { it.info == m.info }?.appShown = m.appShown
        val config = appContext.readConfig()
        val tagItem = appContext.shownTAG + m.info.packageName
        config.removeAll { it == tagItem }.run {
            if (!this) config.add(tagItem)
        }
        appContext.writeConfig(config)
    }
}