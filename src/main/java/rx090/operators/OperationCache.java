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
package rx090.operators;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import rx090.Observable;
import rx090.Observer;
import rx090.Subscription;
import rx090.subjects.ReplaySubject;
import rx090.subscriptions.BooleanSubscription;
import rx090.util.functions.Action1;
import rx090.util.functions.Func1;

/**
 * Similar to {@link Observable#replay()} except that this auto-subscribes to the source sequence.
 * <p>
 * This is useful when returning an Observable that you wish to cache responses but can't control the
 * subscribe/unsubscribe behavior of all the Observers.
 * <p>
 * NOTE: You sacrifice the ability to unsubscribe from the origin with this operator so be careful to not
 * use this on infinite or very large sequences that will use up memory. This is similar to
 * the {@link Observable#toList()} operator in this caution.
 * 
 */
public class OperationCache {

    public static <T> Func1<Observer<T>, Subscription> cache(final Observable<T> source) {
        return new Func1<Observer<T>, Subscription>() {

            final AtomicBoolean subscribed = new AtomicBoolean(false);
            private final ReplaySubject<T> cache = ReplaySubject.create();

            @Override
            public Subscription call(Observer<T> observer) {
                if (subscribed.compareAndSet(false, true)) {
                    // subscribe to the source once
                    source.subscribe(cache);
                    /*
                     * Note that we will never unsubscribe from 'source' as we want to receive and cache all of its values.
                     * 
                     * This means this should never be used on an infinite or very large sequence, similar to toList().
                     */
                }

                return cache.subscribe(observer);
            }

        };
    }

    public static class UnitTest {

        @Test
        public void testCache() throws InterruptedException {
            final AtomicInteger counter = new AtomicInteger();
            Observable<String> o = Observable.create(cache(Observable.create(new Func1<Observer<String>, Subscription>() {

                @Override
                public Subscription call(final Observer<String> observer) {
                    final BooleanSubscription subscription = new BooleanSubscription();
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            counter.incrementAndGet();
                            System.out.println("published observable being executed");
                            observer.onNext("one");
                            observer.onCompleted();
                        }
                    }).start();
                    return subscription;
                }
            })));

            // we then expect the following 2 subscriptions to get that same value
            final CountDownLatch latch = new CountDownLatch(2);

            // subscribe once
            o.subscribe(new Action1<String>() {

                @Override
                public void call(String v) {
                    assertEquals("one", v);
                    System.out.println("v: " + v);
                    latch.countDown();
                }
            });

            // subscribe again
            o.subscribe(new Action1<String>() {

                @Override
                public void call(String v) {
                    assertEquals("one", v);
                    System.out.println("v: " + v);
                    latch.countDown();
                }
            });

            if (!latch.await(1000, TimeUnit.MILLISECONDS)) {
                fail("subscriptions did not receive values");
            }
            assertEquals(1, counter.get());
        }
    }

}
