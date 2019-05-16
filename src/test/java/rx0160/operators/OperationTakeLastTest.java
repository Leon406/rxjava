/**
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx0160.operators;

import java.util.concurrent.TimeUnit;
import static org.mockito.Mockito.*;
import static rx0160.operators.OperationTakeLast.*;

import org.junit.Test;
import org.mockito.InOrder;

import rx0160.Observable;
import rx0160.Observer;
import rx0160.schedulers.TestScheduler;
import rx0160.subjects.PublishSubject;

public class OperationTakeLastTest {

    @Test
    public void testTakeLastEmpty() {
        Observable<String> w = Observable.empty();
        Observable<String> take = Observable.create(takeLast(w, 2));

        @SuppressWarnings("unchecked")
        Observer<String> aObserver = mock(Observer.class);
        take.subscribe(aObserver);
        verify(aObserver, never()).onNext(any(String.class));
        verify(aObserver, never()).onError(any(Throwable.class));
        verify(aObserver, times(1)).onCompleted();
    }

    @Test
    public void testTakeLast1() {
        Observable<String> w = Observable.from("one", "two", "three");
        Observable<String> take = Observable.create(takeLast(w, 2));

        @SuppressWarnings("unchecked")
        Observer<String> aObserver = mock(Observer.class);
        InOrder inOrder = inOrder(aObserver);
        take.subscribe(aObserver);
        inOrder.verify(aObserver, times(1)).onNext("two");
        inOrder.verify(aObserver, times(1)).onNext("three");
        verify(aObserver, never()).onNext("one");
        verify(aObserver, never()).onError(any(Throwable.class));
        verify(aObserver, times(1)).onCompleted();
    }

    @Test
    public void testTakeLast2() {
        Observable<String> w = Observable.from("one");
        Observable<String> take = Observable.create(takeLast(w, 10));

        @SuppressWarnings("unchecked")
        Observer<String> aObserver = mock(Observer.class);
        take.subscribe(aObserver);
        verify(aObserver, times(1)).onNext("one");
        verify(aObserver, never()).onError(any(Throwable.class));
        verify(aObserver, times(1)).onCompleted();
    }

    @Test
    public void testTakeLastWithZeroCount() {
        Observable<String> w = Observable.from("one");
        Observable<String> take = Observable.create(takeLast(w, 0));

        @SuppressWarnings("unchecked")
        Observer<String> aObserver = mock(Observer.class);
        take.subscribe(aObserver);
        verify(aObserver, never()).onNext("one");
        verify(aObserver, never()).onError(any(Throwable.class));
        verify(aObserver, times(1)).onCompleted();
    }

    @Test
    public void testTakeLastWithNull() {
        Observable<String> w = Observable.from("one", null, "three");
        Observable<String> take = Observable.create(takeLast(w, 2));

        @SuppressWarnings("unchecked")
        Observer<String> aObserver = mock(Observer.class);
        take.subscribe(aObserver);
        verify(aObserver, never()).onNext("one");
        verify(aObserver, times(1)).onNext(null);
        verify(aObserver, times(1)).onNext("three");
        verify(aObserver, never()).onError(any(Throwable.class));
        verify(aObserver, times(1)).onCompleted();
    }

    @Test
    public void testTakeLastWithNegativeCount() {
        Observable<String> w = Observable.from("one");
        Observable<String> take = Observable.create(takeLast(w, -1));

        @SuppressWarnings("unchecked")
        Observer<String> aObserver = mock(Observer.class);
        take.subscribe(aObserver);
        verify(aObserver, never()).onNext("one");
        verify(aObserver, times(1)).onError(
                any(IndexOutOfBoundsException.class));
        verify(aObserver, never()).onCompleted();
    }
    
    @Test
    public void takeLastTimed() {
        TestScheduler scheduler = new TestScheduler();
        
        PublishSubject<Object> source = PublishSubject.create();
        
        Observable<Object> result = source.takeLast(1, TimeUnit.SECONDS, scheduler);
        
        Observer<Object> o = mock(Observer.class);
        
        InOrder inOrder = inOrder(o);

        result.subscribe(o);
        
        source.onNext(1); // T: 0ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(2); // T: 250ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(3); // T: 500ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(4); // T: 750ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(5); // T: 1000ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onCompleted(); // T: 1250ms
        
        inOrder.verify(o, times(1)).onNext(2);
        inOrder.verify(o, times(1)).onNext(3);
        inOrder.verify(o, times(1)).onNext(4);
        inOrder.verify(o, times(1)).onNext(5);
        inOrder.verify(o, times(1)).onCompleted();
        
        verify(o, never()).onError(any(Throwable.class));
    }
    @Test
    public void takeLastTimedDelayCompletion() {
        TestScheduler scheduler = new TestScheduler();
        
        PublishSubject<Object> source = PublishSubject.create();
        
        Observable<Object> result = source.takeLast(1, TimeUnit.SECONDS, scheduler);
        
        Observer<Object> o = mock(Observer.class);
        
        InOrder inOrder = inOrder(o);
        
        result.subscribe(o);
        
        source.onNext(1); // T: 0ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(2); // T: 250ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(3); // T: 500ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(4); // T: 750ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(5); // T: 1000ms
        scheduler.advanceTimeBy(1250, TimeUnit.MILLISECONDS);
        source.onCompleted(); // T: 2250ms
        
        inOrder.verify(o, times(1)).onCompleted();

        verify(o, never()).onNext(any());
        verify(o, never()).onError(any(Throwable.class));
    }
    @Test
    public void takeLastTimedWithCapacity() {
        TestScheduler scheduler = new TestScheduler();
        
        PublishSubject<Object> source = PublishSubject.create();
        
        Observable<Object> result = source.takeLast(2, 1, TimeUnit.SECONDS, scheduler);
        
        Observer<Object> o = mock(Observer.class);
        
        InOrder inOrder = inOrder(o);

        result.subscribe(o);
        
        source.onNext(1); // T: 0ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(2); // T: 250ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(3); // T: 500ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(4); // T: 750ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(5); // T: 1000ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onCompleted(); // T: 1250ms
        
        inOrder.verify(o, times(1)).onNext(4);
        inOrder.verify(o, times(1)).onNext(5);
        inOrder.verify(o, times(1)).onCompleted();
        
        verify(o, never()).onError(any(Throwable.class));
    }
    static final class CustomException extends RuntimeException {
        
    }
    @Test
    public void takeLastTimedThrowingSource() {
        TestScheduler scheduler = new TestScheduler();
        
        PublishSubject<Object> source = PublishSubject.create();
        
        Observable<Object> result = source.takeLast(1, TimeUnit.SECONDS, scheduler);
        
        Observer<Object> o = mock(Observer.class);
        
        InOrder inOrder = inOrder(o);

        result.subscribe(o);
        
        source.onNext(1); // T: 0ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(2); // T: 250ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(3); // T: 500ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(4); // T: 750ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(5); // T: 1000ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onError(new CustomException()); // T: 1250ms
        
        inOrder.verify(o, times(1)).onError(any(CustomException.class));
        
        verify(o, never()).onNext(any());
        verify(o, never()).onCompleted();
    }
    
    @Test
    public void takeLastTimedWithZeroCapacity() {
        TestScheduler scheduler = new TestScheduler();
        
        PublishSubject<Object> source = PublishSubject.create();
        
        Observable<Object> result = source.takeLast(0, 1, TimeUnit.SECONDS, scheduler);
        
        Observer<Object> o = mock(Observer.class);
        
        InOrder inOrder = inOrder(o);

        result.subscribe(o);
        
        source.onNext(1); // T: 0ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(2); // T: 250ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(3); // T: 500ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(4); // T: 750ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onNext(5); // T: 1000ms
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        source.onCompleted(); // T: 1250ms
        
        inOrder.verify(o, times(1)).onCompleted();
        
        verify(o, never()).onNext(any());
        verify(o, never()).onError(any(Throwable.class));
    }
}
