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

import static org.mockito.Mockito.*;

import org.junit.Test;

import rx0140.Observable;
import rx0140.Observable.OnSubscribeFunc;
import rx0140.Observer;
import rx0140.Subscription;
import rx0140.util.functions.Func0;

/**
 * Do not create the Observable until an Observer subscribes; create a fresh Observable on each
 * subscription.
 * <p>
 * <img width="640" src="https://github.com/Netflix/RxJava/wiki/images/rx-operators/defer.png">
 * <p>
 * Pass defer an Observable factory function (a function that generates Observables), and defer will
 * return an Observable that will call this function to generate its Observable sequence afresh
 * each time a new Observer subscribes.
 */
public final class OperationDefer {

    public static <T> OnSubscribeFunc<T> defer(final Func0<? extends Observable<? extends T>> observableFactory) {

        return new OnSubscribeFunc<T>() {
            @Override
            public Subscription onSubscribe(Observer<? super T> observer) {
                Observable<? extends T> obs = observableFactory.call();
                return obs.subscribe(observer);
            }
        };

    }

    public static class UnitTest {
        @Test
        @SuppressWarnings("unchecked")
        public void testDefer() throws Throwable {

            Func0<Observable<String>> factory = mock(Func0.class);

            Observable<String> firstObservable = Observable.from("one", "two");
            Observable<String> secondObservable = Observable.from("three", "four");
            when(factory.call()).thenReturn(firstObservable, secondObservable);

            Observable<String> deferred = Observable.defer(factory);

            verifyZeroInteractions(factory);

            Observer<String> firstObserver = mock(Observer.class);
            deferred.subscribe(firstObserver);

            verify(factory, times(1)).call();
            verify(firstObserver, times(1)).onNext("one");
            verify(firstObserver, times(1)).onNext("two");
            verify(firstObserver, times(0)).onNext("three");
            verify(firstObserver, times(0)).onNext("four");
            verify(firstObserver, times(1)).onCompleted();

            Observer<String> secondObserver = mock(Observer.class);
            deferred.subscribe(secondObserver);

            verify(factory, times(2)).call();
            verify(secondObserver, times(0)).onNext("one");
            verify(secondObserver, times(0)).onNext("two");
            verify(secondObserver, times(1)).onNext("three");
            verify(secondObserver, times(1)).onNext("four");
            verify(secondObserver, times(1)).onCompleted();

        }
    }

}
