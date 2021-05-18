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

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.Window
import android.view.WindowManager
import androidx.annotation.FloatRange
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import com.github.hwwwwwlemon.dozeconfig.R
import java.util.regex.Pattern


/**
 * 状态栏透明
 * Created by scwang on 2016/10/26.
 */
object StatusBarUtil {
    var DEFAULT_COLOR = 0
    var DEFAULT_ALPHA = 0f //Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 0.2f : 0.3f;
    private const val MIN_API = 19


    fun immersive(
        activity: Activity,
        color: Int = DEFAULT_COLOR,
        @FloatRange(from = 0.0, to = 1.0) alpha: Float = DEFAULT_ALPHA
    ) {
        immersive(activity.window, color, alpha)
    }

    fun immersive(activity: Activity, color: Int) {
        immersive(activity.window, color, 1f)
    }

    fun immersive(window: Window, color: Int) {
        immersive(window, color, 1f)
    }


    fun immersive(
        window: Window,
        color: Int = DEFAULT_COLOR,
        @FloatRange(from = 0.0, to = 1.0) alpha: Float = DEFAULT_ALPHA
    ) {
        if (Build.VERSION.SDK_INT >= 21) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = mixtureColor(color, alpha)
            var systemUiVisibility = window.decorView.systemUiVisibility
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = systemUiVisibility
        } else if (Build.VERSION.SDK_INT >= 19) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            setTranslucentView(window.decorView as ViewGroup, color, alpha)
        } else if (Build.VERSION.SDK_INT >= MIN_API && Build.VERSION.SDK_INT > 16) {
            var systemUiVisibility = window.decorView.systemUiVisibility
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = systemUiVisibility
        }
    }

    fun setColor(activity: Activity, color: Int) {
        if (Build.VERSION.SDK_INT >= 21) {
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
        }
    }

    fun setGradientColor(@NonNull activity: Activity, view: View?) {
        val decorView = activity.window.decorView as ViewGroup
        val fakeStatusBarView = decorView.findViewById<View>(R.id.custom)
        if (fakeStatusBarView != null) {
            decorView.removeView(fakeStatusBarView)
        }
        setTranslucentView(decorView, DEFAULT_COLOR, DEFAULT_ALPHA)
        immersive(activity)
    }


    fun darkMode(activity: Activity, dark: Boolean) {
        if (isFlyme4Later) {
            darkModeForFlyme4(activity.window, dark)
        } else if (isMIUI6Later) {
            darkModeForMIUI6(activity.window, dark)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            darkModeForM(activity.window, dark)
        }
    }

    fun lightMode(activity: Activity) {
        val window = activity.window
        if (isFlyme4Later) {
            darkModeForFlyme4(window, false)
        } else if (isMIUI6Later) {
            darkModeForMIUI6(window, false)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            darkModeForM(window, false)
        }
    }

    /** 设置状态栏darkMode,字体颜色及icon变黑(目前支持MIUI6以上,Flyme4以上,Android M以上)  */
    fun darkMode(activity: Activity) {
        darkMode(activity.window, DEFAULT_COLOR, DEFAULT_ALPHA)
    }

    fun darkMode(activity: Activity, color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        darkMode(activity.window, color, alpha)
    }

    fun darkOnly(activity: Activity) {
        val window = activity.window
        if (isFlyme4Later) {
            darkModeForFlyme4(window, true)
        } else if (isMIUI6Later) {
            darkModeForMIUI6(window, true)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            darkModeForM(window, true)
        }
    }


    /** 设置状态栏darkMode,字体颜色及icon变黑(目前支持MIUI6以上,Flyme4以上,Android M以上)  */
    fun darkMode(window: Window, color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        if (isFlyme4Later) {
            darkModeForFlyme4(window, true)
            immersive(window, color, alpha)
        } else if (isMIUI6Later) {
            darkModeForMIUI6(window, true)
            immersive(window, color, alpha)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            darkModeForM(window, true)
            immersive(window, color, alpha)
        } else if (Build.VERSION.SDK_INT >= 19) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            setTranslucentView(window.decorView as ViewGroup, color, alpha)
        } else {
            immersive(window, color, alpha)
        }
    }

    /** android 6.0设置字体颜色  */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun darkModeForM(window: Window, dark: Boolean) {
        var systemUiVisibility = window.decorView.systemUiVisibility
        systemUiVisibility = if (dark) {
            systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        window.decorView.systemUiVisibility = systemUiVisibility
    }

    /**
     * 设置Flyme4+的darkMode,darkMode时候字体颜色及icon变黑
     * http://open-wiki.flyme.cn/index.php?title=Flyme%E7%B3%BB%E7%BB%9FAPI
     */
    private fun darkModeForFlyme4(window: Window?, dark: Boolean): Boolean {
        var result = false
        if (window != null) {
            try {
                val e = window.attributes
                val darkFlag =
                    WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(e)
                value = if (dark) {
                    value or bit
                } else {
                    value and bit.inv()
                }
                meizuFlags.setInt(e, value)
                window.attributes = e
                result = true
            } catch (var8: Exception) {
                Log.e("StatusBar", "darkIcon: failed")
            }
        }
        return result
    }

    /**
     * 设置MIUI6+的状态栏是否为darkMode,darkMode时候字体颜色及icon变黑
     * http://dev.xiaomi.com/doc/p=4769/
     */
    private fun darkModeForMIUI6(window: Window, dark: Boolean): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            darkModeForM(window, dark)
        }
        val clazz: Class<out Window> = window.javaClass
        return try {
            var darkModeFlag = 0
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
            darkModeFlag = field.getInt(layoutParams)
            val extraFlagField = clazz.getMethod(
                "setExtraFlags",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
            extraFlagField.invoke(window, if (dark) darkModeFlag else 0, darkModeFlag)
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    /** 判断是否Flyme4以上  */
    private val isFlyme4Later: Boolean
        get() = (Build.FINGERPRINT.contains("Flyme_OS_4")
                || Build.VERSION.INCREMENTAL.contains("Flyme_OS_4")
                || Pattern.compile("Flyme OS [4|5]", Pattern.CASE_INSENSITIVE).matcher(Build.DISPLAY).find())

    /** 判断是否为MIUI6以上  */
    private val isMIUI6Later: Boolean
        get() = try {
            val clz = Class.forName("android.os.SystemProperties")
            val mtd = clz.getMethod("get", String::class.java)
            var `val` = mtd.invoke(null, "ro.miui.ui.version.name") as String
            `val` = `val`.replace("[vV]".toRegex(), "")
            val version = `val`.toInt()
            version >= 6
        } catch (e: Throwable) {
            false
        }

    /** 增加View的paddingTop,增加的值为状态栏高度  */
    fun setPadding(ctx: Context, view: View) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            view.setPadding(
                view.paddingLeft, view.paddingTop + getStatusBarHeight(ctx),
                view.paddingRight, view.paddingBottom
            )
        }
    }

    /** 增加View的paddingTop,增加的值为状态栏高度 (智能判断，并设置高度) */
    fun setPaddingSmart(ctx: Context, view: View) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            val lp = view.layoutParams
            if (lp != null && lp.height > 0) {
                lp.height += getStatusBarHeight(ctx) //增高
            }
            view.setPadding(
                view.paddingLeft, view.paddingTop + getStatusBarHeight(ctx),
                view.paddingRight, view.paddingBottom
            )
        }
    }

    /** 增加View的高度以及paddingTop,增加的值为状态栏高度.一般是在沉浸式全屏给ToolBar用的  */
    fun setHeightAndPadding(ctx: Context, view: View) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            val lp = view.layoutParams
            lp.height += getStatusBarHeight(ctx) //增高
            view.setPadding(
                view.paddingLeft, view.paddingTop + getStatusBarHeight(ctx),
                view.paddingRight, view.paddingBottom
            )
        }
    }

    /** 增加View上边距（MarginTop）一般是给高度为 WARP_CONTENT 的小控件用的 */
    fun setMargin(ctx: Context, view: View) {
        if (Build.VERSION.SDK_INT >= MIN_API) {
            val lp = view.layoutParams
            if (lp is MarginLayoutParams) {
                lp.topMargin += getStatusBarHeight(ctx) //增高
            }
            view.layoutParams = lp
        }
    }

    /**
     * 创建假的透明栏
     */
    private fun setTranslucentView(container: ViewGroup, color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        if (Build.VERSION.SDK_INT >= 19) {
            val mixtureColor = mixtureColor(color, alpha)
            var translucentView = container.findViewById<View>(R.id.custom)
            if (translucentView == null && mixtureColor != 0) {
                translucentView = View(container.context)
                translucentView.id = R.id.custom
                val lp = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(container.context)
                )
                container.addView(translucentView, lp)
            }
            translucentView?.setBackgroundColor(mixtureColor)
        }
    }

    private fun mixtureColor(color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float): Int {
        val a = if (color and -0x1000000 == 0) 0xff else color ushr 24
        return color and 0x00ffffff or ((a * alpha).toInt() shl 24)
    }

    /** 获取状态栏高度  */
    fun getStatusBarHeight(ctx: Context): Int {
        var result = 56
        val resId = ctx.resources.getIdentifier("status_bar_height", "dimen", "android")
        result = if (resId > 0) {
            ctx.resources.getDimensionPixelSize(resId)
        } else {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                result.toFloat(), Resources.getSystem().displayMetrics
            ).toInt()
        }
        return result
    }
}