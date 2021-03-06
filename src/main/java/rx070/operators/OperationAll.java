package rx070.operators;

import org.junit.Test;
import rx070.Observable;
import rx070.Observer;
import rx070.Subscription;
import rx070.util.AtomicObservableSubscription;
import rx070.util.functions.Func1;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class OperationAll {

    public static <T> Func1<Observer<Boolean>, Subscription> all(Observable<T> sequence, Func1<T, Boolean> predicate) {
        return new AllObservable<T>(sequence, predicate);
    }

    private static class AllObservable<T> implements Func1<Observer<Boolean>, Subscription> {
        private final Observable<T> sequence;
        private final Func1<T, Boolean> predicate;

        private final AtomicBoolean status = new AtomicBoolean(true);
        private final AtomicObservableSubscription subscription = new AtomicObservableSubscription();


        private AllObservable(Observable<T> sequence, Func1<T, Boolean> predicate) {
            this.sequence = sequence;
            this.predicate = predicate;
        }


        @Override
        public Subscription call(final Observer<Boolean> observer) {
            return subscription.wrap(sequence.subscribe(new Observer<T>() {
                @Override
                public void onCompleted() {
                    if (status.get()) {
                        observer.onNext(true);
                        observer.onCompleted();
                    }
                }

                @Override
                public void onError(Exception e) {
                    observer.onError(e);
                }

                @Override
                public void onNext(T args) {
                    boolean result = predicate.call(args);
                    boolean changed = status.compareAndSet(true, result);

                    if (changed && !result) {
                        observer.onNext(false);
                        observer.onCompleted();
                        subscription.unsubscribe();
                    }
                }
            }));
        }
    }

    public static class UnitTest {

        @Test
        @SuppressWarnings("unchecked")
        public void testAll() {
            Observable<String> obs = Observable.from("one", "two", "six");

            Observer<Boolean> observer = mock(Observer.class);
            Observable.create(all(obs, new Func1<String, Boolean>() {
                @Override
                public Boolean call(String s) {
                    return s.length() == 3;
                }
            })).subscribe(observer);

            verify(observer).onNext(true);
            verify(observer).onCompleted();
            verifyNoMoreInteractions(observer);
        }

        @Test
        @SuppressWarnings("unchecked")
        public void testNotAll() {
            Observable<String> obs = Observable.from("one", "two", "three", "six");

            Observer<Boolean> observer = mock(Observer.class);
            Observable.create(all(obs, new Func1<String, Boolean>() {
                @Override
                public Boolean call(String s) {
                    return s.length() == 3;
                }
            })).subscribe(observer);

            verify(observer).onNext(false);
            verify(observer).onCompleted();
            verifyNoMoreInteractions(observer);
        }

        @Test
        @SuppressWarnings("unchecked")
        public void testEmpty() {
            Observable<String> obs = Observable.empty();

            Observer<Boolean> observer = mock(Observer.class);
            Observable.create(all(obs, new Func1<String, Boolean>() {
                @Override
                public Boolean call(String s) {
                    return s.length() == 3;
                }
            })).subscribe(observer);

            verify(observer).onNext(true);
            verify(observer).onCompleted();
            verifyNoMoreInteractions(observer);
        }

        @Test
        @SuppressWarnings("unchecked")
        public void testError() {
            Exception error = new Exception();
            Observable<String> obs = Observable.error(error);

            Observer<Boolean> observer = mock(Observer.class);
            Observable.create(all(obs, new Func1<String, Boolean>() {
                @Override
                public Boolean call(String s) {
                    return s.length() == 3;
                }
            })).subscribe(observer);

            verify(observer).onError(error);
            verifyNoMoreInteractions(observer);
        }
    }
}
