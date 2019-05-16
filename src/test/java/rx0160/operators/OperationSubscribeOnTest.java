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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static rx0160.operators.OperationSubscribeOn.*;

import org.junit.Test;

import rx0160.Observable;
import rx0160.Observer;
import rx0160.Scheduler;
import rx0160.Subscription;
import rx0160.schedulers.Schedulers;
import rx0160.test.OperatorTester;
import rx0160.util.functions.Action0;
import rx0160.util.functions.Func2;

public class OperationSubscribeOnTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testSubscribeOn() {
        Observable<Integer> w = Observable.from(1, 2, 3);

        Scheduler scheduler = spy(OperatorTester.forwardingScheduler(Schedulers.immediate()));

        Observer<Integer> observer = mock(Observer.class);
        Subscription subscription = Observable.create(subscribeOn(w, scheduler)).subscribe(observer);

        verify(scheduler, times(1)).schedule(isNull(), any(Func2.class));
        subscription.unsubscribe();
        verify(scheduler, times(1)).schedule(any(Action0.class));
        verifyNoMoreInteractions(scheduler);

        verify(observer, times(1)).onNext(1);
        verify(observer, times(1)).onNext(2);
        verify(observer, times(1)).onNext(3);
        verify(observer, times(1)).onCompleted();
    }
}
