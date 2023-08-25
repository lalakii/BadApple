package com.catchingnow.icebox.sdk_client

/**
 * IceBox SDK
 */
open class IceBox {
    companion object {
        const val PACKAGE_NAME = "com.catchingnow.icebox"
        const val SDK_PERMISSION = "$PACKAGE_NAME.SDK"

        /**
         * 冻结解冻 App
         * PS: 冰箱并不是所有的引擎都支持多用户，所以暂时禁用掉多用户功能。
         *
         * @param context      context
         * @param packageNames 包名
         * @param enable       true for 解冻，false for 冻结
         */
        @androidx.annotation.WorkerThread
        @androidx.annotation.RequiresPermission(SDK_PERMISSION)
        fun setAppEnabledSettings(context: android.content.Context, enable: Boolean, vararg packageNames: String) = SdkImplement.setAppEnabledSettings(context, enable, *packageNames)
    }
}