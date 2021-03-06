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
package rx0160.operators;

import static org.junit.Assert.*;
import static rx0160.operators.OperationMostRecent.*;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;

import org.junit.Test;
import rx0160.Observable;
import rx0160.observables.BlockingObservable;
import rx0160.schedulers.TestScheduler;

import rx0160.subjects.PublishSubject;
import rx0160.subjects.Subject;

public class OperationMostRecentTest {

    @Test
    public void testMostRecent() {
        Subject<String, String> observable = PublishSubject.create();

        Iterator<String> it = mostRecent(observable, "default").iterator();

        assertTrue(it.hasNext());
        assertEquals("default", it.next());
        assertEquals("default", it.next());

        observable.onNext("one");
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        assertEquals("one", it.next());

        observable.onNext("two");
        assertTrue(it.hasNext());
        assertEquals("two", it.next());
        assertEquals("two", it.next());

        observable.onCompleted();
        assertFalse(it.hasNext());

    }

    @Test(expected = TestException.class)
    public void testMostRecentWithException() {
        Subject<String, String> observable = PublishSubject.create();

        Iterator<String> it = mostRecent(observable, "default").iterator();

        assertTrue(it.hasNext());
        assertEquals("default", it.next());
        assertEquals("default", it.next());

        observable.onError(new TestException());
        assertTrue(it.hasNext());

        it.next();
    }

    private static class TestException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
    
    @Test(timeout = 1000)
    public void testSingleSourceManyIterators() {
        TestScheduler scheduler = new TestScheduler();
        BlockingObservable<Long> source = Observable.interval(1, TimeUnit.SECONDS, scheduler).take(10).toBlockingObservable();
        
        Iterable<Long> iter = source.mostRecent(-1L);
        
        for (int j = 0; j < 3; j++) {
            Iterator<Long> it = iter.iterator();
            
            Assert.assertEquals(Long.valueOf(-1), it.next());
            
            for (int i = 0; i < 9; i++) {
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
                
                Assert.assertEquals(true, it.hasNext());
                Assert.assertEquals(Long.valueOf(i), it.next());
            }
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            
            Assert.assertEquals(false, it.hasNext());
        }
        
    }
}
