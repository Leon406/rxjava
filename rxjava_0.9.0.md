## Rxjava 0.9.0

[TOC]



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



### 常用operator解析  ---主要看Fun1 这个类的call 方法及对应Observer

- 创建 (不多做介绍 see@ rxjava_0.5.0.md)
  - create      创建  最原始的
  - just           单个数据转 Observable#toObservable
  - from        多个数据转 Observable#toObservable
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

#### mapMany 与flatMap等价  注意  Func1<T, Observable<R>> func 返回是Observable

相关类:OperationMap.mapMany-->OperationMerge#merge

```
 public static <T, R> Func1<Observer<R>, Subscription> mapMany(Observable<T> sequence,           Func1<T, Observable<R>> func) {
         //create 为 Observable<Observable<T>> 
        return OperationMerge.merge(Observable.create(map(sequence, func)));
 }
```

接下来顺便介绍下merge   ---  将多个Observable 转换成一个Observable

相关类:OperationMerge#merge-->MergeObservable#call-->ParentObserver#onNext

```
//OperationMerge#merge

return new Func1<Observer<T>, Subscription>() { //这里是匿名内类,看call 方法

   @Override
   public Subscription call(Observer<T> observer) {
      return new MergeObservable<T>(o).call(observer);
  }
};

 
 
// MergeObservable#call

.... //同步操作

sequences.subscribe(new ParentObserver(synchronizedObserver));

return subscription;


//ParentObserver#onNext(Observable<T> childObservable)  
 ChildObserver _w = new ChildObserver();
 childObservers.put(_w, _w);
 // 每个childObservable分别订阅,这样actualObserver就能收到所有的子Observable事件回调了
 Subscription _subscription = childObservable.subscribe(_w);

```



#### scan

相关类：OperationScan#scan-->Accumulator/AccuWithoutInitialValue#call

-->AccumulatingObserver#onNext

```
//Accumulator 类 AccuWithoutInitialValue不展开介绍
public Subscription call(final Observer<R> observer) {
      observer.onNext(initialValue); //先发送初始值
      return sequence.subscribe(new AccumulatingObserver<T, R>(observer, initialValue,                accumulatorFunction));//包装observer进行累计
}
```

```
 //AccumulatingObserver#onNext
 public synchronized void onNext(T value) {
      try {
          acc = accumulatorFunction.call(acc, value); //上面已经发送了初始值,累计func进行累计
          observer.onNext(acc); //发送累计的数据
      } catch (Exception ex) {
         observer.onError(ex);
     }
}
```



#### groupBy

相关类:OperationGroupBy#groupBy-->GroupBy#call-->GroupByObserver#onNext

```
public void onNext(final KeyValue<K, V> args) {
	K key = args.key;
	boolean newGroup = keys.putIfAbsent(key, true) == null; 
	if (newGroup) {  //没有key时创建新组,否则跳过
		underlying.onNext(buildObservableFor(source, key));
	}
}

//buildObservableFor方法 是根据key 生成新的Observable,源码用了filter 过滤满足key的事件,再进行map 转换成value
```



### 过滤

#### filter

相关类: OperationFilter#filter-->Filter#call

```
return subscription.wrap(that.subscribe(new Observer<T>() {
                public void onNext(T value) {
                    try {
                       //filter predicate 函数判断true 才发送事件
                        if (predicate.call(value)) { 
                            observer.onNext(value);
                        }
                    } catch (Exception ex) {
                        observer.onError(ex);
                        subscription.unsubscribe();
                    }
                }

                public void onError(Exception ex) {
                    observer.onError(ex);
                }

                public void onCompleted() {
                    observer.onCompleted();
                }
            }));
```

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

相关类:OperationConcat#concat-->Concat#call--> 匿名类Observer#onNext

```
//Concat#call(final Observer<T> observer)
 final Observer<T> reusableObserver = new Observer<T>() { //
                @Override
                public void onNext(T item) {
                    observer.onNext(item);
                }
                @Override
                public void onError(Exception e) {
                        ...
                        observer.onError(e);
                    }
                }
                @Override
                public void onCompleted() {
                    synchronized (nextSequences) {
                        //complete 无nextSequences 结束,否则 轮询下一个进行订阅
                    	if (nextSequences.isEmpty()) { 
                            ...
                            observer.onCompleted();
                            ...
                        } else {
                            innerSubscription = new AtomicObservableSubscription();
                            //轮询下一个并进行订阅
                            innerSubscription.wrap(nextSequences.poll().subscribe(this));
                        }
                    }
                }
            };
 
  outerSubscription.wrap(sequences.subscribe(new Observer<Observable<T>>() {
                @Override
                public void onNext(Observable<T> nextSequence) {
                    synchronized (nextSequences) { //时序同步
                        if (innerSubscription == null) { //第一次发送时进入,订阅事件
                            innerSubscription = new AtomicObservableSubscription();
                            innerSubscription.wrap(nextSequence.subscribe(reusableObserver));
                        } else {
                            nextSequences.add(nextSequence); //非首次加入队列后面
                        }
                    }
                }
```



#### merge

相关类:OperationMerge#merge-->MergeObservable#call-->ParentObserver#onNext

