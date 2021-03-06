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

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import rx090.Observable;
import rx090.Observer;
import rx090.Subscription;
import rx090.util.functions.Action0;
import rx090.util.functions.Func1;

public final class OperationFinally {

    /**
     * Call a given action when a sequence completes (with or without an
     * exception).  The returned observable is exactly as threadsafe as the
     * source observable.
     * <p/>
     * Note that "finally" is a Java reserved word and cannot be an identifier,
     * so we use "finallyDo".
     *
     * @param sequence An observable sequence of elements
     * @param action An action to be taken when the sequence is complete or throws an exception
     * @return An observable sequence with the same elements as the input.
     *         After the last element is consumed (and {@link Observer#onCompleted} has been called),
     *         or after an exception is thrown (and {@link Observer#onError} has been called),
     *         the given action will be called.
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh212133(v=vs.103).aspx">MSDN Observable.Finally method</a>
     */
    public static <T> Func1<Observer<T>, Subscription> finallyDo(final Observable<T> sequence, final Action0 action) {
        return new Func1<Observer<T>, Subscription>() {
            @Override
            public Subscription call(Observer<T> observer) {
                return new Finally<T>(sequence, action).call(observer);
            }
        };
    }

    private static class Finally<T> implements Func1<Observer<T>, Subscription> {
        private final Observable<T> sequence;
        private final Action0 finalAction;

        Finally(final Observable<T> sequence, Action0 finalAction) {
            this.sequence = sequence;
            this.finalAction = finalAction;
        }

        public Subscription call(Observer<T> observer) {
            return sequence.subscribe(new FinallyObserver(observer));
        }

        private class FinallyObserver implements Observer<T> {
            private final Observer<T> observer;

            FinallyObserver(Observer<T> observer) {
                this.observer = observer;
            }

            @Override
            public void onCompleted() {
                try {
                    observer.onCompleted();
                } finally {
                    finalAction.call();
                }
            }

            @Override
            public void onError(Exception e) {
                try {
                    observer.onError(e);
                } finally {
                    finalAction.call();
                }
            }

            @Override
            public void onNext(T args) {
                observer.onNext(args);
            }
        }
    }

    public static class UnitTest {
        private Action0 aAction0;
        private Observer<String> aObserver;
        
        @SuppressWarnings("unchecked") // mocking has to be unchecked, unfortunately
        @Before
        public void before() {
            aAction0 = mock(Action0.class);
            aObserver = mock(Observer.class);
        }
        
        private void checkActionCalled(Observable<String> input) {
            Observable.create(finallyDo(input, aAction0)).subscribe(aObserver);
            verify(aAction0, times(1)).call();
        }
        
        @Test
        public void testFinallyCalledOnComplete() {
            checkActionCalled(Observable.from(new String[] {"1", "2", "3"}));
        }
        
        @Test
        public void testFinallyCalledOnError() {
            checkActionCalled(Observable.<String>error(new RuntimeException("expected")));
        }
    }
}
