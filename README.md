# KLoadMoreAdapter

一个滚动到底刷新组件,可以结合SwipeRefreshLayout使用
使用kotlin开发

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

### 加载完成调用(必须)
用于回调底部loadMore,用于显示完成状态
```
adapter.loadMoring = false
```

### 没有更多数据调用

```
adapter.noMoreData = true // 这个会暂时更多数据的加载,如需开启设置为false,下拉刷新时内部会设置为false
```

### 需要关闭加载更多功能
```
adapter.enableLoadMore = false //如果有需求,需要关闭加载更多功能,则使用这个开关
```

### 监听回调
```
adapter.setOnLoadMoreListener(object : KLoadMoreAdapter.OnLoadMoreListener {
            override fun onLoadMore(adapter: KLoadMoreAdapter<*, *>) {
                loadMore()//业务逻辑
            }
        })
```

### 设置刷新的颜色
```
adapter.setProgressColors(Color.parseColor("#007557")) //参考SwipeRefreshLayout.setColorSchemeColors(int... colors)
```

### 自定义LoadMoreView

#### 自定义View
> 特别说明:这里如果需要响应 加载过程的状态变化结束,则需要实现KLoadMoreAble接口

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


#### 开源协议
Apache 2.0