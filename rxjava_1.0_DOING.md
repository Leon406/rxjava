# Rxjava 1.0

### Why this Version

- 第一个1.0正式版,功能完善,bug 相对较少
- 分包合理,命名规范,没有冗余代码
- 加入背压   支持
- 代码简化
  - 简化操作符 用lift统一实现
  - 简化Schedulers



### 目录结构

   rx
    │  Notification.java   # 消息容器
    │  Observable.java   # 被观察者类 核心
    │  Observer.java       # 观察者接口
    │  Producer.java       # 生产者接口 
    │  Scheduler.java      # 调度器
    │  Subscriber.java     # 订阅者
    │  Subscription.java  # 订阅事件
    │
    ├─exceptions             # 自定义异常
    │      CompositeException.java
    │      Exceptions.java
    │      MissingBackpressureException.java
    │      OnErrorFailedException.java
    │      OnErrorNotImplementedException.java
    │      OnErrorThrowable.java
    │
    ├─functions      #  函数接口及转换工具
    │      Action.java
    │      Action0.java
    │      Action1.java
    │      Action2.java
    │      Action3.java
    │      Action4.java
    │      Action5.java
    │      Action6.java
    │      Action7.java
    │      Action8.java
    │      Action9.java
    │      ActionN.java
    │      Actions.java
    │      Func0.java
    │      Func1.java
    │      Func2.java
    │      Func3.java
    │      Func4.java
    │      Func5.java
    │      Func6.java
    │      Func7.java
    │      Func8.java
    │      Func9.java
    │      FuncN.java
    │      Function.java
    │      Functions.java
    │
    ├─internal        # 内置类,
    │  ├─operators    # 操作符
    │  │      BlockingOperatorLatest.java
    │  │      BlockingOperatorMostRecent.java
    │  │      BlockingOperatorNext.java
    │  │      BlockingOperatorToFuture.java
    │  │      BlockingOperatorToIterator.java
    │  │      BufferUntilSubscriber.java
    │  │      NotificationLite.java
    │  │      OnSubscribeAmb.java
    │  │      OnSubscribeCache.java
    │  │      OnSubscribeCombineLatest.java
    │  │      OnSubscribeDefer.java
    │  │      OnSubscribeDelaySubscription.java
    │  │      OnSubscribeDelaySubscriptionWithSelector.java
    │  │      OnSubscribeFromIterable.java
    │  │      OnSubscribeGroupJoin.java
    │  │      OnSubscribeJoin.java
    │  │      OnSubscribeMulticastSelector.java
    │  │      OnSubscribeRange.java
    │  │      OnSubscribeRedo.java
    │  │      OnSubscribeRefCount.java
    │  │      OnSubscribeTimerOnce.java
    │  │      OnSubscribeTimerPeriodically.java
    │  │      OnSubscribeToObservableFuture.java
    │  │      OnSubscribeUsing.java
    │  │      OperatorAll.java
    │  │      OperatorAny.java
    │  │      OperatorAsObservable.java
    │  │      OperatorBufferWithSingleObservable.java
    │  │      OperatorBufferWithSize.java
    │  │      OperatorBufferWithStartEndObservable.java
    │  │      OperatorBufferWithTime.java
    │  │      OperatorCast.java
    │  │      OperatorConcat.java
    │  │      OperatorDebounceWithSelector.java
    │  │      OperatorDebounceWithTime.java
    │  │      OperatorDefaultIfEmpty.java
    │  │      OperatorDelay.java
    │  │      OperatorDelayWithSelector.java
    │  │      OperatorDematerialize.java
    │  │      OperatorDistinct.java
    │  │      OperatorDistinctUntilChanged.java
    │  │      OperatorDoOnEach.java
    │  │      OperatorDoOnSubscribe.java
    │  │      OperatorDoOnUnsubscribe.java
    │  │      OperatorElementAt.java
    │  │      OperatorFilter.java
    │  │      OperatorFinally.java
    │  │      OperatorGroupBy.java
    │  │      OperatorMap.java
    │  │      OperatorMapNotification.java
    │  │      OperatorMapPair.java
    │  │      OperatorMaterialize.java
    │  │      OperatorMerge.java
    │  │      OperatorMergeDelayError.java
    │  │      OperatorMergeMaxConcurrent.java
    │  │      OperatorMulticast.java
    │  │      OperatorObserveOn.java
    │  │      OperatorOnBackpressureBuffer.java
    │  │      OperatorOnBackpressureDrop.java
    │  │      OperatorOnErrorFlatMap.java
    │  │      OperatorOnErrorResumeNextViaFunction.java
    │  │      OperatorOnErrorResumeNextViaObservable.java
    │  │      OperatorOnErrorReturn.java
    │  │      OperatorOnExceptionResumeNextViaObservable.java
    │  │      OperatorPublish.java
    │  │      OperatorReplay.java
    │  │      OperatorRetryWithPredicate.java
    │  │      OperatorSampleWithObservable.java
    │  │      OperatorSampleWithTime.java
    │  │      OperatorScan.java
    │  │      OperatorSequenceEqual.java
    │  │      OperatorSerialize.java
    │  │      OperatorSingle.java
    │  │      OperatorSkip.java
    │  │      OperatorSkipLast.java
    │  │      OperatorSkipLastTimed.java
    │  │      OperatorSkipTimed.java
    │  │      OperatorSkipUntil.java
    │  │      OperatorSkipWhile.java
    │  │      OperatorSubscribeOn.java
    │  │      OperatorSwitch.java
    │  │      OperatorTake.java
    │  │      OperatorTakeLast.java
    │  │      OperatorTakeLastTimed.java
    │  │      OperatorTakeTimed.java
    │  │      OperatorTakeUntil.java
    │  │      OperatorTakeWhile.java
    │  │      OperatorThrottleFirst.java
    │  │      OperatorTimeInterval.java
    │  │      OperatorTimeout.java
    │  │      OperatorTimeoutBase.java
    │  │      OperatorTimeoutWithSelector.java
    │  │      OperatorTimestamp.java
    │  │      OperatorToMap.java
    │  │      OperatorToMultimap.java
    │  │      OperatorToObservableList.java
    │  │      OperatorToObservableSortedList.java
    │  │      OperatorUnsubscribeOn.java
    │  │      OperatorWindowWithObservable.java
    │  │      OperatorWindowWithSize.java
    │  │      OperatorWindowWithStartEndObservable.java
    │  │      OperatorWindowWithTime.java
    │  │      OperatorZip.java
    │  │      OperatorZipIterable.java
    │  │      TakeLastQueueProducer.java
    │  │
    │  ├─schedulers  # 调度器
    │  │      NewThreadWorker.java
    │  │      ScheduledAction.java
    │  │
    │  └─util           # 工具类
    │      │  FrontPadding.java
    │      │  IndexedRingBuffer.java
    │      │  ObjectPool.java
    │      │  PaddedAtomicInteger.java
    │      │  PaddedAtomicIntegerBase.java
    │      │  PlatformDependent.java
    │      │  README.md
    │      │  RxRingBuffer.java
    │      │  RxThreadFactory.java
    │      │  ScalarSynchronousObservable.java
    │      │  SubscriptionIndexedRingBuffer.java
    │      │  SubscriptionList.java
    │      │  SubscriptionRandomList.java
    │      │  SynchronizedQueue.java
    │      │  SynchronizedSubscription.java
    │      │  UtilityFunctions.java
    │      │
    │      └─unsafe  # 并发包
    │              ConcurrentCircularArrayQueue.java
    │              ConcurrentSequencedCircularArrayQueue.java
    │              MessagePassingQueue.java
    │              MpmcArrayQueue.java
    │              Pow2.java
    │              README.md
    │              SpmcArrayQueue.java
    │              SpscArrayQueue.java
    │              UnsafeAccess.java
    │
    ├─observables  #  自定义Observable
    │      BlockingObservable.java
    │      ConnectableObservable.java
    │      GroupedObservable.java
    │
    ├─observers  #  自定义observer
    │      Observers.java
    │      SafeSubscriber.java
    │      SerializedObserver.java
    │      SerializedSubscriber.java
    │      Subscribers.java
    │      TestObserver.java
    │      TestSubscriber.java
    │
    ├─plugins   #  全局hook插件
    │      RxJavaErrorHandler.java
    │      RxJavaObservableExecutionHook.java
    │      RxJavaObservableExecutionHookDefault.java
    │      RxJavaPlugins.java
    │      RxJavaSchedulersHook.java
    │
    ├─schedulers  # 调度器
    │      CachedThreadScheduler.java
    │      EventLoopsScheduler.java
    │      ExecutorScheduler.java
    │      GenericScheduledExecutorService.java
    │      ImmediateScheduler.java
    │      NewThreadScheduler.java
    │      Schedulers.java
    │      SleepingAction.java
    │      TestScheduler.java
    │      TimeInterval.java
    │      Timestamped.java
    │      TrampolineScheduler.java
    │
    ├─subjects   # subject
    │      AsyncSubject.java
    │      BehaviorSubject.java
    │      PublishSubject.java
    │      ReplaySubject.java
    │      SerializedSubject.java
    │      Subject.java
    │      SubjectSubscriptionManager.java
    │      TestSubject.java
    │
    └─subscriptions  # 自定义subscription
            BooleanSubscription.java
            CompositeSubscription.java
            MultipleAssignmentSubscription.java
            RefCountSubscription.java
            SerialSubscription.java
            Subscriptions.java

