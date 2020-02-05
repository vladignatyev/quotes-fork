package com.quote.mosaic.core.ext

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.quote.mosaic.R
import com.quote.mosaic.data.manager.UserManager

object UserAgent {

    fun generateSupportText(
        context: Context, userManager: UserManager
    ): String {

        val userSession = userManager.getSession()
        val trimmedSession = userSession.substring(0, userSession.length - 16)

        return "" +
                "-----------------------\n" +
                "\n" +
                "${context.getString(R.string.profile_label_email_title)}\n" +
                "\n" +
                "${getUser(context)} | $trimmedSession | \n" +
                "\n" +
                "-----------------------\n" +
                "\n" +
                "${context.getString(R.string.profile_label_email_write)}\n" +
                "\n"
    }

    private fun getUser(context: Context): String {
        with(context.packageManager) {
            val versionName = try {
                getPackageInfo(context.packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                "nameNotFound"
            }
            val versionCode = try {
                getPackageInfo(context.packageName, 0).versionCode.toString()
            } catch (e: PackageManager.NameNotFoundException) {
                "versionCodeNotFound"
            }

            val model = Build.MODEL
            val version = Build.VERSION.SDK_INT
            val versionRelease = Build.VERSION.RELEASE

            return "$versionName($versionCode)/$model SDK $version/Android $versionRelease"
        }
    }
}