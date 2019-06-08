# RxJava 0.1.2

## 四个对象

- Observable               被观察者, 生产者, 发布消息
- Observe                     观察者   ,  消费者, 接受消息 
- Subject                       主题,即可发布消息又可接受消息
- Subcription                订阅,进行取消订阅操作

## 相关函数类

- Action0/Action1/Action2/Action3     订阅事件处理接口, 进行回调处理
- Func0/Func1/Func../Fun9/FunN       函数接口
- Functions                                              Funx或者Object转换 FunN

## 操作符(函数变换,变成新的Observable) 

  <T> Func1<Observer<T>, Subscription>   返回单方法函数Func1 ,输入  Observer<T> , 输出Subscription,调用subcribe 时,返回Subscription

- OperationMap
- OperationTake
- OperationFilter
- OperationScan
- ...
- OperationMerge



## 流程

Observable 创建     核心create

```
//Observable 类
public static <T> Observable<T> create(Func1<Observer<T>, Subscription> func) {
    return new Observable<T>(func);
}

 protected Observable(Func1<Observer<T>, Subscription> onSubscribe) {
        this(onSubscribe, false);
 }
    
 private Observable(Func1<Observer<T>, Subscription> onSubscribe, boolean isTrusted){
        this.onSubscribe = onSubscribe;
        this.isTrusted = isTrusted;
 }
 
 //创建后,就持有Func1<Observer<T>, Subscription> 的引用
```

变换（以map为例）  创建新的MapObservable 并且持有Observable

```
//Observable 类
public static <T, R> Observable<R> map(Observable<T> sequence, Func1<T, R> func) {
    return _create(OperationMap.map(sequence, func));
    //_create 同create
}

//OperationMap 类
 public static <T, R> Func1<Observer<R>, Subscription> map(Observable<T> sequence, Func1<T, R> func) {
        return new MapObservable<T, R>(sequence , func);
    }
    
     private static class MapObservable<T, R> implements Func1<Observer<R>, Subscription{
        public MapObservable(Observable<T> sequence, Func1<T, R> func) {
            this.sequence = sequence;
            this.func = func;
        }
        
          public Subscription call(Observer<R> observer) {
            return sequence.subscribe(new MapObserver<T, R>(observer, func));
        }
     }
 
 private static class MapObserver<T, R> implements Observer<T> {
        public MapObserver(Observer<R> observer, Func1<T, R> func) {
            this.observer = observer;
            this.func = func;
        }

        Observer<R> observer;

        Func1<T, R> func;

        public void onNext(T value) {
          ...
          observer.onNext(func.call(value)); //func.call(value) 为 map 变换的结果
          ...

        }
    }

```

订阅 subscribe  执行 Func1<Observer<T>, Subscription> 的call 方法 ,  

```
// Observable
public Subscription subscribe(Observer<T> observer) {
    ...
    if (isTrusted) {
        return onSubscribe.call(observer);  // 
    } 
    ...
};


```

 MapObservable 的call 方法

```
 public Subscription call(Observer<R> observer) {
            return sequence.subscribe(new MapObserver<T, R>(observer, func));
  }
```

, 再接着执行Observalbe 的subscribe



事件开始发送 Observable.toObservable 为例

```
 public static <T> Observable<T> toObservable(T... items) {
        return toObservable(Arrays.asList(items));
    }
 public static <T> Observable<T> toObservable(Iterable<T> iterable) {
        return _create(OperationToObservableIterable.toObservableIterable(iterable));
    }
    //具体看OperationToObservableIterable.toObservableIterable  这个func1 call 怎么执行的
```



```
// OperationToObservableIterable#toObservableIterable , 当subcribe 的时候, 就会向下游发送事件
 private static class ToObservableIterable<T> implements Func1<Observer<T>, Subscription> {
        public ToObservableIterable(Iterable<T> list) {
            this.iterable = list;
        }

        public Iterable<T> iterable;

        public Subscription call(Observer<T> observer) {
            for (T item : iterable) {
                observer.onNext(item);  //向下游发送事件
            }
            observer.onCompleted();

            return Observable.noOpSubscription();
        }
    }
```

