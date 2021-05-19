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
import com.github.hwwwwwlemon.dozeconfig.DozeManager
import com.github.hwwwwwlemon.dozeconfig.adapter.ApplicationAdapter
import kotlinx.coroutines.*

object DozeFileUtil {
    val checkAppList = mutableListOf<ApplicationAdapter.Apps>()
    var dozeApps = mutableListOf<String>()
    private val controlScope = CoroutineScope(Dispatchers.Default)
    fun loadDozeFile(ctx: Context): Boolean {

        try {
            checkAppList.clear()
            dozeApps.clear()
            val dozeContent = DozeManager.readFile(DozeManager.DOZE_FILE_PATH, ctx, "\n")
            val pm = ctx.packageManager
            Regex("[a-z0-9]{1,24}\\.(.+)").findAll(dozeContent).forEach {
                dozeApps.add(it.value.trim())
            }
            for (i in dozeApps) {
                try {
                    val app = pm.getApplicationInfo(i, 0)
                    val flag = if ((app.flags and ApplicationInfo.FLAG_SYSTEM) == 0) 1 else 2
                    checkAppList.add(
                        ApplicationAdapter.Apps(
                            app,
                            i,
                            pm.getApplicationLabel(app).toString(),
                            flag,
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    continue
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }


    fun saveDozeFile(ctx: Context): Boolean {
        return runBlocking {
            controlScope.launch {
                val sb = StringBuilder("whitelist=\"\n")
                checkAppList.forEach {
                    sb.append("+").append(it.packageName).append("\n")
                }
                val result = sb.append("\"").toString()
                DozeManager.writeFile(DozeManager.DOZE_FILE_PATH, result, ctx)
            }
            delay(200L)
            return@runBlocking DozeManager.checkDozeFile(ctx)
        }
    }
}