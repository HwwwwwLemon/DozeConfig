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


import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast


class ToastUtil {
    private var toast: Toast? = null

    fun init(ctx: Context, message: String): ToastUtil {
        default(ctx, message)
        return this
    }

    fun init(ctx: Context, res: Int): ToastUtil {
        default(ctx, ctx.getString(res))
        return this
    }

    @SuppressLint("ShowToast")
    private fun default(ctx: Context, message: String): ToastUtil {
        toast = Toast.makeText(ctx, message, Toast.LENGTH_SHORT)
        val view: LinearLayout = toast?.view as LinearLayout
        view.gravity = Gravity.CENTER
        val tv = view.findViewById(R.id.message) as TextView
        tv.setBackgroundColor(Color.TRANSPARENT)
        tv.gravity = Gravity.CENTER
        return this
    }

    fun indefinite(duration: Long): ToastUtil {
        Handler().postDelayed({ toast?.cancel() }, duration)
        return this
    }

    fun show() {
        toast?.show()
    }
}