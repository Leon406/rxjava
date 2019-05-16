# RxJava 0.1.2

## 主要四个对象

- Observable               被观察者, 生产者, 发布消息
- Observe                     观察者   ,  消费者, 接受消息 
- Subject                       主题,即可发布消息又可接受消息
- Subcription                订阅,进行取消订阅操作

## 相关函数类

- Action0/Action1/Action2/Action3    订阅事件处理接口, 进行回调处理
- Func0/Func1/Func../Fun9/FunN       函数接口
- Functions                                             Funx或者Object转换 FunN

## 操作符(函数变换,变成新的Observable) 

 <T> Func1<Observer<T>, Subscription>

- OperationMap
- OperationTake
- OperationFilter
- OperationScan
- ...
- OperationMerge



流程

Observable 创建     核心create

```
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

```
toObservable
just
create
```

变换

订阅  执行

```
public Subscription subscribe(Observer<T> observer) {
    if (onSubscribe == null) {
        throw new IllegalStateException("onSubscribe function can not be null.");
        // the subscribe function can also be overridden but generally that's not the appropriate approach so I won't mention that in the exception
    }
    if (isTrusted) {
        return onSubscribe.call(observer);
    } else {
        AtomicObservableSubscription subscription = new AtomicObservableSubscription();
        return subscription.wrap(onSubscribe.call(new AtomicObserver<T>(subscription, observer)));
    }
};
```