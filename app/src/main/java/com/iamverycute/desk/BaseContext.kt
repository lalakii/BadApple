package com.iamverycute.desk

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import com.catchingnow.icebox.sdk_client.IceBox
import com.iamverycute.desk.adapter.ExAdapter
import com.iamverycute.desk.model.AppDetails
import com.iamverycute.iconpackmanager.IconPackManager
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

class BaseContext : Application() {

    val sortTAG = ">"
    val quickTAG = '#'
    val shownTAG = "+"
    var clockIndex = 0
    var isGrantIce: Boolean = false
    var iconPackName = "Pure Icon Pack"
    val list = mutableListOf<AppDetails>()
    lateinit var appConfig: Path
    lateinit var mHandler: Handler
    lateinit var ipm: IconPackManager
    lateinit var deskAdapter: ExAdapter
    private lateinit var customHidePackages: MutableList<String>
    lateinit var iconPacks: HashMap<String?, IconPackManager.IconPack>

    override fun onCreate() {
        super.onCreate() //init app config
        appConfig = Paths.get(getExternalFilesDir(null)?.absolutePath, "AppConfig.conf")
        if (!appConfig.exists()) {
            Files.createFile(appConfig)
        }
        mHandler = Handler(mainLooper)
        registerReceiver(PackageStateReceiver(), IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        })
        customHidePackages =
            mutableListOf(IceBox.PACKAGE_NAME, packageName, "com.iflytek.inputmethod","com.oneplus.member","com.heytap.music","com.finshell.wallet"
            ,"com.heytap.yoli","com.heytap.themestore","com.coloros.calendar","com.nearme.gamecenter")
        ipm = IconPackManager(this)
        iconPacks = ipm.isSupportedIconPacks()
        customHidePackages.addAll(iconPacks.values.map { it.getPackageName() }.toTypedArray())
        initAppListWithSort()
    }

    @Suppress("QueryPermissionsNeeded")
    private fun initAppListWithSort() { // --- read config
        val config = readConfig()
        val shownConfig = config.filter { it.startsWith(shownTAG) }
        val sortConfig = config.filter { it.startsWith(sortTAG) }
        val apps: List<ApplicationInfo> = if (Build.VERSION.SDK_INT > 32) {
            packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of((PackageManager.GET_META_DATA or PackageManager.MATCH_UNINSTALLED_PACKAGES).toLong()))
        } else {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA or PackageManager.MATCH_UNINSTALLED_PACKAGES)
        }
        apps
            .forEach {
                if ((packageManager.getLaunchIntentForPackage(it.packageName) == null && it.flags and ApplicationInfo.FLAG_SYSTEM == 1) or customHidePackages.contains(
                        it.packageName
                    )
                ) return@forEach
                val model = AppDetails(packageManager, it)
                model.appShown = shownConfig.contains(shownTAG + it.packageName)
                if (sortConfig.isNotEmpty()) {
                    val sort = sortConfig.indexOf(sortTAG + it.packageName)
                    if (sort != -1) model.sort = sort
                }
                list.add(model)
            }
        if (sortConfig.isNotEmpty()) {
            list.sortBy { it.sort }
        }
    }

    fun checkAppExists(packageName: String): Boolean {
        return try {
            packageManager.getApplicationEnabledSetting(packageName)
            true
        } catch (ignored: IllegalArgumentException) {
            false
        }
    }

    fun startNewActivity(action: String, uri: Uri?) {
        val intent = getIntent(action)
        intent.data = uri
        startActivity(intent)
    }

    fun readConfig(): MutableList<String> = Files.readAllLines(appConfig)

    fun writeConfig(list: List<String>): Unit = run { Files.write(appConfig, list) }

    fun startNewActivity(intent: Intent?) = startActivity(getIntent(intent))

    private fun getIntent(intentOrAction: Any?): Intent {
        return if (intentOrAction is String) {
            Intent(intentOrAction)
        } else {
            intentOrAction as Intent
        }.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    inner class PackageStateReceiver : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            val packageName = intent.dataString!!.substringAfter(':')
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    if (customHidePackages.contains(packageName) || packageManager.getLaunchIntentForPackage(
                            packageName
                        ) == null
                    ) return
                    if (deskAdapter.list.none { packageName == it.info.packageName }) {
                        val appInfo: ApplicationInfo = if (Build.VERSION.SDK_INT > 32) {
                            packageManager.getApplicationInfo(
                                packageName,
                                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                            )
                        } else {
                            packageManager.getApplicationInfo(
                                packageName, PackageManager.GET_META_DATA
                            )
                        }
                        val model = AppDetails(
                            packageManager, appInfo

                        )
                        model.sort = 999
                        deskAdapter.itemAdd(model)
                    }
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    if (!checkAppExists(packageName)) deskAdapter.removeItemByPackageName(
                        packageName
                    )
                }
            }
        }
    }
}