#### 首先介绍几个新概念

- Operator 函数接口  

   就是前面介绍 Func1接口,

   call调用返回Subscriber

   所有OperatorXxx操作符都实现Operator 接口

- **OnSubscribe**函数接口

    Observable#create时传入

    Observable#subscribe调用时 执行OnSubscribe#call(Subscriber ) 

- **Subscriber**  

   进行所有事件的回调

   实现了  Observer和Subscription

   持有Producer,进行request处理

   Subscriber  之间传递SubscriptionList ,可以进行订阅处理

- SubscriptionList  

   所有Subscription列表,进行统一处理

- Producer函数接口

  request指定Producer生成item数量

- **lift** 操作

  将Operator  转换成Observable

  Observable#subscribe时  执行 onSubscribe#call(st)    (st 为Operator#call返回的Subscriber)



先看个例子

```
String [] names = {"Ben", "George"}
Observable.from(names)
          .map(s -> " rx1.0.0  " + s + "   ")
          .subscribe(s -> System.out.println("Hello " + s + "!"));
```



### Observable的创建

- create  核心
- from
- just

无论是 from 还是just, 其底层还是走的create

```
 # 创建是传入OnSubscribe
 public final static <T> Observable<T> create(OnSubscribe<T> f) {
     return new Observable<T>(hook.onCreate(f));  # 默认未hook,原样返回
 }
 #构造传参 onSubscribe,划重点,后面会用到
 protected Observable(OnSubscribe<T> f) {
     this.onSubscribe = f;
 }
```

