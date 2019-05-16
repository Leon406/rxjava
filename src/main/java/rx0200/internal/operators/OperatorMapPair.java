/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package rx0200.internal.operators;

import rx0200.Observable;
import rx0200.Observable.Operator;
import rx0200.Subscriber;
import rx0200.exceptions.OnErrorThrowable;
import rx0200.functions.Func1;
import rx0200.functions.Func2;

/**
 * An {@link Operator} that pairs up items emitted by a source {@link Observable} with the sequence of items
 * emitted by the {@code Observable} that is derived from each item by means of a selector, and emits the
 * results of this pairing.
 *
 * @param <T>
 *            the type of items emitted by the source {@code Observable}
 * @param <U>
 *            the type of items emitted by the derived {@code Observable}s
 * @param <R>
 *            the type of items to be emitted by this {@code Operator}
 */
public final class OperatorMapPair<T, U, R> implements Operator<Observable<? extends R>, T> {

    /**
     * Creates the function that generates a {@code Observable} based on an item emitted by another {@code Observable}.
     * 
     * @param selector
     *            a function that accepts an item and returns an {@code Iterable} of corresponding items
     * @return a function that converts an item emitted by the source {@code Observable} into an {@code Observable} that emits the items generated by {@code selector} operating on that item
     */
    public static <T, U> Func1<T, Observable<U>> convertSelector(final Func1<? super T, ? extends Iterable<? extends U>> selector) {
        return new Func1<T, Observable<U>>() {
            @Override
            public Observable<U> call(T t1) {
                return Observable.from(selector.call(t1));
            }
        };
    }

    final Func1<? super T, ? extends Observable<? extends U>> collectionSelector;
    final Func2<? super T, ? super U, ? extends R> resultSelector;

    public OperatorMapPair(final Func1<? super T, ? extends Observable<? extends U>> collectionSelector, final Func2<? super T, ? super U, ? extends R> resultSelector) {
        this.collectionSelector = collectionSelector;
        this.resultSelector = resultSelector;
    }

    @Override
    public Subscriber<? super T> call(final Subscriber<? super Observable<? extends R>> o) {
        return new Subscriber<T>(o) {

            @Override
            public void onCompleted() {
                o.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                o.onError(e);
            }

            @Override
            public void onNext(final T outer) {
                try {
                    o.onNext(collectionSelector.call(outer).map(new Func1<U, R>() {

                        @Override
                        public R call(U inner) {
                            return resultSelector.call(outer, inner);
                        }
                    }));
                } catch (Throwable e) {
                    o.onError(OnErrorThrowable.addValueAsLastCause(e, outer));
                }
            }

        };
    }

}