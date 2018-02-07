package com.github.caijinglong.refresh.library

import android.content.Context
import android.support.v4.widget.ContentLoadingProgressBar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Created by cai on 2018/2/7.
 */
abstract class KLoadMoreAdapter<Data, VH : RecyclerView.ViewHolder?>(val list: List<Data>, var swipeRefreshLayout: SwipeRefreshLayout? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    var enableLoadMore = true
        set(value) {
            field = value
        }
        get() {
            return field
        }

    var loadMoring = false

    override fun getItemCount(): Int {
        var add = 0
        if (enableLoadMore) {
            add = 1
        }
        return if (list.isEmpty()) 0 else list.size + add
    }


    override fun getItemViewType(position: Int): Int {
        if (enableLoadMore && position == list.size) {
            return TYPE_LOAD_MORE

        }
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val itemViewType = getItemViewType(position)
        if (itemViewType == TYPE_LOAD_MORE) {
            return
        }
        val data = list[position]
        try {
            @Suppress("UNCHECKED_CAST")
            bindViewHolder(holder as VH?, position, data)
        } catch (e: Throwable) {

        }

    }

    var loadmoreView: View? = null

    abstract fun bindViewHolder(holder: VH?, position: Int, item: Data)

    private fun createDefaultLoadMoreView(parent: ViewGroup): DefaultLoadMoreView {
        return LayoutInflater.from(parent.context).inflate(R.layout.k_layout_default_loadmore, parent, false) as DefaultLoadMoreView
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        if (parent == null) {
            return null
        }

        if (viewType == TYPE_LOAD_MORE) {
            return if (loadmoreView == null) {
                val view = createDefaultLoadMoreView(parent)
                loadmoreView = view
                KLoadMoreHolder(view, view)
            } else {
                KLoadMoreHolder(loadmoreView, null)
            }
        }

        return newViewHolder(parent, viewType)
    }

    abstract fun newViewHolder(parent: ViewGroup, viewType: Int): VH

    private var dataObserver = DataObserver()

    private var helper: LoadMoreHelper? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        registerAdapterDataObserver(dataObserver)
        helper = LoadMoreHelper(recyclerView, this, swipeRefreshLayout)
        helper?.enableLoadMore()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        unregisterAdapterDataObserver(dataObserver)
    }

    companion object {
        val TYPE_LOAD_MORE = 300001
    }

    private var onLoadMoreListener: OnLoadMoreListener? = null

    fun setOnLoadMoreListener(listener: OnLoadMoreListener) {
        this.onLoadMoreListener = listener
    }

    interface OnLoadMoreListener {
        fun onLoadMore(adapter: KLoadMoreAdapter<*, *>)
    }

    fun onLoadMore() {
        loadMoring = true
        KLog.info("start loadmore")
        onLoadMoreListener?.onLoadMore(this)
    }

    open fun onDataChange() {
    }

    inner class DataObserver : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            onDataChange()
        }
    }
}

class DefaultLoadMoreView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), KLoadMoreAble {

    val mViewProgress: ContentLoadingProgressBar  by lazy { findViewById<ContentLoadingProgressBar>(R.id.view_progress) }
    val mTvLoadText: TextView by lazy { findViewById<TextView>(R.id.tv_load_text) }

    init {
        LayoutInflater.from(context).inflate(R.layout.k_default_loadmove_view, this, true)
    }

    override fun onLoadMoreStart() {
        mViewProgress.show()
        mTvLoadText.text = "加载中"

    }

    override fun onLoadMoreEnd() {
        mViewProgress.hide()
        mTvLoadText.text = "加载完成"
    }

    override fun onNoMoreData() {
        mViewProgress.hide()
        mTvLoadText.text = "没有更多数据了"
    }
}

interface KLoadMoreAble {

    fun onLoadMoreStart()

    fun onLoadMoreEnd()

    fun onNoMoreData()

}

class KLoadMoreHolder(itemView: View?, var loadMoreAble: KLoadMoreAble?) : RecyclerView.ViewHolder(itemView) {

    fun loadMoreStart() {
        loadMoreAble?.onLoadMoreStart()
    }

    fun loadMoreEnd() {
        loadMoreAble?.onLoadMoreEnd()
    }

    fun noMoreData() {
        loadMoreAble?.onNoMoreData()
    }

}