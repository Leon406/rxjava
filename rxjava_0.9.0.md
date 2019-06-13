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

  当complete的时候,发送最后一个事件,源码see@ AsyncSubject#onCompleted

- BehaviorSubject

  > ```
  > Subject that publishes the last and all subsequent events to each Observer that subscribes. 
  > ```

  发送上一个 和接下来所有的事件

- **PublishSubject**

  > ```
  > Subject that publishes a single event to each  Observer that has subscribed.
  > ```

​	将事件发送给已经订阅的Observer

- ReplaySubject

  > ```
  > Subject that retains all events and will replay them to an link Observer that subscribes
  > ```

  重新发送所有事件



### 常用operator解析