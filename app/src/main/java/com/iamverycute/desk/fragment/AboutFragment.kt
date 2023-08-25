package com.iamverycute.desk.fragment

import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.iamverycute.desk.BaseContext
import com.iamverycute.desk.HomeActivity
import com.iamverycute.desk.R
import com.iamverycute.desk.databinding.SettingsItemAboutBinding
import com.iamverycute.iconpackmanager.IconPackManager
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.CRC32
import kotlin.io.path.absolutePathString

class AboutFragment(private val appContext: BaseContext) : Fragment(R.layout.settings_item_about),
    View.OnClickListener, OnItemSelectedListener {

    var defaultPackageName: String? = null
    private val pm: PackageManager = appContext.packageManager
    var adapter: ArrayAdapter<String>? = null
    var selectIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val iconPacks = mutableListOf<String>()
        IconPackManager(appContext).isSupportedIconPacks().values.forEach { iconPacks.add(it.name) }
        adapter = ArrayAdapter(appContext, R.layout.settings_item_icon_pack, iconPacks)
        selectIndex = iconPacks.indexOfFirst { it == appContext.iconPackName }
        val info: ResolveInfo? = if (Build.VERSION.SDK_INT > 32) {
            pm.resolveActivity(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            pm.resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0)
        }
        val label: String
        if (info?.activityInfo?.packageName.equals("android")) {
            label = "未设置"
        } else {
            defaultPackageName = info?.activityInfo!!.packageName
            label = info.loadLabel(pm).toString()
        }
        val binding = SettingsItemAboutBinding.bind(view)
        binding.context = this
        val crc32 = CRC32()
        crc32.update(Files.readAllBytes(Paths.get(appContext.packageResourcePath)))
        val crc32String = java.lang.Long.toHexString(crc32.value)
        binding.about.text = Html.fromHtml(getString(R.string.me), Html.FROM_HTML_MODE_COMPACT)
        binding.details.text = String.format(
            "此软件相关信息\n\nAPP包名:\t%s\n默认桌面:\t%s\n冰箱权限:\t%s\nCRC32:\t%s\n配置文件路径:\t%s%s",
            appContext.packageName,
            label,
            if (appContext.isGrantIce) getString(R.string.yes) else getString(R.string.no),
            crc32String,
            appContext.appConfig.absolutePathString(),
            getString(R.string.ver)
        )
    }

    override fun onClick(v: View) {
        val builder = AlertDialog.Builder(requireActivity())
        when (v.id) {
            R.id.opensource -> {
                val list = resources.getStringArray(R.array.links)
                builder.setTitle(R.string.opensource)
                    .setItems(list) { _: DialogInterface?, which: Int ->
                        startActivity(
                            Intent.getIntentOld(list[which])
                        )
                    }.show()
            }

            R.id.feedback -> builder.setMessage(R.string.tips)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    appContext.startNewActivity(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + appContext.packageName)
                    )
                }.setNegativeButton(R.string.cancel, null).show()

            R.id.default_launcher -> {
                val comp = ComponentName(appContext, "com.iamverycute.desk.FakeMe")
                pm.setComponentEnabledSetting(
                    comp,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                appContext.startActivity(
                    Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                pm.setComponentEnabledSetting(
                    comp,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) = run {
        appContext.iconPackName = p0?.getItemAtPosition(p2).toString()
        (requireActivity() as HomeActivity).initIcons(null)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) = Unit
}