package com.catchingnow.icebox.sdk_client

/**
 * IceBox SDK
 */
object SdkImplement : IceBox() {
    private var authorizePendingIntent: Any? = null

    @androidx.annotation.WorkerThread
    @androidx.annotation.RequiresPermission(SDK_PERMISSION)
    fun setAppEnabledSettings(context: android.content.Context, enable: Boolean, vararg packageNames: String) {
        if (authorizePendingIntent == null) authorizePendingIntent = androidx.core.app.PendingIntentCompat.getBroadcast(context, 0x333, android.content.Intent(), android.app.PendingIntent.FLAG_CANCEL_CURRENT, false)
        context.contentResolver.call(SDK_PERMISSION, "set_enable", null, androidx.core.os.bundleOf("enable" to enable, "authorize" to authorizePendingIntent, "package_names" to packageNames, "user_handle" to android.os.Process.myUserHandle().hashCode()))
    }
}