package com.github.refreshrecyclerview

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.github.caijinglong.refresh.library.HeaderFooterHolder
import com.github.caijinglong.refresh.library.KHeaderAdapter
import com.github.caijinglong.refresh.library.KLoadMoreAble
import com.github.caijinglong.refresh.library.KLoadMoreAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {


    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        swipe_refresh_layout.setOnRefreshListener(this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = Adapter(list)
        adapter.isShowEmptyView = true
        adapter.setProgressColors(Color.parseColor("#007557"))
        recyclerView.adapter = adapter

        setCustomText()

        adapter.bindSwipeRefreshLayout(swipe_refresh_layout)
        adapter.setOnLoadMoreListener(object : KLoadMoreAdapter.OnLoadMoreListener {
            override fun onLoadMore(adapter: KLoadMoreAdapter<*, *>) {
                loadMore()
            }
        })
    }

    private fun setCustomText() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val loadView = inflater.inflate(R.layout.layout_custom_loading, recyclerView, false)
        val loadText = loadView.findViewById<TextView>(R.id.tv_load_text)
        val able: KLoadMoreAble = object : KLoadMoreAble {
            override fun onLoadMoreStart() {
                loadText.text = "加载中..."
            }

            override fun onLoadMoreEnd() {
                loadText.text = "加载完成"
            }

            override fun onNoMoreData() {
                loadText.text = "没有更多数据了"
            }

            override fun onReleaseToLoad() {
                loadText.text = "松手加载"
            }
        }
        adapter.setLoadMoreView(loadView, able)
    }

    private val list = arrayListOf<String>()

    private val refreshData = arrayListOf<String>()
    private val loadMoreData = arrayListOf("load more 1", "load more 2", "load more 3")

    private val headerData = "我是头部信息"

    init {
        (0 until 50).mapTo(refreshData) { "刷新的数据 $it" }

//        list.addAll(refreshData)
    }

    override fun onRefresh() {
        refresh()
    }

    fun refresh() {
        val contentOb = Observable
                .create<List<String>> {
                    it.onNext(refreshData)
                    it.onComplete()
                }
                .subscribeOn(Schedulers.io())
                .delay(300, TimeUnit.MILLISECONDS)
                .doOnError {
                    onRefreshSuccess()
                }
                .observeOn(AndroidSchedulers.mainThread())

        val headerOb = Observable.create<String> {
            it.onNext(headerData)
            it.onComplete()
        }.subscribeOn(Schedulers.io())
                .delay(300, TimeUnit.MILLISECONDS)
                .doOnError {
                    onRefreshSuccess()
                }
                .observeOn(AndroidSchedulers.mainThread())

        Observable.zip(contentOb, headerOb, BiFunction { t1: List<String>, t2: String ->
            AllData(t1, t2)
        }).doOnError {

        }.subscribe {
            list.clear()

            if (count % 3 == 0) {
                list.addAll(it.list)
            }

            count++
//            list.addAll(it.list)
//            if (header == null) {
//                header = createHeader(it.header)
//                adapter.addHeaderAdapter(header!!)
//            }
//            header?.data = it.header
            adapter.notifyDataSetChanged()
            onRefreshSuccess()
            toast("刷新完成")
        }
    }

    var count = 0

    var header: KHeaderAdapter<String, *>? = null

    fun createHeader(content: String): KHeaderAdapter<String, *> {
        return HeaderAdapter(content)
    }

    data class AllData(val list: List<String>, val header: String)

    fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun onRefreshSuccess() {
        swipe_refresh_layout.isRefreshing = false
        adapter.noMoreData = false
        page = 0
    }

    var page = 0

    fun loadMore() {
        Observable
                .create<List<String>> {
                    it.onNext(loadMoreData)
                    it.onComplete()
                }
                .delay(300, TimeUnit.MILLISECONDS)
                .doOnError {
                    onLoadmoreSuccess()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    list.addAll(it)
                    page++
                    adapter.notifyDataSetChanged()
                    toast("加载完成")
                    onLoadmoreSuccess()
                    if (page == 3) {
                        adapter.noMoreData = true
                    }
                }
    }

    fun onLoadmoreSuccess() {
        adapter.loadMoring = false
    }

    class Adapter(list: List<String>) : KLoadMoreAdapter<String, VH>(list) {
        override fun bindViewHolder(holder: VH?, position: Int, item: String) {
            holder?.apply {
                textView?.text = item
            }
        }

        override fun newViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_text, parent, false)
            return VH(view)
        }
    }

    class VH(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val textView = itemView?.findViewById<TextView>(R.id.textView)
    }

    class HeaderAdapter(data: String?) : KHeaderAdapter<String, HeaderHolder>(data) {

        override fun onCreateHolder(parent: ViewGroup, viewType: Int): HeaderHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_text, parent, false)
            return HeaderHolder(view)
        }

        override fun onBindData(holder: HeaderHolder, position: Int) {
            holder.textView?.text = data
        }

        override fun itemViewType(): Int {
            return 1001
        }

    }

    class HeaderHolder(itemView: View?) : HeaderFooterHolder(itemView) {

        val textView: TextView? by lazy { itemView?.findViewById<TextView>(R.id.textView) }

    }
}