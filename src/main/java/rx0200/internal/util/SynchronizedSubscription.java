package rx0200.internal.util;

import rx0200.Subscription;

public class SynchronizedSubscription implements Subscription {

    private final Subscription s;

    public SynchronizedSubscription(Subscription s) {
        this.s = s;
    }

    @Override
    public synchronized void unsubscribe() {
        s.unsubscribe();
    }

    @Override
    public synchronized boolean isUnsubscribed() {
        return s.isUnsubscribed();
    }

}
