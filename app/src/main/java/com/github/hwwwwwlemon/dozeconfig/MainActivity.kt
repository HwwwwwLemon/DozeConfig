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


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.github.hwwwwwlemon.dozeconfig.activity.ApplicationsListActivity
import com.github.hwwwwwlemon.dozeconfig.activity.DozeRunLogActivity
import com.github.hwwwwwlemon.dozeconfig.activity.SettingsActivity
import com.github.hwwwwwlemon.dozeconfig.databinding.ActivityMainBinding
import com.github.hwwwwwlemon.dozeconfig.databinding.DialogAboutBinding
import com.github.hwwwwwlemon.dozeconfig.utils.DozeFileUtil
import com.github.hwwwwwlemon.dozeconfig.utils.MyPreferences
import com.github.hwwwwwlemon.dozeconfig.utils.StatusBarUtil
import com.github.hwwwwwlemon.dozeconfig.utils.Utils
import com.google.android.material.card.MaterialCardView


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var statusSummary: TextView
    private lateinit var statusTitle: TextView
    private lateinit var statusCard: MaterialCardView
    private var preferences: MyPreferences? = null
    private var runDozeWhitelistOpt = false
    private var firstPressedTime: Long = 10000
    private var handlerFlag = true
    private var status = 0
    private val counts = 5 // 点击次数
    private var count = 0
    private var isRun = false
    private var mHits = LongArray(counts)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.immersive(this)
        preferences = MyPreferences(this)
        if (!Utils.isDarkMode(this)) {
            StatusBarUtil.darkMode(this);
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //设置Title logo
        Glide.with(binding.dozeIcon).load(R.mipmap.ic_launcher_foreground).into(binding.dozeIcon)
        //应用列表
        binding.status.setOnClickListener {
            continuousClick {
                when (status) {
                    0 -> {
                        Utils.showToast(this, "\uD83E\uDD73")
                        if (runDozeWhitelistOpt) {
                            Utils.refreshDozeWhitelist(this)
                        }

                    }
                    1 -> Utils.showToast(this, "\uD83E\uDD7A")
                    2 -> Utils.showToast(this, "\uD83D\uDE29")
                    else -> Utils.showToast(this, "\uD83E\uDD76")
                }

            }
        }

        binding.applicationsList.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ApplicationsListActivity::class.java
                )
            )
        }
        //Log
        binding.dozeRunLog.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DozeRunLogActivity::class.java
                )
            )
        }
        binding.settings.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
            )
        }
        //about
        binding.about.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
            val dialogBinding = DialogAboutBinding.inflate(LayoutInflater.from(this), null, false)
            alertDialogBuilder.setView(dialogBinding.root)
            dialogBinding.aboutCard.background = ContextCompat.getDrawable(this, R.drawable.gradient_color)
            dialogBinding.github.movementMethod = LinkMovementMethod.getInstance();
            dialogBinding.github.text =
                HtmlCompat.fromHtml(
                    getString(R.string.github) + "<a href='https://github.com/HwwwwwLemon/DozeConfig'>Issues</a>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )

            dialogBinding.author.movementMethod = LinkMovementMethod.getInstance();
            dialogBinding.author.text =
                HtmlCompat.fromHtml(
                    getString(R.string.author) + "<a href='https://www.coolapk.com/u/797452'>@Hwwwww</a>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            dialogBinding.appVersion.text = "version : " + getString(R.string.version)
            dialogBinding.description.text = getString(R.string.about_description)
            alertDialogBuilder.show()
        }


    }

    private fun continuousClick(func: () -> Any) {
        if (!runDozeWhitelistOpt || status != 0) {
            func()
            return
        }

        System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
        mHits[mHits.size - 1] = System.currentTimeMillis()
        if (!isRun) {
            if (handlerFlag) {
                handlerFlag = false
                Handler().postDelayed({
                    runOnUiThread {
                        handlerFlag = true
                        count = 0
                        mHits = LongArray(counts)
                    }
                }, 1500)
            }

            count++
            Utils.showToast(this, "再点击 ${counts - count} 次优化白名单 ", 600)
        }
        if (!isRun and (mHits[0] >= System.currentTimeMillis() - 3000) and (count == 5)) {
            mHits = LongArray(counts) //重新初始化数组
            isRun = true
            count = 0
            func()
            Handler().postDelayed({ runOnUiThread { isRun = false } }, 5000)
        }

    }

    override fun onResume() {
        runDozeWhitelistOpt = preferences?.get("run_doze_whitelist", false) as Boolean
        if (DozeManager.checkPermission(this)) {
            init()
        } else {
            finish()
        }
        super.onResume()
    }

    private fun init() {
        statusSummary = findViewById(R.id.status_summary)
        statusTitle = findViewById(R.id.status_title)
        statusCard = findViewById(R.id.status)
        var successColor = getColor(R.color.success)
        var errorColor = getColor(R.color.error)
        if (Utils.isDarkMode(this)) {
            successColor = getColor(R.color.success_dark)
            errorColor = getColor(R.color.error_dark)
        }



        when {
            DozeManager.checkDozeFile(this) and DozeFileUtil.loadDozeFile(this) -> {
                statusCard.setCardBackgroundColor(successColor)
                statusSummary.text = getString(R.string.found_whitelist_file_prompt)
                statusTitle.text = getString(R.string.found_whitelist_file)
                status = 0
            }
            DozeManager.checkDozeFile(this) and !DozeFileUtil.loadDozeFile(this) -> {
                statusCard.setCardBackgroundColor(errorColor)
                statusSummary.text = getString(R.string.whitelist_file_error)
                statusTitle.text = getString(R.string.found_whitelist_file)
                status = 1
            }
            else -> {
                statusCard.setCardBackgroundColor(errorColor)
                statusSummary.text = getString(R.string.not_found_whitelist_file_prompt)
                statusTitle.text = getString(R.string.not_found_whitelist_file)
                status = 2
            }
        }

    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - firstPressedTime < 2000) {
            super.onBackPressed();    //不要调用父类的方法
        } else {
            Utils.showToast(this, "再按一次退出")
            firstPressedTime = System.currentTimeMillis()
        }

    }
}

