package com.iamverycute.desk

import android.content.DialogInterface
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GestureDetectorCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentActivity
import com.catchingnow.icebox.sdk_client.IceBox
import com.iamverycute.desk.adapter.ExAdapter
import com.iamverycute.desk.databinding.DeskBinding
import com.iamverycute.desk.fragment.SettingsFragment
import com.iamverycute.desk.model.AppDetails
import com.iamverycute.iconpackmanager.IconPackManager
import net.crosp.libs.android.circletimeview.CircleTimeView

class HomeActivity : FragmentActivity(), View.OnTouchListener, CircleTimeView.LapDataProvider,
    GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener,
    View.OnLongClickListener, View.OnClickListener {
    private var week = ""
    private val iconScale = 0.9f
    val time = ObservableField<Long>()
    lateinit var appContext: BaseContext
    private val requestCodeIce = 0x233
    private lateinit var binding: DeskBinding
    var sysList: MutableList<AppDetails>? = null
    private var settings: SettingsFragment? = null
    private lateinit var list: MutableList<AppDetails>
    private lateinit var iconPack: IconPackManager.IconPack
    private lateinit var detector: GestureDetectorCompat
    private var alarmClock: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        appContext = applicationContext as BaseContext
        onBackPressedDispatcher.addCallback(this, onBackPress)
        if (!appContext.isGrantIce) {
            requestPermissions(arrayOf(IceBox.SDK_PERMISSION), requestCodeIce)
        }
        list = appContext.list
        val clockDetails = list.find { it.info.packageName.contains("clock") }
        if (clockDetails != null) {
            alarmClock = clockDetails.info.packageName
            list.remove(clockDetails)
        }
        appContext.deskAdapter = ExAdapter(
            appContext,
            this,
            R.layout.item_desk,
            list.filter { it.appShown == true || it.info.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                .toMutableList()
        )
        binding = DataBindingUtil.setContentView(this, R.layout.desk)
        binding.context = this //init user event
        detector = GestureDetectorCompat(this, this)
        detector.setOnDoubleTapListener(this)
        addRules(appContext.ipm)
        initIcons(null) //init all icons
        initSysIcons()
        binding.parentContainer.setPadding(0, getStatusBarHeight(), 0, 0)
        if (resources.displayMetrics.widthPixels > resources.displayMetrics.heightPixels) {
            val getConfig = Configuration()
            getConfig.orientation = Configuration.ORIENTATION_LANDSCAPE
            onConfigurationChanged(getConfig)
        }
        if (!appContext.checkAppExists(IceBox.PACKAGE_NAME)) {
            binding.icebox.visibility = View.GONE
        }
    }

    @Suppress("InternalInsetResource", "DiscouragedApi")
    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId)
        }
        return 0
    }

    fun initIcons(info: AppDetails?) {
        val tempIconPack = appContext.iconPacks.values.find { it.name == appContext.iconPackName }
        if (tempIconPack != null) {
            iconPack = tempIconPack
            val packageNames =
                appContext.deskAdapter.list.filter { packageManager.getLaunchIntentForPackage(it.info.packageName) == null }
                    .map { it.info.packageName }.toTypedArray()
            appContext.mHandler.post {
                IceBox.setAppEnabledSettings(this, true, *packageNames)
                runOnUiThread {
                    appContext.deskAdapter.list.forEach {
                        if (info != null && info != it) return@forEach
                        loadAppIcon(it)
                    }
                    appContext.mHandler.post {
                        IceBox.setAppEnabledSettings(this, false, *packageNames)
                    }
                }
            }
        }
    }

    private fun initSysIcons() {
        sysList = appContext.list.filter { it.info.flags and ApplicationInfo.FLAG_SYSTEM == 1 }
            .toMutableList()
        val tempIconPack = appContext.iconPacks.values.find { it.name == appContext.iconPackName }
        if (tempIconPack != null) {
            iconPack = tempIconPack
            sysList?.forEach { loadAppIcon(it) }
        }
    }

    private fun loadAppIcon(m: AppDetails) {
        if (packageManager.getLaunchIntentForPackage(m.info.packageName) != null) {
            val icon = iconPack.loadIcon(m.info)
            if (icon != null) {
                if (m.appIcon != icon) m.appIcon = icon
            } else {
                m.appIcon =
                    iconPack.iconCutCircle(m.info.loadIcon(packageManager).toBitmap(), iconScale)
            }
        }
    }

    /**
     * custom rules icons
     *
     * key: packageName or keywords
     * value: componeName or keywords
     */
    @Suppress("SpellCheckingInspection")
    private fun addRules(ipm: IconPackManager) {
        ipm.addRule("via", "browser").addRule("mobimail", "com.netease.mobimail")
            .addRule("mail189", "com.tencent.androidqqmail")
            .addRule("heytap.market", "com.oppo.market")
            .addRule("updater", "com.meizu.flyme.update")
            .addRule("opencamera", "com.motorola.camera.Camera")
            .addRule("unicom", "com.sinovatech.unicom.ui")
            .addRule("bjchuhai", "pro.onevpn.onevpnandroid")
            .addRule("gallery", "com.meizu.media.gallery")
            .addRule("com.icbc", "com.icbc/com.icbc.activity")
            .addRule("tv.danmaku", "com.bilibili.app.in/tv.danmaku.bili.ui")
            .addRule("com.tmri.app.main", "com.tmri.app.main/com.tmri.app.ui.activity")
            .addRule("cn.wsds", "com.xiaomi.gamecenter").addRule("dialer", "com.android.phone")
            .addRule("contact", "com.google.android.contacts")
            .addRule("com.coloros.filemanager", "com.tencent.FileManager")
    }

    override fun onResume() {
        appContext.isGrantIce =
            checkSelfPermission(IceBox.SDK_PERMISSION) == PackageManager.PERMISSION_GRANTED
        super.onResume()
    }

    override fun onPause() {
        settingsDismiss()
        super.onPause()
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent) = detector.onTouchEvent(event)
    override fun onDown(e: MotionEvent) = true
    override fun onLongPress(e: MotionEvent): Unit = toggleDrag()
    override fun onShowPress(e: MotionEvent) = Unit
    override fun onSingleTapUp(e: MotionEvent) = false
    override fun onDoubleTapEvent(e: MotionEvent) = false
    override fun onScroll(
        p0: MotionEvent?,
        e1: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean = false

    override fun onFling(
        p0: MotionEvent?,
        e1: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean = false

    override fun getLapLabelText(currentTimeInSeconds: Long) = week
    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        if (alarmClock == null) {
            alarmClock = sysList?.find { it.info.packageName.contains("clock") }?.info?.packageName
        }
        appContext.deskAdapter.iceStart(alarmClock!!)
        return false
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        settings = SettingsFragment(appContext, this)
        supportFragmentManager.beginTransaction().add(R.id.settings_container, settings!!)
            .commit() //  startActivity(Intent(this, SettingsActivity::class.java))
        binding.settingsContainer.visibility = View.VISIBLE
        return false
    }

    override fun onLongClick(v: View): Boolean {
        val temp = list.map { it.info.loadLabel(packageManager) as String }.toTypedArray()
        var index = temp.indexOf((v as TextView).text)
        if (index < 0) index = 0
        AlertDialog.Builder(this).setTitle(R.string.choose)
            .setSingleChoiceItems(temp, index) { _: DialogInterface?, which: Int -> index = which }
            .setPositiveButton(R.string.choose) { _, _ ->
                val newConfig =
                    appContext.readConfig().filter { !it.startsWith(appContext.quickTAG) }
                        .toMutableList()
                newConfig.add(appContext.quickTAG + temp[index])
                appContext.writeConfig(newConfig)
            }.show()
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == requestCodeIce) {
            appContext.isGrantIce = grantResults.contains(PackageManager.PERMISSION_GRANTED)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @Suppress("SwitchIntDef")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                binding.timeParent.layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT
                val recycleParams = binding.recyclerView.layoutParams as RelativeLayout.LayoutParams
                recycleParams.removeRule(RelativeLayout.RIGHT_OF)
                recycleParams.addRule(RelativeLayout.BELOW, R.id.time_parent)
                binding.parentContainer.setPadding(0, getStatusBarHeight(), 0, 0)
                val quickParams = binding.quickParent.layoutParams as RelativeLayout.LayoutParams
                quickParams.addRule(RelativeLayout.ALIGN_PARENT_END)
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                val metrics = resources.displayMetrics
                binding.timeParent.layoutParams.width = metrics.widthPixels / 2
                val recycleParams = binding.recyclerView.layoutParams as RelativeLayout.LayoutParams
                recycleParams.addRule(RelativeLayout.RIGHT_OF, R.id.time_parent)
                recycleParams.removeRule(RelativeLayout.BELOW)
                binding.parentContainer.setPadding(0, 0, 0, 0)
                val quickParams = binding.quickParent.layoutParams as RelativeLayout.LayoutParams
                quickParams.removeRule(RelativeLayout.ALIGN_PARENT_END)
            }
        }
    }

    fun onTextChange(s: CharSequence) {
        val timeArray = s.split(":")
        time.set(timeArray[0].toInt() * 60L + timeArray[1].toInt())
        val tempWeek = timeArray[2]
        week = if (tempWeek != week) tempWeek else week
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        appContext.readConfig().find { it.startsWith(appContext.quickTAG) }.run {
            if (!this.isNullOrEmpty()) binding.gamer.text = this.trimStart(appContext.quickTAG)
        }
        if (hasFocus) {
            when (appContext.clockIndex) {
                0 -> {
                    binding.circleTimerView.visibility = View.VISIBLE
                    binding.simpleTimeView.visibility = View.GONE
                    binding.noneTimeView.visibility = View.GONE
                }

                1 -> {
                    binding.circleTimerView.visibility = View.GONE
                    binding.simpleTimeView.visibility = View.VISIBLE
                    binding.noneTimeView.visibility = View.GONE
                }

                2 -> {
                    binding.circleTimerView.visibility = View.GONE
                    binding.simpleTimeView.visibility = View.GONE
                    binding.noneTimeView.visibility = View.VISIBLE
                }
            }
        } else {
            if (!appContext.deskAdapter.state) toggleDrag()
        }
        super.onWindowFocusChanged(hasFocus)
    }

    private val onBackPress = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!appContext.deskAdapter.state) toggleDrag()
            settingsDismiss()
            return
        }
    }

    private fun settingsDismiss() {
        if (settings != null && settings!!.isVisible) {
            binding.settingsContainer.visibility = View.GONE
            supportFragmentManager.beginTransaction().remove(settings!!).commit()
        }
    }

    private fun toggleDrag(): Unit = run { appContext.deskAdapter.toggleDragMode() }

    override fun onClick(v: View) {
        if (v is TextView) when (v.id) {
            R.id.icebox -> {
                if (appContext.checkAppExists(IceBox.PACKAGE_NAME)) {
                    startActivity(packageManager.getLaunchIntentForPackage(IceBox.PACKAGE_NAME))
                }
            }

            R.id.gamer -> list.find {
                it.info.loadLabel(packageManager) == binding.gamer.text
            }?.run {
                appContext.deskAdapter.iceStart(info.packageName)
            }
        }
    }
}