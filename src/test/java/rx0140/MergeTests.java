package rx0140;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import rx0140.CovarianceTest.HorrorMovie;
import rx0140.CovarianceTest.Media;
import rx0140.CovarianceTest.Movie;
import rx0140.Observable.OnSubscribeFunc;
import rx0140.subscriptions.Subscriptions;

public class MergeTests {

    /**
     * This won't compile if super/extends isn't done correctly on generics
     */
    @Test
    public void testCovarianceOfMerge() {
        Observable<HorrorMovie> horrors = Observable.from(new HorrorMovie());
        Observable<Observable<HorrorMovie>> metaHorrors = Observable.just(horrors);
        Observable.<Media> merge(metaHorrors);
    }

    @Test
    public void testMergeCovariance() {
        Observable<Media> o1 = Observable.<Media> from(new HorrorMovie(), new Movie());
        Observable<Media> o2 = Observable.from(new Media(), new HorrorMovie());

        Observable<Observable<Media>> os = Observable.from(o1, o2);

        List<Media> values = Observable.merge(os).toList().toBlockingObservable().single();
    }

    @Test
    public void testMergeCovariance2() {
        Observable<Media> o1 = Observable.from(new HorrorMovie(), new Movie(), new Media());
        Observable<Media> o2 = Observable.from(new Media(), new HorrorMovie());

        Observable<Observable<Media>> os = Observable.from(o1, o2);

        List<Media> values = Observable.merge(os).toList().toBlockingObservable().single();
    }
    
    @Test
    public void testMergeCovariance3() {
        Observable<Movie> o1 = Observable.from(new HorrorMovie(), new Movie());
        Observable<Media> o2 = Observable.from(new Media(), new HorrorMovie());

        List<Media> values = Observable.merge(o1, o2).toList().toBlockingObservable().single();
        
        assertTrue(values.get(0) instanceof HorrorMovie);
        assertTrue(values.get(1) instanceof Movie);
        assertTrue(values.get(2) instanceof Media);
        assertTrue(values.get(3) instanceof HorrorMovie);
    }

    @Test
    public void testMergeCovariance4() {

        Observable<Movie> o1 = Observable.create(new OnSubscribeFunc<Movie>() {

            @Override
            public Subscription onSubscribe(Observer<? super Movie> o) {
                o.onNext(new HorrorMovie());
                o.onNext(new Movie());
                //                o.onNext(new Media()); // correctly doesn't compile
                o.onCompleted();
                return Subscriptions.empty();
            }
        });

        Observable<Media> o2 = Observable.from(new Media(), new HorrorMovie());

        List<Media> values = Observable.merge(o1, o2).toList().toBlockingObservable().single();
        
        assertTrue(values.get(0) instanceof HorrorMovie);
        assertTrue(values.get(1) instanceof Movie);
        assertTrue(values.get(2) instanceof Media);
        assertTrue(values.get(3) instanceof HorrorMovie);
    }
    
    
}
