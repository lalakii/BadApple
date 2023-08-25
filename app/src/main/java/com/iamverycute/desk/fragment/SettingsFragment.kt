package com.iamverycute.desk.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.iamverycute.desk.BaseContext
import com.iamverycute.desk.HomeActivity
import com.iamverycute.desk.R
import com.iamverycute.desk.adapter.ExAdapter
import com.iamverycute.desk.databinding.SettingsTabBinding
import com.iamverycute.desk.model.AppDetails

class SettingsFragment(private val appContext: BaseContext, private val activity: HomeActivity) : Fragment() {

    var tabPosition = ObservableField<Int>()
    lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var binding: SettingsTabBinding
    private var themeIcon: Drawable? = null
    private lateinit var tabLayout: TabLayout
    lateinit var adapter: ExAdapter
    lateinit var adapterFragment: AdapterFragment

    val callBack = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            tabPosition.set(position)
            tabLayout.getTabAt(position)?.select()
            super.onPageSelected(position)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        activity.window.insetsController?.show(WindowInsets.Type.statusBars())
        val list = activity.sysList
        adapter = ExAdapter(appContext, this, R.layout.settings_item_sys, list!!) //init theme info and selected
        val themeList = mutableListOf(AppDetails(AppDetails.ThemeDetails(1, "默认")), AppDetails(
            AppDetails.ThemeDetails(2, "极简")
        ), AppDetails(AppDetails.ThemeDetails(3, "不显示")))
        if (appContext.clockIndex < themeList.count()) {
            themeList[appContext.clockIndex].appShown = true
        } // -- Fragments Add
        binding = DataBindingUtil.inflate(inflater, R.layout.settings_tab, container, false)
        binding.context = this
        adapterFragment = AdapterFragment(ExAdapter(appContext, this, R.layout.settings_item_theme, themeList), R.layout.recycler)
        viewPagerAdapter = ViewPagerAdapter(activity.supportFragmentManager, lifecycle, listOf(AdapterFragment(adapter, R.layout.recycler), adapterFragment, AboutFragment(appContext))) // create tabLayout
        tabLayout = binding.tabLayout
        themeIcon = AppCompatResources.getDrawable(activity, android.R.drawable.sym_def_app_icon)
        list.find { it.info.packageName.contains("themestore") }?.run { themeIcon = info.loadIcon(activity.packageManager) }
        tabLayout.addTab(tabLayout.newTab().setText(R.string.AppShown).setIcon(android.R.drawable.sym_def_app_icon))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.appTheme).setIcon(themeIcon))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.about).setIcon(android.R.drawable.ic_dialog_info))
        return binding.root
    }

    override fun onDestroy() {
        adapter.list.filter { (it.appShown!! && appContext.deskAdapter.list.none { list -> list.info.packageName == it.info.packageName }) || (!it.appShown!! && appContext.deskAdapter.list.any { list -> list.info.packageName == it.info.packageName }) }.forEach {
            if (it.appShown!!) appContext.deskAdapter.itemAdd(it)
            else appContext.deskAdapter.removeItemByPackageName(it.info.packageName)
        }
        activity.onWindowFocusChanged(true)
        activity.window.insetsController?.hide(WindowInsets.Type.statusBars())
        super.onDestroy()
    }

    inner class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, private val fragments: List<Fragment>) : FragmentStateAdapter(fragmentManager, lifecycle), TabLayout.OnTabSelectedListener {
        override fun createFragment(position: Int) = fragments[position]
        override fun getItemCount() = fragments.size
        override fun onTabSelected(tab: TabLayout.Tab?) = tabPosition.set(tab?.position)
        override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
        override fun onTabReselected(tab: TabLayout.Tab?) = Unit
    }
}