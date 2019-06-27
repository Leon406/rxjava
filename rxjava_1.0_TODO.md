# Rxjava 1.0

### Why this Version

- 第一个1.0正式版,功能完善,bug 相对较少
- 分包合理,命名规范,没有冗余代码
- 加入背压   支持
- 代码简化
  - 简化操作符 用lift统一实现
  - 简化Schedulers





首先介绍几个新概念

- Operator 函数接口  

   就是前面介绍 Func1接口,

   call调用返回Subscriber

   所有OperatorXxx操作符都实现Operator 接口

- OnSubscribe函数接口

    Observable#create时传入

    Observable#subscribe调用时 执行OnSubscribe#call(Subscriber ) 

- Subscriber  

   进行所有事件的回调

   实现了  Observer和Subscription

   持有Producer,进行request处理

   Subscriber  之间传递SubscriptionList ,可以进行订阅处理

- SubscriptionList  

   所有Subscription列表,进行统一处理

- Producer函数接口

  request指定Producer生成item数量

- lift 操作

  将Operator  转换成Observable

  Observable#subscribe时  执行 onSubscribe#call(st)    (st 为Operator#call返回的Subscriber)

