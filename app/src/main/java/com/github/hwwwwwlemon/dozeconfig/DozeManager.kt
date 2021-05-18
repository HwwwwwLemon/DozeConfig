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
package com.github.hwwwwwlemon.dozeconfig

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.os.Process
import android.util.Log
import androidx.core.app.ActivityCompat
import com.github.hwwwwwlemon.dozeconfig.utils.Utils
import java.io.*


object DozeManager {
    private const val REQUEST_EXTERNAL_STORAGE = 1
    const val DOZE_FILE_PATH = "Android/doze.conf"
    const val DOZE_LOG_PATH = "Android/doze.log"
    private val SDCARD_PATH = File(Environment.getExternalStorageDirectory().path)
    private val PERMISSIONS_STORAGE = arrayOf(
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE"
    )


    fun checkDozeFile(ctx: Context): Boolean {
        try {
            if (checkPermission(ctx)) {
                val file = File(SDCARD_PATH, DOZE_FILE_PATH)
                return file.exists()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    fun deleteFile(path: String, ctx: Context) {
        if (checkPermission(ctx)) {
            val file = File(SDCARD_PATH, path)
            if (!file.exists()) {
                throw FileNotFoundException(file.absolutePath)
            } else {
                file.delete()
            }
        }

    }

    fun writeFile(path: String, content: String, ctx: Context) {
        if (checkPermission(ctx)) {
            val file = File(SDCARD_PATH, path)
            if (!file.exists()) {
                try {
                    file.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            var fileOutputStream: FileOutputStream? = null
            try {
                fileOutputStream = FileOutputStream(file)
                fileOutputStream.write(content.toByteArray())
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fileOutputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun readFile(path: String, ctx: Context, suffix: String = ""): String {
        val result = StringBuilder()
        if (checkPermission(ctx)) {
            var inputStream: InputStream? = null
            var reader: Reader? = null
            var bufferedReader: BufferedReader? = null
            try {
                val file = File(SDCARD_PATH, path)
                if (!file.exists()) {
                    throw FileNotFoundException(file.absolutePath)
                }
                inputStream = FileInputStream(file)
                reader = InputStreamReader(inputStream)
                bufferedReader = BufferedReader(reader)
                var temp: String?
                while (bufferedReader.readLine().also { temp = it } != null) {
                    result.append(temp).append(suffix)
                }
            } catch (e: Exception) {
                throw e
            } finally {
                try {
                    bufferedReader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                try {
                    reader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return result.toString()
    }

    fun checkPermission(ctx: Context): Boolean {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(ctx as Activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            Utils.showToast(ctx, ctx.getString(R.string.permission_prompt))
            Log.e(
                "DozeManager", "The external storage read and write permissions are failed obtained!" +
                        "${
                            ActivityCompat.checkSelfPermission(
                                ctx,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                        }"
            )
            return ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            Log.d("DozeManager", "The external storage read and write permissions are successfully obtained!")
        }
        return true
    }

    private fun restartApp(ctx: Context) {
        val intent = Intent(ctx, MainActivity::class.java)
        intent.putExtra("PackageName", ctx.packageName)
        intent.putExtra("Delayed", 1000)
        ctx.startService(intent)
        Log.e("DozeManager", "Restart")
        Process.killProcess(Process.myPid())

    }
}