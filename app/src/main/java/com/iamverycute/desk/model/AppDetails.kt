package com.iamverycute.desk.model

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.iamverycute.desk.BR
import com.iamverycute.desk.adapter.ExHolder

class AppDetails(val themeModel: ThemeDetails?) : BaseObservable() {
    class ThemeDetails(val themeId: Int, val themeName: String)
    constructor(pm: PackageManager, info: ApplicationInfo) : this(null) {
        this.info = info
        appName = pm.getApplicationLabel(info) as String
        appIcon = info.loadIcon(pm)
    }

    var sort: Int = 999
    lateinit var info: ApplicationInfo
    lateinit var util: ExHolder.AnimaUtil
    @Bindable var appIcon: Drawable? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.appIcon)
        }
    @Bindable var appName: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.appName)
        }
    @Bindable var appShown: Boolean? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.appShown)
        }
}