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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import rx0140.Observable;
import rx0140.Observable.OnSubscribeFunc;
import rx0140.Observer;
import rx0140.Subscription;

/**
 * Returns the element at a specified index in a sequence.
 */
public class OperationElementAt {

    /**
     * Returns the element at a specified index in a sequence.
     * 
     * @param source
     *            Observable sequence to return the element from.
     * @param index
     *            The zero-based index of the element to retrieve.
     * 
     * @return An observable sequence that produces the element at the specified
     *         position in the source sequence.
     * 
     * @throws IndexOutOfBoundsException
     *             Index is greater than or equal to the number of elements in
     *             the source sequence.
     * @throws IndexOutOfBoundsException
     *             Index is less than 0.
     */
    public static <T> OnSubscribeFunc<T> elementAt(Observable<? extends T> source, int index) {
        return new ElementAt<T>(source, index, null, false);
    }

    /**
     * Returns the element at a specified index in a sequence or the default
     * value if the index is out of range.
     * 
     * @param source
     *            Observable sequence to return the element from.
     * @param index
     *            The zero-based index of the element to retrieve.
     * @param defaultValue
     *            The default value.
     * 
     * @return An observable sequence that produces the element at the specified
     *         position in the source sequence, or the default value if the
     *         index is outside the bounds of the source sequence.
     * 
     * @throws IndexOutOfBoundsException
     *             Index is less than 0.
     */
    public static <T> OnSubscribeFunc<T> elementAtOrDefault(Observable<? extends T> source, int index, T defaultValue) {
        return new ElementAt<T>(source, index, defaultValue, true);
    }

    private static class ElementAt<T> implements OnSubscribeFunc<T> {

        private final Observable<? extends T> source;
        private final int index;
        private final boolean hasDefault;
        private final T defaultValue;

        private ElementAt(Observable<? extends T> source, int index,
                T defaultValue, boolean hasDefault) {
            this.source = source;
            this.index = index;
            this.defaultValue = defaultValue;
            this.hasDefault = hasDefault;
        }

        @Override
        public Subscription onSubscribe(final Observer<? super T> observer) {
            final SafeObservableSubscription subscription = new SafeObservableSubscription();
            return subscription.wrap(source.subscribe(new Observer<T>() {

                private AtomicInteger counter = new AtomicInteger();

                @Override
                public void onNext(T value) {
                    try {
                        int currentIndex = counter.getAndIncrement();
                        if (currentIndex == index) {
                            observer.onNext(value);
                            observer.onCompleted();
                        } else if (currentIndex > index) {
                            // this will work if the sequence is asynchronous,
                            // it will have no effect on a synchronous observable
                            subscription.unsubscribe();
                        }
                    } catch (Throwable ex) {
                        observer.onError(ex);
                        // this will work if the sequence is asynchronous, it
                        // will have no effect on a synchronous observable
                        subscription.unsubscribe();
                    }

                }

                @Override
                public void onError(Throwable ex) {
                    observer.onError(ex);
                }

                @Override
                public void onCompleted() {
                    if (index < 0) {
                        observer.onError(new IndexOutOfBoundsException(index + " is out of bounds"));
                    } else if (counter.get() <= index) {
                        if (hasDefault) {
                            observer.onNext(defaultValue);
                            observer.onCompleted();
                        } else {
                            observer.onError(new IndexOutOfBoundsException(index + " is out of bounds"));
                        }
                    }
                }
            }));
        }
    }

    public static class UnitTest {

        @Test
        public void testElementAt() {
            Observable<Integer> w = Observable.from(1, 2);
            Observable<Integer> observable = Observable.create(elementAt(w, 1));

            @SuppressWarnings("unchecked")
            Observer<Integer> aObserver = mock(Observer.class);
            observable.subscribe(aObserver);
            verify(aObserver, never()).onNext(1);
            verify(aObserver, times(1)).onNext(2);
            verify(aObserver, never()).onError(
                    any(Throwable.class));
            verify(aObserver, times(1)).onCompleted();
        }

        @Test
        public void testElementAtWithMinusIndex() {
            Observable<Integer> w = Observable.from(1, 2);
            Observable<Integer> observable = Observable
                    .create(elementAt(w, -1));

            try {
                Iterator<Integer> iter = OperationToIterator
                        .toIterator(observable);
                assertTrue(iter.hasNext());
                iter.next();
                fail("expect an IndexOutOfBoundsException when index is out of bounds");
            } catch (IndexOutOfBoundsException e) {
            }
        }

        @Test
        public void testElementAtWithIndexOutOfBounds()
                throws InterruptedException, ExecutionException {
            Observable<Integer> w = Observable.from(1, 2);
            Observable<Integer> observable = Observable.create(elementAt(w, 2));
            try {
                Iterator<Integer> iter = OperationToIterator
                        .toIterator(observable);
                assertTrue(iter.hasNext());
                iter.next();
                fail("expect an IndexOutOfBoundsException when index is out of bounds");
            } catch (IndexOutOfBoundsException e) {
            }
        }

        @Test
        public void testElementAtOrDefault() throws InterruptedException,
                ExecutionException {
            Observable<Integer> w = Observable.from(1, 2);
            Observable<Integer> observable = Observable
                    .create(elementAtOrDefault(w, 1, 0));

            @SuppressWarnings("unchecked")
            Observer<Integer> aObserver = mock(Observer.class);
            observable.subscribe(aObserver);
            verify(aObserver, never()).onNext(1);
            verify(aObserver, times(1)).onNext(2);
            verify(aObserver, never()).onError(any(Throwable.class));
            verify(aObserver, times(1)).onCompleted();
        }

        @Test
        public void testElementAtOrDefaultWithIndexOutOfBounds()
                throws InterruptedException, ExecutionException {
            Observable<Integer> w = Observable.from(1, 2);
            Observable<Integer> observable = Observable
                    .create(elementAtOrDefault(w, 2, 0));

            @SuppressWarnings("unchecked")
            Observer<Integer> aObserver = mock(Observer.class);
            observable.subscribe(aObserver);
            verify(aObserver, never()).onNext(1);
            verify(aObserver, never()).onNext(2);
            verify(aObserver, times(1)).onNext(0);
            verify(aObserver, never()).onError(any(Throwable.class));
            verify(aObserver, times(1)).onCompleted();
        }

        @Test
        public void testElementAtOrDefaultWithMinusIndex() {
            Observable<Integer> w = Observable.from(1, 2);
            Observable<Integer> observable = Observable
                    .create(elementAtOrDefault(w, -1, 0));

            try {
                Iterator<Integer> iter = OperationToIterator
                        .toIterator(observable);
                assertTrue(iter.hasNext());
                iter.next();
                fail("expect an IndexOutOfBoundsException when index is out of bounds");
            } catch (IndexOutOfBoundsException e) {
            }
        }
    }
}
