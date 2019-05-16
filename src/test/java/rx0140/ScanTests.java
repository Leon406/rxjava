package rx0140;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import rx0140.EventStream.Event;
import rx0140.util.functions.Action1;
import rx0140.util.functions.Func2;

public class ScanTests {

    @Test
    public void testUnsubscribeScan() {

        EventStream.getEventStream("HTTP-ClusterB", 20)
                .scan(new HashMap<String, String>(), new Func2<Map<String, String>, Event, Map<String, String>>() {

                    @Override
                    public Map<String, String> call(Map<String, String> accum, Event perInstanceEvent) {
                        accum.put("instance", perInstanceEvent.instanceId);
                        return accum;
                    }

                })
                .take(10)
                .toBlockingObservable().forEach(new Action1<Map<String, String>>() {

                    @Override
                    public void call(Map<String, String> v) {
                        System.out.println(v);
                    }

                });
    }
}
