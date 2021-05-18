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

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.hwwwwwlemon.dozeconfig.R
import com.github.hwwwwwlemon.dozeconfig.utils.DozeFileUtil
import com.github.hwwwwwlemon.dozeconfig.utils.Utils
import kotlinx.coroutines.*


class ApplicationAdapter() : RecyclerView.Adapter<ApplicationAdapter.AppViewHolder>() {

    private val apps: MutableList<Apps> = mutableListOf()
    private var showApps: MutableList<Apps> = mutableListOf()
    private var searchApps: MutableList<Apps> = mutableListOf()
    private var checkApps: MutableList<Apps> = mutableListOf()
    private lateinit var rv: RecyclerView
    private lateinit var pm: PackageManager
    private lateinit var activity: Activity
    var filterType: Int = 1
    var searchStr: String = ""
    private var isRefreshing = false
    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val controlScope = CoroutineScope(Dispatchers.Default)

    constructor(
        activity: Activity,
        filterType: Int,
        rv: RecyclerView
    ) : this() {
        this.rv = rv
        this.pm = activity.packageManager
        this.activity = activity
        this.filterType = filterType
    }

    inner class AppViewHolder constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var appName: TextView = itemView.findViewById<View>(R.id.app_name) as TextView
        var packageName: TextView = itemView.findViewById<View>(R.id.package_name) as TextView
        var appIcon: ImageView = itemView.findViewById<View>(R.id.app_icon) as ImageView
        var switchIcon: Switch = itemView.findViewById<View>(R.id.check_white_list_app) as Switch
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.application_unit_layout, parent, false)
        return AppViewHolder(v)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            bind(holder, position)
        }

    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        bind(holder, position)
    }

    private fun bind(holder: AppViewHolder, position: Int) {
        try {
            holder.appName.text = showApps[position].appName
            holder.packageName.text = showApps[position].packageName
            Glide.with(holder.appIcon)
                .load(showApps[position].applicationInfo.loadIcon(pm))
                .into(object : CustomTarget<Drawable?>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {
                        holder.appIcon.setImageDrawable(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        holder.appIcon.setImageDrawable(pm.defaultActivityIcon)
                    }
                })
            holder.switchIcon.setOnCheckedChangeListener(null)
            holder.switchIcon.isChecked = DozeFileUtil.checkAppList.contains(showApps[position])
            holder.itemView.setOnClickListener {
                holder.switchIcon.toggle()
            }
            holder.switchIcon.setOnCheckedChangeListener { _, isChecked ->
                if (rv.scrollState == 0) {
                    onCheckedChange(isChecked, showApps[position])
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onCheckedChange(isChecked: Boolean, app: Apps) {
        if (isChecked) {
            DozeFileUtil.checkAppList.add(app)
        } else {
            DozeFileUtil.checkAppList.remove(app)
        }

    }

    override fun onViewRecycled(holder: ApplicationAdapter.AppViewHolder) {
        /*  holder.appIcon.setImageDrawable(null)
          Glide.with(holder.appIcon).clear(holder.appIcon)*/
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return showApps.size

    }

    private fun getFilter(): AppFilter {
        return AppFilter()
    }

    @Synchronized
    private fun setRefreshStatus(status: Boolean) {
        this.isRefreshing = status
    }

    @Synchronized
    private fun getRefreshStatus(): Boolean {
        return isRefreshing
    }


    override fun getItemId(position: Int): Long {
        val info = showApps[position].applicationInfo;
        return (info.packageName + "!" + info.uid / 100000).hashCode().toLong()
    }

    fun refresh(force: Boolean) {
        synchronized(this) {
            if (getRefreshStatus()) {
                Log.e("isRefreshing", "Cancel Refreshing")
                return;
            }
            setRefreshStatus(true)
        }

        try {
            controlScope.launch {
                async {
                    checkApps.clear()
                    checkApps.addAll(DozeFileUtil.checkAppList)
                    sortList(checkApps)
                }.join()

                if (force) {
                    val loadAppsJob = async { loadApps() }
                    loadAppsJob.join()
                    val searchAppsJob = async { sortList(searchApps) }
                    searchAppsJob.join()

                    joinAll(loadAppsJob, searchAppsJob)
                }
                val ready = async {
                    withContext(Dispatchers.Main) {
                        synchronized(this) {
                            getFilter().filter(searchStr);
                            setRefreshStatus(false)
                        }
                    }
                }
                ready.join()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadApps() = withContext(Dispatchers.Default) {
        try {
            apps.clear()
            searchApps.clear()
            var flag: Int?
            val appList: MutableList<ApplicationInfo> = Utils.getAppList(activity.applicationContext)
            for (i in appList) {
                flag = if ((i.flags and ApplicationInfo.FLAG_SYSTEM) == 0) 1 else 2
                val excludeApp = !(i.packageName.contains("com.qti")
                        || i.packageName.contains("com.android.theme")
                        || i.packageName.contains("com.qualcomm")
                        || i.packageName.contains("overlay")
                        || i.packageName.contains("android.internal")
                        )

                if (excludeApp) {
                    val app = Apps(i, i.packageName, pm.getApplicationLabel(i).toString(), flag)
                    if (checkApps.contains(app)) {
                        continue
                    }
                    apps.add(app)
                }
            }
            searchApps.addAll(apps)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    private suspend fun sortList(list: MutableList<Apps>) = withContext(Dispatchers.Default) {
        try {
            list.sortBy {
                it.appName
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun getSearchListener(): SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchStr = query
                refresh(false)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchStr = newText
                refresh(false)
                return true
            }
        }
    }


    fun clear() {
        apps.clear()
        showApps.clear()
        checkApps.clear()
        searchApps.clear()
        uiScope.cancel()
        controlScope.cancel()
        viewModelJob.cancel()
    }

    inner class AppFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterResults = FilterResults()
            val filtered: MutableList<Apps> = mutableListOf()
            val filteredCheck: MutableList<Apps> = mutableListOf()

            if (constraint.toString().isEmpty()) {
                filtered.addAll(checkApps)
                filtered.addAll(
                    if (filterType == 1) {
                        searchApps.filter { it.flag == 1 }
                    } else {
                        searchApps
                    }
                )

            } else {
                val showSystemApps = filterType == 2
                for (i in searchApps) {
                    if ((i.packageName.contains(constraint.toString(), true) ||
                                i.appName.contains(constraint.toString(), true))
                    ) {
                        if ((i.flag == 2) and !showSystemApps) {
                            continue
                        }
                        filtered.add(i)
                    }

                }
                for (i in checkApps) {
                    if ((i.packageName.contains(constraint.toString(), true) ||
                                i.appName.contains(constraint.toString(), true))
                    ) {
                        filteredCheck.add(i)
                    }

                }
            }
            filtered.addAll(0, filteredCheck)
            filterResults.values = filtered
            filterResults.count = filtered.size
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            showApps.clear()
            showApps.addAll(results.values as Collection<Apps>)
            notifyDataSetChanged()
        }
    }


    class Apps(
        var applicationInfo: ApplicationInfo,
        var packageName: String,
        var appName: String,
        var flag: Int,
    ) {
        override fun equals(other: Any?): Boolean {
            if (other !is Apps) {
                return false;
            }
            return this.appName == other.appName && this.packageName == other.packageName
        }

        override fun hashCode(): Int {
            var result = 31 * packageName.hashCode()
            result = 31 * result + appName.hashCode()
            return result
        }
    }

}