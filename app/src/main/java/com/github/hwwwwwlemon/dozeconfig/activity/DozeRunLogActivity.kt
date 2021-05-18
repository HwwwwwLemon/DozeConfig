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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.github.hwwwwwlemon.dozeconfig.DozeManager
import com.github.hwwwwwlemon.dozeconfig.R
import com.github.hwwwwwlemon.dozeconfig.activity.base.BaseActivity
import com.github.hwwwwwlemon.dozeconfig.databinding.ActivityDozeRunLogBinding
import com.github.hwwwwwlemon.dozeconfig.databinding.TipsLayoutBinding

class DozeRunLogActivity : BaseActivity() {
    private lateinit var mBinding: ActivityDozeRunLogBinding
    private lateinit var mLogContent: String
    private lateinit var mLogTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityDozeRunLogBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initStatusBar()
    }

    override fun onResume() {
        loadLogData()
        super.onResume()
    }

    override fun initStatusBar() {
        super.initStatusBar()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = getString(R.string.doze_run_log)
    }

    private fun loadLogData() {
        try {
            mLogContent = DozeManager.readFile("Android/doze.log", this, "\n")
            if (mLogContent.isEmpty()) {
                mLogContent = getString(R.string.found_log_file) + "但是内容为空!"
            }
        } catch (e: Exception) {
            mLogContent = getString(R.string.not_found_log_file)
        }
        mLogTextView = mBinding.logContent
        mLogTextView.text = mLogContent
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_run_log, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_log -> {
                val promptBinding = TipsLayoutBinding.inflate(layoutInflater)
                promptBinding.promptContent.text = getString(R.string.prompt_delete_log)
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle(R.string.prompt)
                alertDialogBuilder.setView(promptBinding.root)
                alertDialogBuilder.setNegativeButton(
                    "OK"
                ) { _, _ ->
                    run {
                        DozeManager.deleteFile("Android/doze.log", this)
                        loadLogData()
                    }
                }

                alertDialogBuilder.setPositiveButton(
                    "Cancel"
                ) { dialog, _ ->
                    dialog.cancel()
                }
                val dialog = alertDialogBuilder.create()
                dialog.show()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}