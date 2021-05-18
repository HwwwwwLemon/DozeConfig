/*
 * MIT License (MIT)
 *
 * Copyright © 2021  Hwwwww
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
 * associated documentation files (the “Software”), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial 
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */
package com.github.hwwwwwlemon.dozeconfig.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Log
import android.util.TypedValue
import java.io.DataOutputStream


object Utils {

    fun refreshDozeWhitelist(ctx: Context): Boolean {
        val sb = StringBuffer("dumpsys deviceidle whitelist ")
        val app = getAppList(ctx)
        app.forEach {
            sb.append("-").append(it.packageName).append(" ")
        }
        DozeFileUtil.checkAppList.forEach {
            sb.append("+").append(it.packageName).append(" ")
        }
        val suProcess = Runtime.getRuntime().exec("su")
        val os = DataOutputStream(suProcess.outputStream)
        os.writeBytes(sb.toString())
        os.flush()
        os.close()
        val exitValue = suProcess.waitFor()
        Log.e("code", exitValue.toString())
        return exitValue == 0
    }


    fun isDarkMode(ctx: Context): Boolean {
        val mode = ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    fun getColorPrimary(ctx: Context): Int {
        val typedValue = TypedValue()
        ctx.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        return typedValue.data
    }

    fun getAppList(ctx: Context): MutableList<ApplicationInfo> {
        val pm: PackageManager = ctx.packageManager
        return pm.getInstalledApplications(0)

    }

    fun showToast(ctx: Context, content: String, duration: Int = 3000) {
        ToastUtil().init(ctx, content).indefinite(duration.toLong()).show()
    }

    fun showToast(ctx: Context, res: Int, duration: Int = 3000) {
        ToastUtil().init(ctx, res).indefinite(duration.toLong()).show()
    }
}