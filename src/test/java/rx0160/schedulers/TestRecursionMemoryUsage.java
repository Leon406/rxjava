/**
 * Copyright 2013 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx0160.schedulers;

import rx0160.Observable;
import rx0160.Observable.OnSubscribeFunc;
import rx0160.Observer;
import rx0160.Scheduler;
import rx0160.Subscription;
import rx0160.subscriptions.Subscriptions;
import rx0160.util.functions.Action0;
import rx0160.util.functions.Action1;
import rx0160.util.functions.Func2;

/**
 * Used for manual testing of memory leaks with recursive schedulers.
 * 
 */
public class TestRecursionMemoryUsage {

    public static void main(String args[]) {
        usingFunc2(Schedulers.newThread());
        usingAction0(Schedulers.newThread());

        usingFunc2(Schedulers.currentThread());
        usingAction0(Schedulers.currentThread());

        usingFunc2(Schedulers.threadPoolForComputation());
        usingAction0(Schedulers.threadPoolForComputation());
    }

    protected static void usingFunc2(final Scheduler scheduler) {
        System.out.println("************ usingFunc2: " + scheduler);
        Observable.create(new OnSubscribeFunc<Long>() {

            @Override
            public Subscription onSubscribe(final Observer<? super Long> o) {
                return scheduler.schedule(0L, new Func2<Scheduler, Long, Subscription>() {

                    @Override
                    public Subscription call(Scheduler innerScheduler, Long i) {
                        i++;
                        if (i % 500000 == 0) {
                            System.out.println(i + "  Total Memory: " + Runtime.getRuntime().totalMemory() + "  Free: " + Runtime.getRuntime().freeMemory());
                            o.onNext(i);
                        }
                        if (i == 100000000L) {
                            o.onCompleted();
                            return Subscriptions.empty();
                        }

                        return innerScheduler.schedule(i, this);
                    }
                });
            }
        }).toBlockingObservable().last();
    }

    protected static void usingAction0(final Scheduler scheduler) {
        System.out.println("************ usingAction0: " + scheduler);
        Observable.create(new OnSubscribeFunc<Long>() {

            @Override
            public Subscription onSubscribe(final Observer<? super Long> o) {
                return scheduler.schedule(new Action1<Action0>() {

                    private long i = 0;

                    @Override
                    public void call(Action0 self) {
                        i++;
                        if (i % 500000 == 0) {
                            System.out.println(i + "  Total Memory: " + Runtime.getRuntime().totalMemory() + "  Free: " + Runtime.getRuntime().freeMemory());
                            o.onNext(i);
                        }
                        if (i == 100000000L) {
                            o.onCompleted();
                            return;
                        }
                        self.call();
                    }
                });
            }
        }).toBlockingObservable().last();
    }
}
