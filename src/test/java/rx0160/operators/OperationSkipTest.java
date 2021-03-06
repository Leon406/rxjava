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
import static rx0160.operators.OperationSkip.*;

import org.junit.Test;
import org.mockito.InOrder;

import rx0160.Observable;
import rx0160.Observer;
import rx0160.concurrency.TestScheduler;
import rx0160.subjects.PublishSubject;

public class OperationSkipTest {

    @Test
    public void testSkip1() {
        Observable<String> w = Observable.from("one", "two", "three");
        Observable<String> skip = Observable.create(skip(w, 2));

        @SuppressWarnings("unchecked")
        Observer<String> aObserver = mock(Observer.class);
        skip.subscribe(aObserver);
        verify(aObserver, never()).onNext("one");
        verify(aObserver, never()).onNext("two");
        verify(aObserver, times(1)).onNext("three");
        verify(aObserver, never()).onError(any(Throwable.class));
        verify(aObserver, times(1)).onCompleted();
    }

    @Test
    public void testSkip2() {
        Observable<String> w = Observable.from("one", "two", "three");
        Observable<String> skip = Observable.create(skip(w, 1));

        @SuppressWarnings("unchecked")
        Observer<String> aObserver = mock(Observer.class);
        skip.subscribe(aObserver);
        verify(aObserver, never()).onNext("one");
        verify(aObserver, times(1)).onNext("two");
        verify(aObserver, times(1)).onNext("three");
        verify(aObserver, never()).onError(any(Throwable.class));
        verify(aObserver, times(1)).onCompleted();
    }
    
    @Test
    public void testSkipTimed() {
        TestScheduler scheduler = new TestScheduler();
        
        PublishSubject<Integer> source = PublishSubject.create();
        
        Observable<Integer> result = source.skip(1, TimeUnit.SECONDS, scheduler);
        
        Observer<Object> o = mock(Observer.class);
        
        result.subscribe(o);
        
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        
        source.onNext(4);
        source.onNext(5);
        source.onNext(6);
        
        source.onCompleted();
        
        InOrder inOrder = inOrder(o);
        
        inOrder.verify(o, never()).onNext(1);
        inOrder.verify(o, never()).onNext(2);
        inOrder.verify(o, never()).onNext(3);
        inOrder.verify(o).onNext(4);
        inOrder.verify(o).onNext(5);
        inOrder.verify(o).onNext(6);
        inOrder.verify(o).onCompleted();
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onError(any(Throwable.class));
    }
    @Test
    public void testSkipTimedFinishBeforeTime() {
        TestScheduler scheduler = new TestScheduler();
        
        PublishSubject<Integer> source = PublishSubject.create();
        
        Observable<Integer> result = source.skip(1, TimeUnit.SECONDS, scheduler);
        
        Observer<Object> o = mock(Observer.class);
        
        result.subscribe(o);
        
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onCompleted();
        
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        
        InOrder inOrder = inOrder(o);
        
        inOrder.verify(o).onCompleted();
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onNext(any());
        verify(o, never()).onError(any(Throwable.class));
    }
    static class CustomException extends RuntimeException { }
    @Test
    public void testSkipTimedErrorBeforeTime() {
        TestScheduler scheduler = new TestScheduler();
        
        PublishSubject<Integer> source = PublishSubject.create();
        
        Observable<Integer> result = source.skip(1, TimeUnit.SECONDS, scheduler);
        
        Observer<Object> o = mock(Observer.class);
        
        result.subscribe(o);
        
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onError(new CustomException());
        
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        
        InOrder inOrder = inOrder(o);
        
        inOrder.verify(o).onError(any(CustomException.class));
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onNext(any());
        verify(o, never()).onCompleted();
    }
    @Test
    public void testSkipTimedErrorAfterTime() {
        TestScheduler scheduler = new TestScheduler();
        
        PublishSubject<Integer> source = PublishSubject.create();
        
        Observable<Integer> result = source.skip(1, TimeUnit.SECONDS, scheduler);
        
        Observer<Object> o = mock(Observer.class);
        
        result.subscribe(o);
        
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        
        source.onNext(4);
        source.onNext(5);
        source.onNext(6);
        
        source.onError(new CustomException());
        
        InOrder inOrder = inOrder(o);
        
        inOrder.verify(o, never()).onNext(1);
        inOrder.verify(o, never()).onNext(2);
        inOrder.verify(o, never()).onNext(3);
        inOrder.verify(o).onNext(4);
        inOrder.verify(o).onNext(5);
        inOrder.verify(o).onNext(6);
        inOrder.verify(o).onError(any(CustomException.class));
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onCompleted();
        
    }
}
