package me.leon;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx090.plugins.RxJavaErrorHandler;

import java.util.concurrent.atomic.AtomicInteger;


public class T {
    private static final Logger logger = LoggerFactory.getLogger(T.class);

    public static void main(String args[]) {
        hello("Ben", "George");
//         final AtomicInteger counter = new AtomicInteger(0);
//        System.out.println(counter.getAndIncrement());
//        System.out.println(counter.get());
//        performTest();
//        logger.info("Current Time: {}", System.currentTimeMillis());
//        logger.info("Current Time: " + System.currentTimeMillis());
//        logger.info("Current Time: {}", System.currentTimeMillis());
//        logger.trace("trace log");
//        logger.warn("warn log");
//        logger.debug("debug log");
//        logger.info("info log");
//        logger.error("error log");
    }

    public static void hello(String... names) {

//        rx050.Observable.toObservable(names)
//                .map(s -> " rx0.5.0  " + s + "   ")
//                .subscribe(s -> System.out.println("Hello " + s + "!"));
//
//        rx054.Observable.toObservable(names)
//                .map(s -> " rx0.5.4  " + s + "   ")
//                .subscribe(s -> System.out.println("Hello " + s + "!"));
//
//        rx061.Observable.toObservable(names)
//                .map(s -> " rx0.6.1  " + s + "   ")
//                .subscribe(s -> System.out.println("Hello " + s + "!"));
//
//        rx070.Observable.toObservable(names)
//                .map(s -> " rx0.7.0  " + s + "   ")
//                .subscribe(s -> System.out.println("Hello " + s + "!"));
//        // 变更
//        rx090.plugins.RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler(){
//            @Override
//            public void handleError(Exception e) {
//                System.out.println(  "handle err" +e.getMessage());
//                super.handleError(e);
//            }
//        });
//        rx090.Observable.from(names)
//                .map(s -> " rx0.9.0  " + s + "   " +Integer.parseInt(s))
//                .subscribe(s -> System.out.println("Hello " + s + "!"));
//
//
//        rx0140.Observable.from(names)
//                .map(s -> " rx0.14.0  " + s + "   ")
//                .subscribe(s -> System.out.println("Hello " + s + "!"));
//
//        rx0160.Observable.from(names)
//                .map(s -> " rx0.16.0  " + s + "   ")
//                .subscribe(s -> System.out.println("Hello " + s + "!"));
//
//        rx0200.Observable.from(names)
//                .map(s -> " rx0.20.0  " + s + "   ")
//                .subscribe(s -> System.out.println("Hello " + s + "!"));

        rx100.Observable.from(names)
                .take(2)
                .map(s -> " rx1.0.0  " + s + "   ")
                .subscribe(s -> System.out.println("Hello " + s + "!"));

    }

    public static long start = 0;

    public static void performTest() {
        start = System.currentTimeMillis();
        rx050.Observable.just(1).map(i -> {
            return String.valueOf(i);
        }).map(i -> {
            return Integer.parseInt(i);
        }).subscribe(s -> {
            System.out.println(" rx050 takes " + (System.currentTimeMillis() - start));
        });

        start = System.currentTimeMillis();
        rx050.Observable.just(1).map(i -> {
            return String.valueOf(i);
        }).map(i -> {
            return Integer.parseInt(i);
        }).subscribe(s -> {
            System.out.println(" rx050 takes " + (System.currentTimeMillis() - start));
        });
        start = System.currentTimeMillis();

        rx054.Observable<Integer> o = rx054.Observable.just(1);
        o.map(i -> {
            return String.valueOf(i);
        }).map(i -> {
            return Integer.parseInt(i);
        }).subscribe(s -> {
            System.out.println(" rx054 takes " + (System.currentTimeMillis() - start));
        });


        start = System.currentTimeMillis();

        rx061.Observable.just(1).map(i -> {
            return String.valueOf(i);
        }).map(i -> {
            return Integer.parseInt(i);
        }).subscribe(s -> {
            System.out.println(" rx061 takes " + (System.currentTimeMillis() - start));
        });

        start = System.currentTimeMillis();

        rx070.Observable.just(1).map(i -> {
            return String.valueOf(i);
        }).map(i -> {
            return Integer.parseInt(i);
        }).subscribe(s -> {
            System.out.println(" rx070 takes " + (System.currentTimeMillis() - start));
        });

        start = System.currentTimeMillis();

        rx090.Observable.just(1).map(i -> {
            return String.valueOf(i);
        }).map(i -> {
            return Integer.parseInt(i);
        }).subscribe(s -> {
            System.out.println(" rx090 takes " + (System.currentTimeMillis() - start));
        });

        start = System.currentTimeMillis();

        rx0140.Observable.just(1).map(i -> {
            return String.valueOf(i);
        }).map(i -> {
            return Integer.parseInt(i);
        }).subscribe(s -> {
            System.out.println("rx0140 takes " + (System.currentTimeMillis() - start));
        });

        start = System.currentTimeMillis();

        rx0160.Observable.just(1).map(i -> {
            return String.valueOf(i);
        }).map(i -> {
            return Integer.parseInt(i);
        }).subscribe(s -> {
            System.out.println(" rx0160 takes " + (System.currentTimeMillis() - start));
        });

        start = System.currentTimeMillis();

        rx0200.Observable.just(1).map(i -> {
            return String.valueOf(i);
        }).map(i -> {
            return Integer.parseInt(i);
        }).subscribe(s -> {
            System.out.println(" rx0200 takes " + (System.currentTimeMillis() - start));
        });
        start = System.currentTimeMillis();

        rx100.Observable.just(1).map(i -> {
            return String.valueOf(i);
        }).map(i -> {
            return Integer.parseInt(i);
        }).subscribe(s -> {
            System.out.println("rx100 takes " + (System.currentTimeMillis() - start));
        });

    }

}