OnSubscribe 是什么呢?

```
 public static interface OnSubscribe<T> extends Action1<Subscriber<? super T>> {
        // cover for generics insanity
  }
```

其实就是Action1的马甲, 跟上篇的Fun1 一样,是个函数接口,那么看下 from的  OnSubscribe 

```
# 将array 转成Iterable对象
public final static <T> Observable<T> from(T[] array) {
     return from(Arrays.asList(array));
}
#  由OnSubscribeFromIterable创建 OnSubscribe
public final static <T> Observable<T> from(Iterable<? extends T> iterable) {
    return create(new OnSubscribeFromIterable<T>(iterable));
}
```

下面分析 OnSubscribeFromIterable

```
 #构造传参
 public OnSubscribeFromIterable(Iterable<? extends T> iterable) {
        this.is = iterable;
 }
 
 #call 方法 Observable#subscribe会调用(后面详细介绍)
 public void call(final Subscriber<? super T> o) {
        final Iterator<? extends T> it = is.iterator();
        o.setProducer(new IterableProducer<T>(o, it));
  }
```

**看下Subscriber,重点**

```
 pivate final SubscriptionList cs;
 private final Subscriber<?> op; # 每进行一次操作,都会有一个新的op
 
 protected Subscriber() {
        this.op = null;
        this.cs = new SubscriptionList();
    }

 #  自己构造自己, 有点像链表的Node
 protected Subscriber(Subscriber<?> op) {
        this.op = op;
        this.cs = op.cs;
  }
  
  # 向上传递producer,直到根节点再请求数据,producer#request
  public void setProducer(Producer producer) {
        long toRequest;
        boolean setProducer = false;
        synchronized (this) {
            toRequest = requested;
            p = producer;
            if (op != null) {
                // middle operator ... we pass thru unless a request has been made
                if (toRequest == Long.MIN_VALUE) {
                    setProducer = true;
                }

            }
        }
        // do after releasing lock
        if (setProducer) {
            op.setProducer(p);
        } else {
            if (toRequest == Long.MIN_VALUE) {
                p.request(Long.MAX_VALUE);
            } else {
                p.request(toRequest);
            }
        }
    }
```

