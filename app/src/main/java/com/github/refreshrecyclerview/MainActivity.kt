package com.github.refreshrecyclerview

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.github.caijinglong.refresh.library.KLoadMoreAdapter
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {


    lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView.layoutManager = LinearLayoutManager(this)
        swipe_refresh_layout.setOnRefreshListener(this)
        adapter = Adapter(list)
        adapter.bindSwipeRefreshLayout(swipe_refresh_layout)
        recyclerView.adapter = adapter
        adapter.setOnLoadMoreListener(object : KLoadMoreAdapter.OnLoadMoreListener {
            override fun onLoadMore(adapter: KLoadMoreAdapter<*, *>) {
                loadMore()
            }
        })
    }

    val list = arrayListOf<String>()

    val refreshData = arrayListOf<String>()
    val loadMoreData = arrayListOf("load more 1", "load more 2", "load more 3")

    init {
        (0 until 50).mapTo(refreshData) { "刷新的数据 $it" }

        list.addAll(refreshData)
    }

    override fun onRefresh() {
        refresh()
    }

    fun refresh() {
        Observable
                .create<List<String>> {
                    it.onNext(refreshData)
                    it.onComplete()
                }
                .subscribeOn(Schedulers.io())
                .delay(2, TimeUnit.SECONDS)
                .doOnError {
                    onRefreshSuccess()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    list.clear()
                    list.addAll(it)
                    adapter.notifyDataSetChanged()
                    onRefreshSuccess()
                    toast("刷新完成")
                }
    }

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
                .delay(2, TimeUnit.SECONDS)
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

    class VH(itemView: View?) : UltimateRecyclerviewViewHolder<String>(itemView) {
        val textView = itemView?.findViewById<TextView>(R.id.textView)
    }
}
