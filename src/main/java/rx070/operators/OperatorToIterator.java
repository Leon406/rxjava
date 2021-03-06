package rx070.operators;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

import rx070.Notification;
import rx070.Observable;
import rx070.Observer;
import rx070.Subscription;
import rx070.subscriptions.Subscriptions;
import rx070.util.Exceptions;
import rx070.util.functions.Func1;

/**
 * @see https://github.com/Netflix/RxJava/issues/50
 */
public class OperatorToIterator {

    /**
     * Returns an iterator that iterates all values of the observable.
     * 
     * @param that
     *            an observable sequence to get an iterator for.
     * @param <T>
     *            the type of source.
     * @return the iterator that could be used to iterate over the elements of the observable.
     */
    public static <T> Iterator<T> toIterator(Observable<T> that) {
        final BlockingQueue<Notification<T>> notifications = new LinkedBlockingQueue<Notification<T>>();

        Observable.materialize(that).subscribe(new Observer<Notification<T>>() {
            @Override
            public void onCompleted() {
                // ignore
            }

            @Override
            public void onError(Exception e) {
                // ignore
            }

            @Override
            public void onNext(Notification<T> args) {
                notifications.offer(args);
            }
        });

        return new Iterator<T>() {
            private Notification<T> buf;

            @Override
            public boolean hasNext() {
                if (buf == null) {
                    buf = take();
                }
                return !buf.isOnCompleted();
            }

            @Override
            public T next() {
                if (buf == null) {
                    buf = take();
                }
                if (buf.isOnError()) {
                    throw Exceptions.propagate(buf.getException());
                }

                T result = buf.getValue();
                buf = null;
                return result;
            }

            private Notification<T> take() {
                try {
                    return notifications.take();
                } catch (InterruptedException e) {
                    throw Exceptions.propagate(e);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Read-only iterator");
            }
        };
    }

    @Test
    public void testToIterator() {
        Observable<String> obs = Observable.toObservable("one", "two", "three");

        Iterator<String> it = toIterator(obs);

        assertEquals(true, it.hasNext());
        assertEquals("one", it.next());

        assertEquals(true, it.hasNext());
        assertEquals("two", it.next());

        assertEquals(true, it.hasNext());
        assertEquals("three", it.next());

        assertEquals(false, it.hasNext());

    }

    @Test(expected = TestException.class)
    public void testToIteratorWithException() {
        Observable<String> obs = Observable.create(new Func1<Observer<String>, Subscription>() {

            @Override
            public Subscription call(Observer<String> observer) {
                observer.onNext("one");
                observer.onError(new TestException());
                return Subscriptions.empty();
            }
        });

        Iterator<String> it = toIterator(obs);

        assertEquals(true, it.hasNext());
        assertEquals("one", it.next());

        assertEquals(true, it.hasNext());
        it.next();
    }

    private static class TestException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