IterableProducer#request

```
private final Subscriber<? super T> o;

#request 回调 Subscriber#onNext#onCompleted#onError
public void request(long n) {
     while (it.hasNext()) {
           if (o.isUnsubscribed()) {
               return;
            }
            o.onNext(it.next());
      }
      if (!o.isUnsubscribed()) {
          o.onCompleted();
      }
}
```



### Observerable 订阅

Observable#subscribe

```
 #subcribe  注意这里是无参构造, 这是最里层的Subscriber
 public final Subscription subscribe(final Action1<? super T> onNext) 
     return subscribe(new Subscriber<T>() {
       ...
  }
  
  # 最终走的都是Subscriber, 底层 onSubscribe.call(subscriber)
  # 可以回顾下前面OnSubscribeFromIterable的介绍
 public final Subscription subscribe(Subscriber<? super T> subscriber) { 
 		...
 		
 		#对非SafeSubscriber 进行包装, 一般都会包装
 		if (!(subscriber instanceof SafeSubscriber)) {
            subscriber = new SafeSubscriber<T>(subscriber);
        }
        
 		 try {
            // allow the hook to intercept and/or decorate
            hook.onSubscribeStart(this, onSubscribe).call(subscriber);#核心
            return hook.onSubscribeReturn(subscriber);
        } catch (Throwable e) {
           subscriber.onError(hook.onSubscribeError(e));
        }
        ...
         return Subscriptions.empty();
 }
```



### 变换

Observable#lift

```
 public final <R> Observable<R> lift(final Operator<? extends R, ? super T> lift) {
        return new Observable<R>(new OnSubscribe<R>() {
            @Override
            public void call(Subscriber<? super R> o) {
                try {
                    Subscriber<? super T> st = hook.onLift(lift).call(o);
                    try {
                         onStart();
                        onSubscribe.call(st);
                    } catch (Throwable e) {
                   
                        if (e instanceof OnErrorNotImplementedException) {
                            throw (OnErrorNotImplementedException) e;
                        }
                        st.onError(e);
                    }
                } catch (Throwable e) {
                    if (e instanceof OnErrorNotImplementedException) {
                        throw (OnErrorNotImplementedException) e;
                    }
                    o.onError(e);
                }
            }
        });
    }
```