```
//OperationMerge#merge

return new Func1<Observer<T>, Subscription>() { //这里是匿名内类,看call 方法

   @Override
   public Subscription call(Observer<T> observer) {
      return new MergeObservable<T>(o).call(observer);
  }
};

 
 
// MergeObservable#call

.... //同步操作

sequences.subscribe(new ParentObserver(synchronizedObserver));

return subscription;


//ParentObserver#onNext(Observable<T> childObservable)  
 ChildObserver _w = new ChildObserver();
 childObservers.put(_w, _w);
 
 // 每个childObservable分别订阅,这样actualObserver就能收到所有的子Observable事件回调了
 Subscription _subscription = childObservable.subscribe(_w);

```



#### combineLatest

相关类:OperationCombineLatest#combineLatest-->Aggregatorr#call,addObserver,next

​             -->CombineObserver#onNext

```
//OperationCombineLatest#combineLatest  注意看输入输出参数

     Aggregator<R> a = new Aggregator<R>(Functions.fromFunc(combineLatestFunction));
     a.addObserver(new CombineObserver<R, T0>(a, w0));
     a.addObserver(new CombineObserver<R, T1>(a, w1));
 
//Aggregator#addObserver  combine的Observable 存入列表
  	observers.add(w);  //observers 为LinkList
  
//当subscribe时会执行 Aggregator#call
      for (CombineObserver<R, ?> rw : observers) {  //遍历加入的CombineObserver,进行订阅 
            rw.startWatching();
      }

//CombineObserver#startWatching
  	 subscription = w.subscribe(this); //Observable 订阅自己

//CombineObserver#onNext
  	  a.next(this, args);   //将自己传给Aggregator 进行操作
  
//Aggregator#onNext
      latestValue.put(w, arg);  //将CombineObserver 和onNext 值存入map 中
      hasLatestValue.add(w);   //将CombineObserver 存入set集合中
      for (CombineObserver<R, ?> rw : observers) { //遍历所有将CombineObserver
        if (!hasLatestValue.contains(rw)) { // 没有onNext不执行下一步
            return;
         }
      }
      int i = 0;
      for (CombineObserver<R, ?> _w : observers) {  //遍历所有将CombineObserver
         argsToCombineLatest[i++] = latestValue.get(_w);// 将最新的值存入数组                                                                         //argsToCombineLatest
      }
      //最后,回调变换后的结果
      observer.onNext(combineLatestFunction.call(argsToCombineLatest));
```



#### zip    

相关类:OperationZip#zip-->Aggregator#call,addObserver,next-->ZipObserver#onNext

```
//OperationZip#zip
     Aggregator<R> a = new Aggregator<R>(Functions.fromFunc(zipFunction));
     a.addObserver(new ZipObserver<R, T0>(a, w0));
     a.addObserver(new ZipObserver<R, T1>(a, w1));
 
//Aggregator#addObserver  combine的Observable 存入列表
     observers.add(w);  //observers 为ConcurrentLinkedQueue
     receivedValuesPerObserver.put(w, new ConcurrentLinkedQueue<Object>());

//当subscribe时会执行 Aggregator#call
     for (ZipObserver<T, ?> rw : observers) {
         rw.startWatching();                     // //遍历加入的ZipObserver,进行订阅 
     }
 
//ZipObserver#startWatching
 	subscription.wrap(w.subscribe(this));  //Observable 订阅自己
 
//ZipObserver#onNext
 	a.next(this, args);  //将自己传给Aggregator 进行操作
  
//Aggregator#onNext
    receivedValuesPerObserver.get(w).add(arg); //将next的事件加入到ZipObserver 的队列中
      //receivedValuesPerObserver中每个ZipObserver 都有 数据才会发送事件
    for (ZipObserver<T, ?> rw : receivedValuesPerObserver.keySet()) {
        if (receivedValuesPerObserver.get(rw).peek() == null) {
                 return;                         
             }
     }
          
     int i = 0;
     for (ZipObserver<T, ?> rw : observers) {  //打包数据argsToZip
         argsToZip[i++] = receivedValuesPerObserver.get(rw).remove();
     }
     //最后,回调变换后的结果
     observer.onNext(zipFunction.call(argsToZip));
```



### 转换

#### toList

相关类:OperationToObservableList#toObservableList-->ToObservableList#call-->匿名Observer#onNext                        	          onCompleted

```
//ToObservableList#call
   return that.subscribe(new Observer<T>() {...})  //直接返回了匿名Observer

//匿名Observer#onNext
   list.add(value);  //将所有的value 加入到ConcurrentLinkedQueue 队列

//匿名Observer#onCompleted  将LinkedQueue 转成list 交由observer 一并发送
    ArrayList<T> l = new ArrayList<T>(list.size()); 
  	for (T t : list) {
         l.add(t);
     }
    observer.onNext(l);
    observer.onCompleted();

```



#### toSortedList(与toList基本相同,加了排序func)

相关类:OperationToObservableSortedList#toSortedList-->ToObservableSortedList#call

-->匿名Observer#onNext onCompleted

```
//前面流程与toList相同
//匿名Observer#onCompleted  将LinkedQueue 转成list 交由observer 一并发送
    ArrayList<T> l = new ArrayList<T>(list.size()); 
  	for (T t : list) {
         l.add(t);
     }
     
    //自定义排序函数排序
    Collections.sort(l, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return sortFunction.call(o1, o2);
            }

     });
    observer.onNext(l);
    observer.onCompleted();
```



## 终于分析完了~~~~~~~~~~~~~~~~~~~