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

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager


class MyPreferences {

    private val TAG = "MyPreferences"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @SuppressLint("CommitPrefEdits")
    constructor(ctx: Context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx)
        editor = sharedPreferences.edit()
    }

    @SuppressLint("CommitPrefEdits")
    constructor(ctx: Context, fileName:String){
        sharedPreferences = ctx.getSharedPreferences(fileName,Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }


    fun save(key: String, defValue: Any) {
        when (defValue) {
            is Int -> editor.putInt(key, defValue)
            is Float -> editor.putFloat(key, defValue)
            is String -> editor.putString(key, defValue)
            is Boolean -> editor.putBoolean(key, defValue)
            is Long -> editor.putLong(key, defValue)
        }
        editor.apply()

    }

    fun get(key: String, defValue: Any): Any {
        return when (defValue) {
            is Int -> sharedPreferences.getInt(key, defValue)
            is Float -> sharedPreferences.getFloat(key, defValue)
            is String -> sharedPreferences.getString(key, defValue) as Any
            is Boolean -> sharedPreferences.getBoolean(key, defValue)
            is Long -> sharedPreferences.getLong(key, defValue)
            else -> ""
        }
    }


    fun removeUserInfo(key: String) {
        editor.remove(key)
        editor.apply()
    }


    fun clearUserInfo() {
        editor.clear()
        editor.apply()
    }
}