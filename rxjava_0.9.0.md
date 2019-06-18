## Rxjava 0.9.0

### Why this Version

- 类相对较少 98个,学习成本低
- 完善的subjects 
- 线程调度Schedulers



#### 此篇不在介绍整体调用流程,请参考上篇rxjava_0.5.0, 会针对某几个模块进行解析 

- 全局hook 机制
- 线程切换机制
- subject介绍
- 常用operator解析



#### 全局hook 机制   Observable#subscribe

![1560309346361](D:\github\rxjava\img\1560309346361.png)

```
  hook  = RxJavaPlugins.getInstance().getObservableExecutionHook(); //默认是空实现
```

抽象类 RxJavaObservableExecutionHook 三个方法, 默认是原样返回

- onSubscribeStart
- onSubscribeReturn
- onSubscribeError

代码比较简单,Observable#subscribe 在各个位置加了hook,如图红框位置



### 线程切换机制  

####  Observable#subscribeOn

```
 public Observable<T> subscribeOn(Scheduler scheduler) {
        return subscribeOn(this, scheduler);
 }
  public static <T> Observable<T> subscribeOn(Observable<T> source, Scheduler scheduler) {
        return create(OperationSubscribeOn.subscribeOn(source, scheduler));
} 
 
```

主要实现在OperationSubscribeOn#subscribeOn

```
public static <T> Func1<Observer<T>, Subscription> subscribeOn(Observable<T> source, Scheduler scheduler) {
        return new SubscribeOn<T>(source, scheduler);
 }
 //继续看 SubscribeOn call,   scheduler.schedule
 
public Subscription call(final Observer<T> observer) {
            return scheduler.schedule(new Func0<Subscription>() {
                @Override
                public Subscription call() {
                    return new ScheduledSubscription(source.subscribe(observer), scheduler);
                }
            });
}

```

scheduler.schedule 方法  这里分析Scheduler的子类 NewThreadScheduler

```
public <T> Subscription schedule(final T state, final Func2<Scheduler, T, Subscription> action) {
        final AtomicObservableSubscription subscription = new AtomicObservableSubscription();
        final Scheduler _scheduler = this;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                subscription.wrap(action.call(_scheduler, state));
            }
        }, "RxNewThreadScheduler");

        t.start();

        return subscription;
    } 
  //创建了名字为 RxNewThreadScheduler 的线程, 并返回subscription订阅
```

一般我们使用Schedulers 工具类创建线程,里面有默认io 和compute 线程池有兴趣可以看看

#### Observable#observeOn

调用顺序Observable#observeOn->  OperationObserveOn#observeOn ->ObserveOn#call , 直接看call方法

```
if (scheduler instanceof ImmediateScheduler) {
	//当前线程不操作
    return source.subscribe(observer);
 } else {
	//交由 ScheduledObserver 进行调度
    return source.subscribe(new ScheduledObserver<T>(observer, 	   								scheduler));
}
```

ScheduledObserver中onNext,onComplete,onError 由ConcurrentLinkedQueue 消息队列逐个轮询处理

```
private void processQueue() {
 scheduler.schedule(new Action0(){
     ...
 }
}
//这里scheduler.schedule 最终调用还是与subscribeOn 相同的
```

### subject介绍

既是Observable, 又是Observer,有4个子类

- AsyncSubject

  > ```
  > Subject that publishes only the last event to each  Observer that has subscribed when the completes. 
  > ```

  当complete的时候,发送最后一个事件,*源码see@ AsyncSubject#onCompleted*

- BehaviorSubject

  > ```
  > Subject that publishes the last and all subsequent events to each Observer that subscribes. 
  > ```

  发送上一个(没有取默认值) 和接下订阅来所有的事件,

  *源码see@ BehaviorSubject##onSubscribe, 订阅时会发送上一个value 的next 事件*

- **PublishSubject**

  > ```
  > Subject that publishes a single event to each  Observer that has subscribed.
  > ```

​	将事件发送给已经订阅的Observer, 必须要订阅后才能接受事件

​        *比较简单,源码see@ PublishSubject#onNext, 订阅后,才会收到事件*

- ReplaySubject

  > ```
  > Subject that retains all events and will replay them to an link Observer that subscribes
  > ```

  重新发送所有事件

  *源码see@ReplaySubject#onNext    订阅后,会从history 遍历发送事件*



### 常用operator解析  ---主要看Fun1 这个类的call 方法

- 创建 (不多做介绍 see@ rxjava_0.5.0.md)
  - create      创建  最原始的
  - just          Observable#toObservable
  - from        Observable#toObservable
- 变换
  - map                              变换 1对1    (不多做介绍 see@ rxjava_0.5.0.md)
  - flatMap                        1对多
  - scan                              累计
  - groupBy                       分组
- 过滤
  - filter                              过滤
  - skip                                跳过
  - take/takeLast                取个数
- 组合
  - concat                            连接
  - merge                             合并
  - startWith                        开始
  - combineLatest              合并多个Observable 最新的数据
  - zip                                   压缩成Pair
- 转换 
  - toObservable        转Observable(不多做介绍 see@ rxjava_0.5.0.md)
  - toList                      转list Observable
  - toSortedList          转排序后list Observable
- 辅助   (不多做介绍 see@ 线程切换)
  - subscribeOn         指定订阅线程
  - observerOn          指定观察线程



### 变换 mapMany /scan/groupBy

#### mapMany

相关类:OperationMap.mapMany-->OperationMerge#merge

#### scan

#### groupBy



### 过滤

#### filter



#### scan



#### take

相关类: OperationTake#take -->  Take#call-->ItemObserver#onNext

```
 public void onNext(T args) {
                final int count = counter.incrementAndGet();
                if (count <= num) {  //判断事件的数量是否达到指定数量
                    observer.onNext(args);
                    if (count == num) {
                        observer.onCompleted();
                    }
                }
                if (count >= num) {  //事件的数量达到指定数量后取消订阅
                    subscription.unsubscribe();
                }
            }
```

#### takeLast

相关类:OperationTakeLast#takeLast-->TakeLast#call-->ItemObserver#onCompleted

```
public void onCompleted() {
       Iterator<T> reverse = deque.descendingIterator();//反序
       while (reverse.hasNext()) {
             observer.onNext(reverse.next()); //发送反序的数据
        }
         bserver.onCompleted();
 }

public void onNext(T args) {
      while (!deque.offerFirst(args)) {  //加入deque中的第一个节点
            deque.removeLast();     //失败的时候移除最后一个
      }
}
//Deque简介,双向链表,间距队列和栈的特点,可以操作两端
// 例如发送事件是  1 2 3 , 取2个, 这里 deque长度为2
       1
       2 1
       3 2  (dequq长度满了,1删除)
  反序后为
       2 3
       
```

### 组合

#### concat

#### merge

#### startWith

#### combineLatest

#### zip



### 转换

#### toList

#### toSortedList