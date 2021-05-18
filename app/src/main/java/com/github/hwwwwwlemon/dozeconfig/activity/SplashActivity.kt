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

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.github.hwwwwwlemon.dozeconfig.DozeManager
import com.github.hwwwwwlemon.dozeconfig.MainActivity
import com.github.hwwwwwlemon.dozeconfig.R
import com.github.hwwwwwlemon.dozeconfig.databinding.ActivitySplashBinding
import com.github.hwwwwwlemon.dozeconfig.utils.Utils

class SplashActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.author.movementMethod = LinkMovementMethod.getInstance();
        mBinding.author.text =
            HtmlCompat.fromHtml(
                getString(R.string.author) + "<a href='https://www.coolapk.com/u/797452'>@Hwwwww</a>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        mBinding.version.text = "version: " + resources.getString(R.string.version)
    }

    override fun onResume() {
        try {
            val handler = Handler()
            handler.postDelayed({
                if (DozeManager.checkPermission(this)) {
                    Utils.showToast(this, "😴")
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    mBinding.prompt.text = getString(R.string.permission_prompt)
                }

            }, 1500)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onResume()
    }
}

