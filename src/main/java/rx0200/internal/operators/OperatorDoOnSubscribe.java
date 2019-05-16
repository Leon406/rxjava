/**
 * Copyright 2014 Netflix, Inc.
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
package rx0200.internal.operators;

import rx0200.Observable.Operator;
import rx0200.Subscriber;
import rx0200.functions.Action0;

/**
 * This operator modifies an {@link rx0200.Observable} so a given action is invoked when the {@link rx0200.Observable} is subscribed.
 * @param <T> The type of the elements in the {@link rx0200.Observable} that this operator modifies
 */
public class OperatorDoOnSubscribe<T> implements Operator<T, T> {
    private final Action0 subscribe;

    /**
     * Constructs an instance of the operator with the callback that gets invoked when the modified Observable is subscribed
     * @param unsubscribe The action that gets invoked when the modified {@link rx0200.Observable} is subscribed
     */
    public OperatorDoOnSubscribe(Action0 subscribe) {
        this.subscribe = subscribe;
    }

    @Override
    public Subscriber<? super T> call(final Subscriber<? super T> child) {
        subscribe.call();
        // Pass through since this operator is for notification only, there is
        // no change to the stream whatsoever.
        return child;
    }
}
