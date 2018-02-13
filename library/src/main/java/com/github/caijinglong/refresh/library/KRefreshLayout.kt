package com.github.caijinglong.refresh.library

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

/**
 * Created by cjl on 2018/2/12.
 */
open class KRefreshLayout<Data, VH : RecyclerView.ViewHolder?> @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val recyclerView: RecyclerView by bindView(R.id.recyclerView)
    val swipeRefreshLayout: SwipeRefreshLayout by bindView(R.id.swipeRefreshLayout)
    val list = arrayListOf<Data>()
    val adapter: Adapter by lazy { Adapter(list, swipeRefreshLayout) }

    val onLoadMore = object : KLoadMoreAdapter.OnLoadMoreListener {
        override fun onLoadMore(adapter: KLoadMoreAdapter<*, *>) {
            onLoadDataListener?.onLoadMore(adapter)
        }
    }

    lateinit var viewHolderBuilder: KViewHolderBuilder<Data, VH>

    private var onLoadDataListener: OnLoadDataListener? = null

    fun setOnLoadDataListener(onLoadDataListener: OnLoadDataListener?) {
        this.onLoadDataListener = onLoadDataListener
    }

    fun addData(list: List<Data>, clear: Boolean) {
        if (clear) {
            this.list.clear()
            adapter.isRefreshing = false
        } else {
            adapter.isLoading = false
        }
        this.list.addAll(list)
        adapter.notifyDataSetChanged()
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_k_refreshlayout, this, true)
        swipeRefreshLayout.setOnRefreshListener {
            onLoadDataListener?.onRefresh()
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.setOnLoadMoreListener(onLoadMore)
    }

    inner class Adapter(list: List<Data>, swipeRefreshLayout: SwipeRefreshLayout?) : KLoadMoreAdapter<Data, VH>(list, swipeRefreshLayout) {
        override fun bindViewHolder(holder: VH?, position: Int, item: Data) {
            viewHolderBuilder.bindData(holder, position, item)
        }

        override fun newViewHolder(parent: ViewGroup, viewType: Int): VH {
            return viewHolderBuilder.build(parent, viewType)
        }

    }

    interface OnLoadDataListener : KLoadMoreAdapter.OnLoadMoreListener, SwipeRefreshLayout.OnRefreshListener

    interface ViewHolderBuilder<VH> {

        fun build(parent: ViewGroup, viewType: Int): VH
    }

    interface DataBinder<Data, VH> {
        fun bindData(holder: VH?, position: Int, item: Data)
    }

    interface KViewHolderBuilder<Data, VH> : ViewHolderBuilder<VH>, DataBinder<Data, VH>

    private var onItemClickListener: KLoadMoreAdapter.OnItemClickListener<Data>? = null

    fun setOnItemClickListener(onItemClickListener: KLoadMoreAdapter.OnItemClickListener<Data>?) {
        this.onItemClickListener = onItemClickListener
        this.adapter.setOnItemClickListener(onItemClickListener)
    }
}


fun KRefreshLayout.KViewHolderBuilder<*, *>.createView(parent: ViewGroup, @LayoutRes layoutId: Int): View {
    return LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
}