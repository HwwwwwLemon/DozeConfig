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


import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.hwwwwwlemon.dozeconfig.R
import com.github.hwwwwwlemon.dozeconfig.activity.base.BaseActivity
import com.github.hwwwwwlemon.dozeconfig.adapter.ApplicationAdapter
import com.github.hwwwwwlemon.dozeconfig.databinding.ActivityApplicationListBinding
import com.github.hwwwwwlemon.dozeconfig.utils.DozeFileUtil
import com.github.hwwwwwlemon.dozeconfig.utils.MyPreferences
import com.github.hwwwwwlemon.dozeconfig.utils.Utils
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.zhanghai.android.fastscroll.FastScroller
import me.zhanghai.android.fastscroll.FastScrollerBuilder


class ApplicationsListActivity : BaseActivity() {
    private var refreshLayout: RefreshLayout? = null
    private var rv: RecyclerView? = null
    private var fastScroll: FastScroller? = null
    private var adapter: ApplicationAdapter? = null
    private var enableShowSystemApps: Int = 1
    private var self = this
    private val controlScope = CoroutineScope(Dispatchers.Default)
    private var searchView: SearchView? = null
    private lateinit var binding: ActivityApplicationListBinding
    private var preferences: MyPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationListBinding.inflate(layoutInflater)
        preferences = MyPreferences(this)
        setContentView(binding.root)
        initStatusBar()
        initLayout()

    }

    override fun initStatusBar() {
        super.initStatusBar()
        enableHomeButton(true)
        supportActionBar?.title = getString(R.string.applications)
    }

    private fun initLayout() {
        refreshLayout = findViewById<View>(R.id.refreshLayout) as RefreshLayout
        refreshLayout?.setRefreshHeader(MaterialHeader(this))
        refreshLayout?.apply {
            setOnRefreshListener { loadApps(true) }
            autoRefresh()
        }

        rv = findViewById(R.id.my_recycler_view)
        val llm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = ApplicationAdapter(self, enableShowSystemApps, rv!!)
        adapter?.setHasStableIds(true);
        adapter?.refresh(true)
        rv?.addItemDecoration(SpaceItemDecoration(10));
        fastScroll = FastScrollerBuilder(rv!!).useMd2Style().build()
        if (preferences?.get("lazy_load_icon", false) as Boolean) {
            rv?.addOnScrollListener(AutoLoadRecyclerView(this).init())
        }
        rv?.itemAnimator?.changeDuration = 1500;
        rv?.layoutManager = llm
        rv?.adapter = adapter
    }

    private fun loadApps(force: Boolean = false) {
        try {
            adapter?.filterType = enableShowSystemApps
            adapter?.refresh(force)
            val close = if (adapter?.itemCount!! > 0) 100 else 2000
            refreshLayout?.finishRefresh(close)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun enableHomeButton(status: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(status)
        supportActionBar?.setHomeButtonEnabled(status)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_show_app_list, menu)
        //Toolbar的搜索框
        val searchItem = menu?.findItem(R.id.toolbar_search)
        searchView = searchItem?.actionView as SearchView

        searchView?.queryHint = getString(R.string.search_hint)
        searchView?.setOnCloseListener {
            adapter?.searchStr = ""
            enableHomeButton(true)
            false
        }
        searchView?.setOnSearchClickListener {
            enableHomeButton(false)
        }
        searchView?.setOnQueryTextListener(adapter?.getSearchListener())
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (rv?.scrollState == 0) {
            return when (item.itemId) {
                R.id.toolbar_show_system_apps -> {
                    item.isChecked = !item.isChecked
                    enableShowSystemApps = if (item.isChecked) 2 else 1
                    loadApps()
                    true
                }
                R.id.toolbar_save -> {
                    if (DozeFileUtil.saveDozeFile(this)) {
                        Utils.showToast(this, getString(R.string.save_success))
                    } else {
                        Utils.showToast(this, getString(R.string.save_failed))
                    }
                    finish()
                    true
                }
                android.R.id.home -> {
                    finish()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        } else {
            Utils.showToast(this, getString(R.string.in_loop))
            return false
        }

    }

    private fun release() {
        try {
            if (isFinishing) {
                Glide.with(self.applicationContext).pauseAllRequests()
                controlScope.launch {
                    withContext(Dispatchers.Main) {
                        Glide.get(self.applicationContext).clearMemory();
                    }
                    adapter?.clear()
                    adapter = null
                    rv = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onBackPressed() {
        if (!searchView!!.isIconified) {
            searchView?.isIconified = true;
        } else {
            super.onBackPressed()
        }

    }

    override fun onStop() {
        release()
        super.onStop()
    }


    inner class AutoLoadRecyclerView(ctx: Context, attrs: AttributeSet?, defStyle: Int) :
        RecyclerView(ctx, attrs, defStyle) {
        constructor(ctx: Context) : this(ctx, null) {}
        constructor(ctx: Context, attrs: AttributeSet?) : this(ctx, attrs, 0) {}

        fun init(): ImageAutoLoadScrollListener {
            return ImageAutoLoadScrollListener()
        }

        //监听滚动来对图片加载进行判断处理
        inner class ImageAutoLoadScrollListener : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    SCROLL_STATE_IDLE ->                     //当屏幕停止滚动，加载图片
                        try {
                            if (context != null) Glide.with(context).resumeRequests()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    SCROLL_STATE_DRAGGING ->                     //当屏幕滚动且用户使用的触碰或手指还在屏幕上，停止加载图片
                        try {
                            if (context != null) Glide.with(context).pauseRequests()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    SCROLL_STATE_SETTLING ->                     //由于用户的操作，屏幕产生惯性滑动，停止加载图片
                        try {

                            if (context != null) {
                                Glide.with(context).pauseRequests()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                }
            }
        }
    }

    inner class SpaceItemDecoration(var mSpace: Int) : RecyclerView.ItemDecoration() {
        /**
         * Retrieve any offsets for the given item. Each field of `outRect` specifies
         * the number of pixels that the item view should be inset by, similar to padding or margin.
         * The default implementation sets the bounds of outRect to 0 and returns.

         * If this ItemDecoration does not affect the positioning of item views, it should set
         * all four fields of `outRect` (left, top, right, bottom) to zero
         * before returning.
         *

         * If you need to access Adapter for additional data, you can call
         * [RecyclerView.getChildAdapterPosition] to get the adapter position of the
         * View.
         *
         * @param outRect Rect to receive the output.
         * @param view    The child view to decorate
         * @param parent  RecyclerView this ItemDecoration is decorating
         * @param state   The current state of RecyclerView.
         */
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            // outRect.left = mSpace
            // outRect.right = mSpace
            outRect.bottom = mSpace
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = mSpace
            }
        }
    }
}