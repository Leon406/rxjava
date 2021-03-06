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
package rx090.observables;

import rx090.Observable;
import rx090.Observer;
import rx090.Subscription;
import rx090.util.functions.Func1;

public abstract class ConnectableObservable<T> extends Observable<T> {

    protected ConnectableObservable(Func1<Observer<T>, Subscription> onSubscribe) {
        super(onSubscribe);
    }

    public abstract Subscription connect();

}
