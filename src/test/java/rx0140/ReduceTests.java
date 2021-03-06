package rx0140;

import static org.junit.Assert.*;

import org.junit.Test;

import rx0140.CovarianceTest.HorrorMovie;
import rx0140.CovarianceTest.Movie;
import rx0140.operators.OperationScan;
import rx0140.util.functions.Func2;

public class ReduceTests {

    @Test
    public void reduceInts() {
        Observable<Integer> o = Observable.from(1, 2, 3);
        int value = o.reduce(new Func2<Integer, Integer, Integer>() {

            @Override
            public Integer call(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).toBlockingObservable().single();

        assertEquals(6, value);
    }

    @SuppressWarnings("unused")
    @Test
    public void reduceWithObjects() {
        Observable<Movie> horrorMovies = Observable.<Movie> from(new HorrorMovie());

        Func2<Movie, Movie, Movie> chooseSecondMovie =
                new Func2<Movie, Movie, Movie>() {
                    public Movie call(Movie t1, Movie t2) {
                        return t2;
                    }
                };

        Observable<Movie> reduceResult = Observable.create(OperationScan.scan(horrorMovies, chooseSecondMovie)).takeLast(1);

        Observable<Movie> reduceResult2 = horrorMovies.reduce(chooseSecondMovie);
    }

    /**
     * Reduce consumes and produces T so can't do covariance.
     * 
     * https://github.com/Netflix/RxJava/issues/360#issuecomment-24203016
     */
    @SuppressWarnings("unused")
    @Test
    public void reduceWithCovariantObjects() {
        Observable<Movie> horrorMovies = Observable.<Movie> from(new HorrorMovie());

        Func2<Movie, Movie, Movie> chooseSecondMovie =
                new Func2<Movie, Movie, Movie>() {
                    public Movie call(Movie t1, Movie t2) {
                        return t2;
                    }
                };

        Observable<Movie> reduceResult2 = horrorMovies.reduce(chooseSecondMovie);
    }

    /**
     * Reduce consumes and produces T so can't do covariance.
     * 
     * https://github.com/Netflix/RxJava/issues/360#issuecomment-24203016
     */
    @Test
    public void reduceCovariance() {
        // must type it to <Movie>
        Observable<Movie> horrorMovies = Observable.<Movie> from(new HorrorMovie());
        libraryFunctionActingOnMovieObservables(horrorMovies);
    }

    /*
     * This accepts <Movie> instead of <? super Movie> since `reduce` can't handle covariants
     */
    public void libraryFunctionActingOnMovieObservables(Observable<Movie> obs) {
        Func2<Movie, Movie, Movie> chooseSecondMovie =
                new Func2<Movie, Movie, Movie>() {
                    public Movie call(Movie t1, Movie t2) {
                        return t2;
                    }
                };

        obs.reduce(chooseSecondMovie);
    }

}
