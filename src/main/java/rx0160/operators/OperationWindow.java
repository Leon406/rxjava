/**
 * Copyright 2013 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx0160.operators;

import java.util.concurrent.TimeUnit;

import rx0160.Observable;
import rx0160.Observable.OnSubscribeFunc;
import rx0160.Observer;
import rx0160.Scheduler;
import rx0160.Subscription;
import rx0160.schedulers.Schedulers;
import rx0160.util.functions.Func0;
import rx0160.util.functions.Func1;

public final class OperationWindow extends ChunkedOperation {

    public static <T> Func0<Window<T>> windowMaker() {
        return new Func0<Window<T>>() {
            @Override
            public Window<T> call() {
                return new Window<T>();
            }
        };
    }

    /**
     * <p>This method creates a {@link Func1} object which represents the window operation. This operation takes
     * values from the specified {@link rx0160.Observable} source and stores them in a window until the {@link rx0160.Observable} constructed using the {@link Func0} argument, produces a
     * value. The window is then
     * emitted, and a new window is created to replace it. A new {@link rx0160.Observable} will be constructed using the
     * provided {@link Func0} object, which will determine when this new window is emitted. When the source {@link rx0160.Observable} completes or produces an error, the current window
     * is emitted, and the event is propagated
     * to all subscribed {@link rx0160.Observer}s.</p>
     * 
     * <p>Note that this operation only produces <strong>non-overlapping windows</strong>. At all times there is
     * exactly one window actively storing values.</p>
     * 
     * @param source
     *            The {@link rx0160.Observable} which produces values.
     * @param windowClosingSelector
     *            A {@link Func0} object which produces {@link rx0160.Observable}s. These {@link rx0160.Observable}s determine when a window is emitted and replaced by simply
     *            producing an object.
     * @return
     *         the {@link Func1} object representing the specified window operation.
     */
    public static <T, TClosing> OnSubscribeFunc<Observable<T>> window(final Observable<? extends T> source, final Func0<? extends Observable<? extends TClosing>> windowClosingSelector) {
        return new OnSubscribeFunc<Observable<T>>() {
            @Override
            public Subscription onSubscribe(final Observer<? super Observable<T>> observer) {
                NonOverlappingChunks<T, Observable<T>> windows = new NonOverlappingChunks<T, Observable<T>>(observer, OperationWindow.<T> windowMaker());
                ChunkCreator creator = new ObservableBasedSingleChunkCreator<T, Observable<T>, TClosing>(windows, windowClosingSelector);
                return source.subscribe(new ChunkObserver<T, Observable<T>>(windows, observer, creator));
            }

        };
    }

    /**
     * <p>This method creates a {@link Func1} object which represents the window operation. This operation takes
     * values from the specified {@link rx0160.Observable} source and stores them in the currently active window. Initially
     * there are no windows active.</p>
     * 
     * <p>Windows can be created by pushing a {@link rx0160.util.Opening} value to the "windowOpenings" {@link rx0160.Observable}.
     * This creates a new window which will then start recording values which are produced by the "source" {@link rx0160.Observable}. Additionally the "windowClosingSelector" will be used to construct an
     * {@link rx0160.Observable} which can produce values. When it does so it will close this (and only this) newly created
     * window. When the source {@link rx0160.Observable} completes or produces an error, all windows are emitted, and the
     * event is propagated to all subscribed {@link rx0160.Observer}s.</p>
     * 
     * <p>Note that when using this operation <strong>multiple overlapping windows</strong>
     * could be active at any one point.</p>
     * 
     * @param source
     *            The {@link rx0160.Observable} which produces values.
     * @param windowOpenings
     *            An {@link rx0160.Observable} which when it produces a {@link rx0160.util.Opening} value will
     *            create a new window which instantly starts recording the "source" {@link rx0160.Observable}.
     * @param windowClosingSelector
     *            A {@link Func0} object which produces {@link rx0160.Observable}s. These {@link rx0160.Observable}s determine when a window is emitted and replaced by simply
     *            producing an object.
     * @return
     *         the {@link Func1} object representing the specified window operation.
     */
    public static <T, TOpening, TClosing> OnSubscribeFunc<Observable<T>> window(final Observable<? extends T> source, final Observable<? extends TOpening> windowOpenings, final Func1<? super TOpening, ? extends Observable<? extends TClosing>> windowClosingSelector) {
        return new OnSubscribeFunc<Observable<T>>() {
            @Override
            public Subscription onSubscribe(final Observer<? super Observable<T>> observer) {
                OverlappingChunks<T, Observable<T>> windows = new OverlappingChunks<T, Observable<T>>(observer, OperationWindow.<T> windowMaker());
                ChunkCreator creator = new ObservableBasedMultiChunkCreator<T, Observable<T>, TOpening, TClosing>(windows, windowOpenings, windowClosingSelector);
                return source.subscribe(new ChunkObserver<T, Observable<T>>(windows, observer, creator));
            }
        };
    }

    /**
     * <p>This method creates a {@link Func1} object which represents the window operation. This operation takes
     * values from the specified {@link rx0160.Observable} source and stores them in a window until the window contains
     * a specified number of elements. The window is then emitted, and a new window is created to replace it.
     * When the source {@link rx0160.Observable} completes or produces an error, the current window is emitted, and
     * the event is propagated to all subscribed {@link rx0160.Observer}s.</p>
     * 
     * <p>Note that this operation only produces <strong>non-overlapping windows</strong>. At all times there is
     * exactly one window actively storing values.</p>
     * 
     * @param source
     *            The {@link rx0160.Observable} which produces values.
     * @param count
     *            The number of elements a window should have before being emitted and replaced.
     * @return
     *         the {@link Func1} object representing the specified window operation.
     */
    public static <T> OnSubscribeFunc<Observable<T>> window(Observable<? extends T> source, int count) {
        return window(source, count, count);
    }

    /**
     * <p>This method creates a {@link Func1} object which represents the window operation. This operation takes
     * values from the specified {@link rx0160.Observable} source and stores them in all active windows until the window
     * contains a specified number of elements. The window is then emitted. windows are created after a certain
     * amount of values have been received. When the source {@link rx0160.Observable} completes or produces an error, the
     * currently active windows are emitted, and the event is propagated to all subscribed {@link rx0160.Observer}s.</p>
     * 
     * <p>Note that this operation can produce <strong>non-connected, connected non-overlapping, or overlapping
     * windows</strong> depending on the input parameters.</p>
     * 
     * @param source
     *            The {@link rx0160.Observable} which produces values.
     * @param count
     *            The number of elements a window should have before being emitted.
     * @param skip
     *            The interval with which windows have to be created. Note that when "skip" == "count"
     *            that this is the same as calling {@link OperationWindow#window(rx0160.Observable, int)}.
     *            If "skip" < "count", this window operation will produce overlapping windows and if "skip"
     *            > "count" non-overlapping windows will be created and some values will not be pushed
     *            into a window at all!
     * @return
     *         the {@link Func1} object representing the specified window operation.
     */
    public static <T> OnSubscribeFunc<Observable<T>> window(final Observable<? extends T> source, final int count, final int skip) {
        return new OnSubscribeFunc<Observable<T>>() {
            @Override
            public Subscription onSubscribe(final Observer<? super Observable<T>> observer) {
                Chunks<T, Observable<T>> chunks = new SizeBasedChunks<T, Observable<T>>(observer, OperationWindow.<T> windowMaker(), count);
                ChunkCreator creator = new SkippingChunkCreator<T, Observable<T>>(chunks, skip);
                return source.subscribe(new ChunkObserver<T, Observable<T>>(chunks, observer, creator));
            }
        };
    }

    /**
     * <p>This method creates a {@link Func1} object which represents the window operation. This operation takes
     * values from the specified {@link rx0160.Observable} source and stores them in a window. Periodically the window
     * is emitted and replaced with a new window. How often this is done depends on the specified timespan.
     * When the source {@link rx0160.Observable} completes or produces an error, the current window is emitted, and
     * the event is propagated to all subscribed {@link rx0160.Observer}s.</p>
     * 
     * <p>Note that this operation only produces <strong>non-overlapping windows</strong>. At all times there is
     * exactly one window actively storing values.</p>
     * 
     * @param source
     *            The {@link rx0160.Observable} which produces values.
     * @param timespan
     *            The amount of time all windows must be actively collect values before being emitted.
     * @param unit
     *            The {@link TimeUnit} defining the unit of time for the timespan.
     * @return
     *         the {@link Func1} object representing the specified window operation.
     */
    public static <T> OnSubscribeFunc<Observable<T>> window(Observable<? extends T> source, long timespan, TimeUnit unit) {
        return window(source, timespan, unit, Schedulers.threadPoolForComputation());
    }

    /**
     * <p>This method creates a {@link Func1} object which represents the window operation. This operation takes
     * values from the specified {@link rx0160.Observable} source and stores them in a window. Periodically the window
     * is emitted and replaced with a new window. How often this is done depends on the specified timespan.
     * When the source {@link rx0160.Observable} completes or produces an error, the current window is emitted, and
     * the event is propagated to all subscribed {@link rx0160.Observer}s.</p>
     * 
     * <p>Note that this operation only produces <strong>non-overlapping windows</strong>. At all times there is
     * exactly one window actively storing values.</p>
     * 
     * @param source
     *            The {@link rx0160.Observable} which produces values.
     * @param timespan
     *            The amount of time all windows must be actively collect values before being emitted.
     * @param unit
     *            The {@link TimeUnit} defining the unit of time for the timespan.
     * @param scheduler
     *            The {@link rx0160.Scheduler} to use for timing windows.
     * @return
     *         the {@link Func1} object representing the specified window operation.
     */
    public static <T> OnSubscribeFunc<Observable<T>> window(final Observable<? extends T> source, final long timespan, final TimeUnit unit, final Scheduler scheduler) {
        return new OnSubscribeFunc<Observable<T>>() {
            @Override
            public Subscription onSubscribe(final Observer<? super Observable<T>> observer) {
                NonOverlappingChunks<T, Observable<T>> windows = new NonOverlappingChunks<T, Observable<T>>(observer, OperationWindow.<T> windowMaker());
                ChunkCreator creator = new TimeBasedChunkCreator<T, Observable<T>>(windows, timespan, unit, scheduler);
                return source.subscribe(new ChunkObserver<T, Observable<T>>(windows, observer, creator));
            }
        };
    }

    /**
     * <p>This method creates a {@link Func1} object which represents the window operation. This operation takes
     * values from the specified {@link rx0160.Observable} source and stores them in a window. Periodically the window
     * is emitted and replaced with a new window. How often this is done depends on the specified timespan.
     * Additionally the window is automatically emitted once it reaches a specified number of elements.
     * When the source {@link rx0160.Observable} completes or produces an error, the current window is emitted, and
     * the event is propagated to all subscribed {@link rx0160.Observer}s.</p>
     * 
     * <p>Note that this operation only produces <strong>non-overlapping windows</strong>. At all times there is
     * exactly one window actively storing values.</p>
     * 
     * @param source
     *            The {@link rx0160.Observable} which produces values.
     * @param timespan
     *            The amount of time all windows must be actively collect values before being emitted.
     * @param unit
     *            The {@link TimeUnit} defining the unit of time for the timespan.
     * @param count
     *            The maximum size of the window. Once a window reaches this size, it is emitted.
     * @return
     *         the {@link Func1} object representing the specified window operation.
     */
    public static <T> OnSubscribeFunc<Observable<T>> window(Observable<? extends T> source, long timespan, TimeUnit unit, int count) {
        return window(source, timespan, unit, count, Schedulers.threadPoolForComputation());
    }

    /**
     * <p>This method creates a {@link Func1} object which represents the window operation. This operation takes
     * values from the specified {@link rx0160.Observable} source and stores them in a window. Periodically the window
     * is emitted and replaced with a new window. How often this is done depends on the specified timespan.
     * Additionally the window is automatically emitted once it reaches a specified number of elements.
     * When the source {@link rx0160.Observable} completes or produces an error, the current window is emitted, and
     * the event is propagated to all subscribed {@link rx0160.Observer}s.</p>
     * 
     * <p>Note that this operation only produces <strong>non-overlapping windows</strong>. At all times there is
     * exactly one window actively storing values.</p>
     * 
     * @param source
     *            The {@link rx0160.Observable} which produces values.
     * @param timespan
     *            The amount of time all windows must be actively collect values before being emitted.
     * @param unit
     *            The {@link TimeUnit} defining the unit of time for the timespan.
     * @param count
     *            The maximum size of the window. Once a window reaches this size, it is emitted.
     * @param scheduler
     *            The {@link rx0160.Scheduler} to use for timing windows.
     * @return
     *         the {@link Func1} object representing the specified window operation.
     */
    public static <T> OnSubscribeFunc<Observable<T>> window(final Observable<? extends T> source, final long timespan, final TimeUnit unit, final int count, final Scheduler scheduler) {
        return new OnSubscribeFunc<Observable<T>>() {
            @Override
            public Subscription onSubscribe(final Observer<? super Observable<T>> observer) {
                Chunks<T, Observable<T>> chunks = new TimeAndSizeBasedChunks<T, Observable<T>>(observer, OperationWindow.<T> windowMaker(), count, timespan, unit, scheduler);
                ChunkCreator creator = new SingleChunkCreator<T, Observable<T>>(chunks);
                return source.subscribe(new ChunkObserver<T, Observable<T>>(chunks, observer, creator));
            }
        };
    }

    /**
     * <p>This method creates a {@link Func1} object which represents the window operation. This operation takes
     * values from the specified {@link rx0160.Observable} source and stores them in a window. Periodically the window
     * is emitted and replaced with a new window. How often this is done depends on the specified timespan.
     * The creation of windows is also periodical. How often this is done depends on the specified timeshift.
     * When the source {@link rx0160.Observable} completes or produces an error, the current window is emitted, and
     * the event is propagated to all subscribed {@link rx0160.Observer}s.</p>
     * 
     * <p>Note that this operation can produce <strong>non-connected, or overlapping windows</strong> depending
     * on the input parameters.</p>
     * 
     * @param source
     *            The {@link rx0160.Observable} which produces values.
     * @param timespan
     *            The amount of time all windows must be actively collect values before being emitted.
     * @param timeshift
     *            The amount of time between creating windows.
     * @param unit
     *            The {@link TimeUnit} defining the unit of time for the timespan.
     * @return
     *         the {@link Func1} object representing the specified window operation.
     */
    public static <T> OnSubscribeFunc<Observable<T>> window(Observable<? extends T> source, long timespan, long timeshift, TimeUnit unit) {
        return window(source, timespan, timeshift, unit, Schedulers.threadPoolForComputation());
    }

    /**
     * <p>This method creates a {@link Func1} object which represents the window operation. This operation takes
     * values from the specified {@link rx0160.Observable} source and stores them in a window. Periodically the window
     * is emitted and replaced with a new window. How often this is done depends on the specified timespan.
     * The creation of windows is also periodical. How often this is done depends on the specified timeshift.
     * When the source {@link rx0160.Observable} completes or produces an error, the current window is emitted, and
     * the event is propagated to all subscribed {@link rx0160.Observer}s.</p>
     * 
     * <p>Note that this operation can produce <strong>non-connected, or overlapping windows</strong> depending
     * on the input parameters.</p>
     * 
     * @param source
     *            The {@link rx0160.Observable} which produces values.
     * @param timespan
     *            The amount of time all windows must be actively collect values before being emitted.
     * @param timeshift
     *            The amount of time between creating windows.
     * @param unit
     *            The {@link TimeUnit} defining the unit of time for the timespan.
     * @param scheduler
     *            The {@link rx0160.Scheduler} to use for timing windows.
     * @return
     *         the {@link Func1} object representing the specified window operation.
     */
    public static <T> OnSubscribeFunc<Observable<T>> window(final Observable<? extends T> source, final long timespan, final long timeshift, final TimeUnit unit, final Scheduler scheduler) {
        return new OnSubscribeFunc<Observable<T>>() {
            @Override
            public Subscription onSubscribe(final Observer<? super Observable<T>> observer) {
                OverlappingChunks<T, Observable<T>> windows = new TimeBasedChunks<T, Observable<T>>(observer, OperationWindow.<T> windowMaker(), timespan, unit, scheduler);
                ChunkCreator creator = new TimeBasedChunkCreator<T, Observable<T>>(windows, timeshift, unit, scheduler);
                return source.subscribe(new ChunkObserver<T, Observable<T>>(windows, observer, creator));
            }
        };
    }

    /**
     * This class represents a single window: A sequence of recorded values.
     * 
     * @param <T>
     *            The type of objects which this {@link Window} can hold.
     */
    protected static class Window<T> extends Chunk<T, Observable<T>> {
        /**
         * @return
         *         The mutable underlying {@link Observable} which contains all the
         *         recorded values in this {@link Window} object.
         */
        @Override
        public Observable<T> getContents() {
            return Observable.from(contents);
        }
    }
}
