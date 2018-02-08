package com.github.caijinglong.refresh.library

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

/**
 * Created by cai on 2018/2/8.
 */
abstract class KAdapter<Data, VH : HeaderFooterHolder?>(var data: Data?) : RecyclerView.Adapter<HeaderFooterHolder>() {

    override fun getItemCount(): Int {
        return 1
    }

    override fun getItemViewType(position: Int): Int {
        return itemViewType()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: HeaderFooterHolder?, position: Int) {
        onBindData(holder as VH, position)
    }

    abstract fun onBindData(holder: VH, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH? {
        if (parent == null) {
            return null
        }
        return onCreateHolder(parent, viewType)
    }

    abstract fun onCreateHolder(parent: ViewGroup, viewType: Int): VH

    abstract fun itemViewType(): Int

    private var observer = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            mKAdapter?.onDataChange()
        }
    }

    var mKAdapter: KLoadMoreAdapter<*, *>? = null

    fun attachToKAdapter(adapter: KLoadMoreAdapter<*, *>) {
        this.mKAdapter = adapter
        registerAdapterDataObserver(observer)
    }

    fun swapAttachToKAdapter(adapter: KLoadMoreAdapter<*, *>): KLoadMoreAdapter<*, *>? {
        val old = mKAdapter
        attachToKAdapter(adapter)
        return old
    }

    fun detachFromKAdapter() {
        this.mKAdapter = null
        unregisterAdapterDataObserver(observer)
    }
}

abstract class KHeaderAdapter<Data, VH : HeaderFooterHolder?>(data: Data?) : KAdapter<Data, VH>(data)

abstract class KFooterAdapter<Data, VH : HeaderFooterHolder?>(data: Data?) : KAdapter<Data, VH>(data)

open class HeaderFooterHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)