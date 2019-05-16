package me.leon;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx012.Observable;
import rx012.Observer;
import rx012.Subscription;
import rx012.util.functions.Action1;
import rx012.util.functions.Func1;

public class T {
    private static final Logger logger = LoggerFactory.getLogger(T.class);
    public static void main(String args[]) {
        hello("Ben", "George");
        logger.info("Current Time: {}", System.currentTimeMillis());
        logger.info("Current Time: " + System.currentTimeMillis());
        logger.info("Current Time: {}", System.currentTimeMillis());
        logger.trace("trace log");
        logger.warn("warn log");
        logger.debug("debug log");
        logger.info("info log");
        logger.error("error log");
    }

    public static void hello(String... names) {
        Observable.just("hello");

        Observable.toObservable(names)
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        return " lal  "+s+" lal ";
                    }
                })

                .subscribe(new Action1<String>() {

            @Override
            public void call(String s) {
                System.out.println("Hello " + s + "!");
            }

        });
    }
}
