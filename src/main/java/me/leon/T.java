package me.leon;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx012.Observable;

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

        rx012.Observable.toObservable(names)
                .map(s -> " rx0.1.2  "+s+"  ")
                .subscribe(s -> System.out.println("Hello " + s + "!"));

        rx050.Observable.toObservable(names)
                .map(s -> " rx0.5.0  "+s+"   ")
                .subscribe(s -> System.out.println("Hello " + s + "!"));

        rx054.Observable.toObservable(names)
                .map(s -> " rx0.5.4  "+s+"   ")
                .subscribe(s -> System.out.println("Hello " + s + "!"));

        rx061.Observable.toObservable(names)
                .map(s -> " rx0.6.1  "+s+"   ")
                .subscribe(s -> System.out.println("Hello " + s + "!"));

        rx070.Observable.toObservable(names)
                .map(s -> " rx0.7.0  "+s+"   ")
                .subscribe(s -> System.out.println("Hello " + s + "!"));
        // 变更
        rx090.Observable.from(names)
                .map(s -> " rx0.9.0  "+s+"   ")
                .subscribe(s -> System.out.println("Hello " + s + "!"));


        rx0140.Observable.from(names)
                .map(s -> " rx0.14.0  "+s+"   ")
                .subscribe(s -> System.out.println("Hello " + s + "!"));

        rx0160.Observable.from(names)
                .map(s -> " rx0.16.0  "+s+"   ")
                .subscribe(s -> System.out.println("Hello " + s + "!"));

        rx0200.Observable.from(names)
                .map(s -> " rx0.20.0  "+s+"   ")
                .subscribe(s -> System.out.println("Hello " + s + "!"));

        rx100.Observable.from(names)
                .map(s -> " rx1.0.0  "+s+"   ")
                .subscribe(s -> System.out.println("Hello " + s + "!"));
    }
}
