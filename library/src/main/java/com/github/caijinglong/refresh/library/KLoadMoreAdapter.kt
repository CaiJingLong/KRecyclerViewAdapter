package com.github.caijinglong.refresh.library

import android.content.Context
import android.graphics.Color
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
            notifyDataSetChanged()
        }

    var loadMoring = false
        set(value) {
            field = value
            helper?.onLoadingChange(value)
            val v = loadmoreView
            if (v is KLoadMoreAble) {
                if (value) {
                    v.onLoadMoreStart()
                } else {
                    v.onLoadMoreEnd()
                }
            }
        }

    var noMoreData = false
        set(value) {
            field = value
            val v = loadmoreView
            if (v is KLoadMoreAble) {
                v.onNoMoreData()
            }
        }

    override fun getItemCount(): Int {
        val headerCount = headerAdapterList.count()

        var loadMoreCount = 0
        if (enableLoadMore) {
            loadMoreCount = 1
        }
        return (list.size) + loadMoreCount + headerCount
    }

    //cache the viewType
    private val headerTypeSet = HashSet<Int>()
    private val typePositionMap = HashMap<Int, Int>()

    override fun getItemViewType(position: Int): Int {
        if (position < headerAdapterList.count()) {
            val itemViewType = headerAdapterList[position].itemViewType()
            headerTypeSet.add(itemViewType)
            typePositionMap.put(itemViewType, position)
            return itemViewType
        }

        if (enableLoadMore && position == itemCount - 1) {
            return TYPE_LOAD_MORE
        }
        return getChildItemType(getRealPosition(position))
    }

    open fun getChildItemType(listPosition: Int): Int {
        return 0
    }

    private val headerAdapterList = ArrayList<KHeaderAdapter<*, *>>()

    fun addHeaderAdapter(adapter: KHeaderAdapter<*, *>) {
        headerAdapterList.add(adapter)
        adapter.attachToKAdapter(this)
        notifyDataSetChanged()
    }

    fun removeHeaderAdapter(adapter: KHeaderAdapter<*, *>) {
        headerAdapterList.remove(adapter)
        adapter.detachFromKAdapter()
        notifyDataSetChanged()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val itemViewType = getItemViewType(position)
        if (itemViewType == TYPE_LOAD_MORE) {
            return
        }
        if (headerTypeSet.contains(itemViewType)) {
            headerAdapterList[position].onBindViewHolder(holder as HeaderFooterHolder?, position)
            return
        }
        val realPosition = getRealPosition(position)
        val data = list[realPosition]
        try {
            bindViewHolder(holder as VH?, realPosition, data)
        } catch (e: Throwable) {

        }

    }

    fun getHeaderCount(): Int {
        return headerAdapterList.count()
    }

    fun getRealPosition(position: Int): Int {
        return position - getHeaderCount()
    }

    var loadmoreView: View? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    abstract fun bindViewHolder(holder: VH?, position: Int, item: Data)

    open fun createDefaultLoadMoreView(parent: ViewGroup): View {
        val defaultLoadMoreView = LayoutInflater.from(parent.context).inflate(R.layout.k_layout_default_loadmore, parent, false) as DefaultLoadMoreView
        defaultLoadMoreView.mViewProgress.progressDrawable.setColorSchemeColors(*mProgressColors)
        return defaultLoadMoreView
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        if (parent == null) {
            return null
        }

        if (headerTypeSet.contains(viewType)) {
            typePositionMap[viewType]?.apply {
                val kHeaderAdapter = headerAdapterList[this]
                return kHeaderAdapter.createViewHolder(parent, viewType)
            }
        }

        if (viewType == TYPE_LOAD_MORE) {
            return if (loadmoreView == null) {
                val view = createDefaultLoadMoreView(parent)
                loadmoreView = view
                val able: KLoadMoreAble? = if (view is KLoadMoreAble) view else null
                KLoadMoreHolder(view, able)
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

    fun bindSwipeRefreshLayout(swipeRefreshLayout: SwipeRefreshLayout?) {
        this.swipeRefreshLayout = swipeRefreshLayout
        swipeRefreshLayout?.setColorSchemeColors(*mProgressColors)
        helper?.bindSwipeRefreshLayout(swipeRefreshLayout)
    }

    fun unbindSwipeRefreshLayout() {
        this.swipeRefreshLayout = null
        helper?.unbindSwipeRefreshLayout()
    }

    private var mProgressColors: IntArray = intArrayOf(Color.parseColor("#009944"))
        set(value) {
            field = value
            val v = loadmoreView
            if (v is DefaultLoadMoreView) {
                v.setProgressColors(*mProgressColors)
            }
        }

    fun setProgressColors(vararg colors: Int) {
        this.mProgressColors = colors
        swipeRefreshLayout?.setColorSchemeColors(*colors)
    }
}

class DefaultLoadMoreView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), KLoadMoreAble {

    val mViewProgress: KProgressBar  by lazy { findViewById<KProgressBar>(R.id.view_progress) }
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

    fun setProgressColors(vararg colors: Int) {
        mViewProgress.progressDrawable.setColorSchemeColors(*colors)
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