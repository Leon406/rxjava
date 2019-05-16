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
package rx0160.concurrency;

import java.util.concurrent.TimeUnit;

import rx0160.Scheduler;
import rx0160.Subscription;
import rx0160.util.functions.Func2;

/**
 * Deprecated. Package changed from rx.concurrency to rx.schedulers.
 * 
 * @deprecated Use {@link rx0160.schedulers.NewThreadScheduler} instead. This will be removed before 1.0 release.
 */
@Deprecated
public class NewThreadScheduler extends Scheduler {

    private final static NewThreadScheduler INSTANCE = new NewThreadScheduler();

    public static NewThreadScheduler getInstance() {
        return INSTANCE;
    }

    private final rx0160.schedulers.NewThreadScheduler actual;

    private NewThreadScheduler() {
        actual = rx0160.schedulers.NewThreadScheduler.getInstance();
    }

    @Override
    public <T> Subscription schedule(T state, Func2<? super Scheduler, ? super T, ? extends Subscription> action) {
        return actual.schedule(state, action);
    }

    @Override
    public <T> Subscription schedule(T state, Func2<? super Scheduler, ? super T, ? extends Subscription> action, long delayTime, TimeUnit unit) {
        return actual.schedule(state, action, delayTime, unit);
    }

}
