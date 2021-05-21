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
package com.github.hwwwwwlemon.dozeconfig.activity

import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceFragmentCompat
import com.github.hwwwwwlemon.dozeconfig.DozeManager
import com.github.hwwwwwlemon.dozeconfig.R
import com.github.hwwwwwlemon.dozeconfig.activity.base.BaseActivity
import com.github.hwwwwwlemon.dozeconfig.databinding.ActivitySettingsBinding
import com.github.hwwwwwlemon.dozeconfig.utils.Utils


class SettingsActivity : BaseActivity() {
    private lateinit var mBinding: ActivitySettingsBinding
    private lateinit var mDialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, SettingsFragment()).commit()
        }

        initStatusBar()
    }

    override fun initStatusBar() {
        super.initStatusBar()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.delete_config -> {

                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.prompt)
                builder.setMessage(R.string.delete_config_dialog)
                builder.setPositiveButton(getString(R.string.cancel)) { _, _ ->
                    mDialog.dismiss()
                }
                builder.setNegativeButton(getString(R.string.confirm)) { _, _ ->
                    try {
                        if (DozeManager.deleteFile(DozeManager.DOZE_FILE_PATH, this)) {
                            Utils.showToast(this, R.string.delete_success)
                        }
                    } catch (e: Exception) {
                        Utils.showToast(this, R.string.delete_failed)
                    }
                }
                mDialog = builder.create()
                mDialog.show()
                val btn: Button = mDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                val timer: CountDownTimer = object : CountDownTimer(6000, 1000) {
                    override fun onTick(arg0: Long) {
                        val thetime = (arg0 / 1000).toInt()
                        mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).text =
                            "${getString(R.string.confirm)}${if (thetime > 0) "($thetime)" else ""}"
                    }

                    override fun onFinish() {
                        btn.isEnabled = true
                    }
                }
                btn.isEnabled = false
                timer.start()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)
        }
    }
}