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

import rx0160.Observable;
import rx0160.Observable.OnSubscribeFunc;
import rx0160.Observer;
import rx0160.Subscription;
import rx0160.subscriptions.CompositeSubscription;
import rx0160.subscriptions.Subscriptions;
import rx0160.util.functions.Func0;
import rx0160.util.functions.Func1;

/**
 * Constructs an observable sequence that depends on a resource object.
 */
public class OperationUsing {

    public static <T, RESOURCE extends Subscription> OnSubscribeFunc<T> using(
            final Func0<RESOURCE> resourceFactory,
            final Func1<RESOURCE, Observable<T>> observableFactory) {
        return new OnSubscribeFunc<T>() {
            @Override
            public Subscription onSubscribe(Observer<? super T> observer) {
                Subscription resourceSubscription = Subscriptions.empty();
                try {
                    RESOURCE resource = resourceFactory.call();
                    if (resource != null) {
                        resourceSubscription = resource;
                    }
                    Observable<T> observable = observableFactory.call(resource);
                    SafeObservableSubscription subscription = new SafeObservableSubscription();
                    // Use SafeObserver to guarantee resourceSubscription will
                    // be unsubscribed.
                    return subscription.wrap(new CompositeSubscription(
                            observable.subscribe(new SafeObserver<T>(
                                    subscription, observer)),
                            resourceSubscription));
                } catch (Throwable e) {
                    resourceSubscription.unsubscribe();
                    return Observable.<T> error(e).subscribe(observer);
                }
            }
        };
    }
}
