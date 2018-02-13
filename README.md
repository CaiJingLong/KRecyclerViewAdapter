# KLoadMoreAdapter

一个滚动到底刷新组件,可以结合SwipeRefreshLayout使用
使用kotlin开发

[![Release](https://jitpack.io/v/caijinglong/KRecyclerViewAdapter.svg)](https://jitpack.io/#caijinglong/KRecyclerViewAdapter)

## install

当前最新版本可以查看tag
文档编写时为0.8.1

### project's build.gradle

```gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

### module's build.gradle

```gradle
    dependencies {
            compile 'com.github.CaiJingLong:KRecyclerViewAdapter:0.8.1'
    }
```
## use

### 点击事件

泛型类型和Adapter的Data泛型一致

```kotlin
            setOnItemClickListener(object : KLoadMoreAdapter.OnItemClickListener<String> {
                override fun onAdapterItemClick(data: String, position: Int) {
                    Toast.makeText(this@MainActivity, data, Toast.LENGTH_SHORT).show()
                }
            })
```

### 加载完成调用(必须)
用于回调底部loadMore,用于显示完成状态
set/get
下面是废弃属性,保持有效
```
adapter.loadMoring = false
```

新属性
```
adapter.isLoading = false
```

### 没有更多数据调用
set/get
```
adapter.noMoreData = true // 这个会暂时更多数据的加载,如需开启设置为false,下拉刷新时内部会设置为false
```

### 需要关闭加载更多功能
set/get
```
adapter.enableLoadMore = false //如果有需求,需要关闭加载更多功能,则使用这个开关
```

### 监听回调
set
```
adapter.setOnLoadMoreListener(object : KLoadMoreAdapter.OnLoadMoreListener {
            override fun onLoadMore(adapter: KLoadMoreAdapter<*, *>) {
                loadMore()//业务逻辑
            }
        })
```

### 设置刷新的颜色
set
```
adapter.setProgressColors(Color.parseColor("#007557")) //参考SwipeRefreshLayout.setColorSchemeColors(int... colors)
```

### 自定义LoadMoreView

#### 自定义View
> 特别说明:这里如果需要响应 加载过程的状态变化结束,则需要实现KLoadMoreAble接口KLoadMoreAble可以为空
> 也可以让自定义View实现KLoadMoreAble来实现相同效果
> KLoadMoreAble和view同时都有时,前者优先于后者

```kotlin
val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
val loadView = inflater.inflate(R.layout.layout_custom_loading, recyclerView, false)
val loadText = loadView.findViewById<TextView>(R.id.textView)
val able: KLoadMoreAble = object : KLoadMoreAble {
    override fun onLoadMoreStart() {
        loadText.text = "加载中"
    }

    override fun onLoadMoreEnd() {
        loadText.text = "加载完成"
    }

    override fun onNoMoreData() {
        loadText.text = "没有更多数据了"
    }
}
adapter.setLoadMoreView(loadView, able)
```

#### 复写adapter中的createDefaultLoadMoreView 方法
```
override fun createDefaultLoadMoreView(parent: ViewGroup): View {
    val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val loadView = inflater.inflate(R.layout.layout_custom_loading, recyclerView, false)
    val loadText = loadView.findViewById<TextView>(R.id.textView)
    val able: KLoadMoreAble = object : KLoadMoreAble {
        override fun onLoadMoreStart() {
            loadText.text = "加载中"
        }

        override fun onLoadMoreEnd() {
            loadText.text = "加载完成"
        }

        override fun onNoMoreData() {
            loadText.text = "没有更多数据了"
        }
    }
    this.setLoadMoreView(loadView, able)
```

### EmptyView

#### 开关
0.8.5 新增 默认关闭

```
adapter.isShowEmptyView = true
```

注意:这里如果设置为true时,当EmptyView显示且SwipeRefreshLayout可用,loadMore默认会被关闭,只能通过下拉刷新来更新数据,如不存在SwipeRefreshLayout则下拉刷新功能会开启

#### 自定义默认文字
优先级低于自定义EmptyView
```
adapter.emptyText = "没有数据"
```

#### 自定义EmptyView

    val view:View = createView() //自己实现View
    adapter.emptyView = view //可为空,为空则使用默认的空数据View

### 单独使用

#### 定义
定义Adapter继承KLoadMoreAdapter,实现抽象方法

```kotlin
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
```

#### 绑定方法

和正常使用Adapter相同

```kotlin
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = Adapter(list)
        recyclerView.adapter = adapter
```

### 结合SwipeRefreshLayout使用

用于处理上拉刷新和上拉加载可能造成的数据错乱问题
现在加载更多和下拉刷新不能同时进行,即上拉加载过程中不能下拉刷新,反之亦然

#### 绑定
```
adapter.bindSwipeRefreshLayout(swipe_refresh_layout)
```

#### 解除绑定
```
adapter.unbindSwipeRefreshLayout()
```

#### 设置刷新状态
0.8.4新增属性
set/get
    adapter.isRefreshing //与SwipeRefeshLayout的isRefreshing相等,srl为空时,返回false

### KRefreshLayout

新的控件
KRefreshLayout(内部封装了KLoadMoreAdapter 和SwipeRefreshLayout)
用于处理SwipeRefreshLayout和Adapter的关系

#### use

private val layoutRefresh: KRefreshLayout<GoodData, VH> by bindView(R.id.layout_refresh)

```kotlin
 layoutRefresh.viewHolderBuilder = VH.Companion

        layoutRefresh.setOnLoadDataListener(object : KRefreshLayout.OnLoadDataListener {
            override fun onLoadMore(adapter: KLoadMoreAdapter<*, *>) {
                listPresenter.loadData()
                        ?.subscribe {
                            layoutRefresh.addData(it.list, false)
                        }
            }

            override fun onRefresh() {
                listPresenter.refreshData()
                        ?.subscribe {
                            layoutRefresh.addData(it.list, true)
                        }
            }
        })

        layoutRefresh.setOnItemClickListener(object : KLoadMoreAdapter.OnItemClickListener<GoodData> {
            override fun onAdapterItemClick(data: GoodData, position: Int) {
                Toast.makeText(this@KListActivity, data.id, Toast.LENGTH_SHORT).show()
            }
        })
```

#### 方法/属性

1. layoutRefresh.viewHolderBuilder = VH.Companion

```
    lateinit var viewHolderBuilder: KViewHolderBuilder<Data, VH>

    interface ViewHolderBuilder<VH> {

        fun build(parent: ViewGroup, viewType: Int): VH
    }

    interface DataBinder<Data, VH> {
        fun bindData(holder: VH?, position: Int, item: Data)
    }

    interface KViewHolderBuilder<Data, VH> : ViewHolderBuilder<VH>, DataBinder<Data, VH>

```

提供创建ViewHolder和绑定holder数据的接口,必传

2. setOnItemClickListener

设置条目的点击事件

```
layoutRefresh.setOnItemClickListener(object : KLoadMoreAdapter.OnItemClickListener<GoodData> {
            override fun onAdapterItemClick(data: GoodData, position: Int) {
                Toast.makeText(this@KListActivity, data.id, Toast.LENGTH_SHORT).show()
            }
        })
```

3. setOnLoadDataListener(listener:OnLoadDataListener)

设置下拉刷新和上拉加载的回调

方法签名:

`interface OnLoadDataListener : KLoadMoreAdapter.OnLoadMoreListener, SwipeRefreshLayout.OnRefreshListener`

## 开源协议
Apache 2.0