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
package rx0140.operators;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.Mockito;

import rx0140.Observable;
import rx0140.Observable.OnSubscribeFunc;
import rx0140.Observer;
import rx0140.Subscription;
import rx0140.util.functions.Func1;

/**
 * Filters an Observable by discarding any items it emits that do not meet some test.
 * <p>
 * <img width="640" src="https://github.com/Netflix/RxJava/wiki/images/rx-operators/filter.png">
 */
public final class OperationFilter<T> {

    public static <T> OnSubscribeFunc<T> filter(Observable<? extends T> that, Func1<? super T, Boolean> predicate) {
        return new Filter<T>(that, predicate);
    }

    private static class Filter<T> implements OnSubscribeFunc<T> {

        private final Observable<? extends T> that;
        private final Func1<? super T, Boolean> predicate;

        public Filter(Observable<? extends T> that, Func1<? super T, Boolean> predicate) {
            this.that = that;
            this.predicate = predicate;
        }

        public Subscription onSubscribe(final Observer<? super T> observer) {
            final SafeObservableSubscription subscription = new SafeObservableSubscription();
            return subscription.wrap(that.subscribe(new Observer<T>() {
                public void onNext(T value) {
                    try {
                        if (predicate.call(value)) {
                            observer.onNext(value);
                        }
                    } catch (Throwable ex) {
                        observer.onError(ex);
                        // this will work if the sequence is asynchronous, it will have no effect on a synchronous observable
                        subscription.unsubscribe();
                    }
                }

                public void onError(Throwable ex) {
                    observer.onError(ex);
                }

                public void onCompleted() {
                    observer.onCompleted();
                }
            }));
        }

    }

    public static class UnitTest {

        @Test
        public void testFilter() {
            Observable<String> w = Observable.from("one", "two", "three");
            Observable<String> observable = Observable.create(filter(w, new Func1<String, Boolean>() {

                @Override
                public Boolean call(String t1) {
                    return t1.equals("two");
                }
            }));

            @SuppressWarnings("unchecked")
            Observer<String> aObserver = mock(Observer.class);
            observable.subscribe(aObserver);
            verify(aObserver, Mockito.never()).onNext("one");
            verify(aObserver, times(1)).onNext("two");
            verify(aObserver, Mockito.never()).onNext("three");
            verify(aObserver, Mockito.never()).onError(any(Throwable.class));
            verify(aObserver, times(1)).onCompleted();
        }
    }
}
