package com.github.caijinglong.refresh.library

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * Created by cai on 2018/2/7.
 */
class LoadMoreHelper(val recyclerView: RecyclerView?, val loadmoreAdapter: KLoadMoreAdapter<*, *>, var swipeRefreshLayout: SwipeRefreshLayout?) {

    fun enableLoadMore() {
        val lm = recyclerView?.layoutManager as? LinearLayoutManager ?: return

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var currentState = RecyclerView.SCROLL_STATE_IDLE

            override fun onScrolled(rv: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    checkNeedLoadMore(lm)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                this.currentState = newState
                KLog.info("current state is $newState")
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    checkNeedLoadMore(lm)
                }
            }
        })
    }

    fun checkNeedLoadMore(lm: LinearLayoutManager) {
        KLog.info("checkLoad start")
        if (!loadmoreAdapter.enableLoadMore || loadmoreAdapter.loadMoring) {
            KLog.info("not loadMore")
            return
        }
        if (loadmoreAdapter.noMoreData) {
            return
        }
        if (swipeRefreshLayout != null && swipeRefreshLayout!!.isRefreshing) {
            KLog.info("refresh ing")
            return
        }
        val itemPosition = lm.findLastVisibleItemPosition()
        KLog.info("the loadMore")
        if (itemPosition >= loadmoreAdapter.itemCount - 1) {
            loadmoreAdapter.onLoadMore()
        }
    }

    fun disableLoadMore() {
        loadmoreAdapter.enableLoadMore = false
    }

    fun bindSwipeRefreshLayout(swipeRefreshLayout: SwipeRefreshLayout?) {
        this.swipeRefreshLayout = swipeRefreshLayout
    }

    fun unbindSwipeRefreshLayout() {
        this.swipeRefreshLayout = null
    }

    fun onLoadingChange(loading: Boolean) {
        swipeRefreshLayout?.isEnabled = !loading
    }

}