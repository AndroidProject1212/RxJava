/**
 * Copyright 2015 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package io.reactivex.plugins;

import java.util.function.*;

import org.reactivestreams.*;

import io.reactivex.Scheduler;

/**
 * Utility class to inject handlers to certain standard RxJava operations.
 */
public final class RxJavaPlugins {
    
    static volatile Consumer<Throwable> errorHandler;
    
    static volatile Function<Subscriber<Object>, Subscriber<Object>> onSubscribeHandler;
    
    static volatile Function<Publisher<Object>, Publisher<Object>> onCreateHandler;

    static volatile Function<Runnable, Runnable> onScheduleHandler;

    static volatile Function<Scheduler, Scheduler> onInitComputationHandler;
    
    static volatile Function<Scheduler, Scheduler> onInitSingleHandler;
    
    static volatile Function<Scheduler, Scheduler> onInitIOHandler;
    
    static volatile Function<Scheduler, Scheduler> onComputationHandler;
    
    static volatile Function<Scheduler, Scheduler> onSingleHandler;
    
    static volatile Function<Scheduler, Scheduler> onIOHandler;
    
    /** Prevents changing the plugins. */
    private static volatile boolean lockdown;
    
    /**
     * Prevents changing the plugins from then on.
     * <p>This allows container-like environments to prevent clients
     * messing with plugins. 
     */
    public static void lockdown() {
        lockdown = true;
    }
    
    /**
     * Returns true if the plugins were locked down.
     * @return true if the plugins were locked down
     */
    public static boolean isLockdown() {
        return lockdown;
    }
    
    public static Function<Scheduler, Scheduler> getComputationSchedulerHandler() {
        return onComputationHandler;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> Function<Publisher<T>, Publisher<T>> getCreateHandler() {
        return (Function)onCreateHandler;
    }

    public static Consumer<Throwable> getErrorHandler() {
        return errorHandler;
    }
    
    public static Function<Scheduler, Scheduler> getInitComputationSchedulerHandler() {
        return onInitComputationHandler;
    }

    public static Function<Scheduler, Scheduler> getInitIOSchedulerHandler() {
        return onInitIOHandler;
    }
    
    public static Function<Scheduler, Scheduler> getInitSingleSchedulerHandler() {
        return onInitSingleHandler;
    }

    public static Function<Scheduler, Scheduler> getIOSchedulerHandler() {
        return onIOHandler;
    }
    
    public static Function<Runnable, Runnable> getScheduleHandler() {
        return onScheduleHandler;
    }
    public static Function<Scheduler, Scheduler> getSingleSchedulerHandler() {
        return onSingleHandler;
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> Function<Subscriber<T>, Subscriber<T>> getSubscribeHandler() {
        return (Function)onSubscribeHandler;
    }
    
    public static Scheduler initComputationScheduler(Scheduler defaultScheduler) {
        Function<Scheduler, Scheduler> f = onInitComputationHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return f.apply(defaultScheduler); // JIT will skip this
    }

    public static Scheduler initIOScheduler(Scheduler defaultScheduler) {
        Function<Scheduler, Scheduler> f = onInitIOHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return f.apply(defaultScheduler);
    }

    public static Scheduler initSingleScheduler(Scheduler defaultScheduler) {
        Function<Scheduler, Scheduler> f = onInitSingleHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return f.apply(defaultScheduler);
    }

    public static Scheduler onComputationScheduler(Scheduler defaultScheduler) {
        Function<Scheduler, Scheduler> f = onComputationHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return f.apply(defaultScheduler);
    }
    /**
     * Called when an Observable is created.
     * @param publisher
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes"})
    public static <T> Publisher<T> onCreate(Publisher<T> publisher) {
        Function<Publisher<Object>, Publisher<Object>> f = onCreateHandler;
        if (f == null) {
            return publisher;
        }
        return (Publisher)((Function)f).apply(publisher);
    }
    /**
     * Called when an undeliverable error occurs.
     * @param error the error to report
     */
    public static void onError(Throwable error) {
        Consumer<Throwable> f = errorHandler;
        if (f != null) {
            try {
                f.accept(error);
                return;
            } catch (Throwable e) {
                if (error == null) {
                    error = new NullPointerException();
                }
                error.addSuppressed(e);
            }
        } else {
            if (error == null) {
                error = new NullPointerException();
            }
        }
        error.printStackTrace();
    }
    
    public static Scheduler onIOScheduler(Scheduler defaultScheduler) {
        Function<Scheduler, Scheduler> f = onIOHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return f.apply(defaultScheduler);
    }

    /**
     * Called when a task is scheduled.
     * @param run
     * @return
     */
    public static Runnable onSchedule(Runnable run) {
        Function<Runnable, Runnable> f = onScheduleHandler;
        if (f == null) {
            return run;
        }
        return f.apply(run);
    }

    public static Scheduler onSingleScheduler(Scheduler defaultScheduler) {
        Function<Scheduler, Scheduler> f = onSingleHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return f.apply(defaultScheduler);
    }

    /**
     * Called when a subscriber subscribes to an observable.
     * @param subscriber
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes"})
    public static <T> Subscriber<T> onSubscribe(Subscriber<T> subscriber) {
        Function<Subscriber<Object>, Subscriber<Object>> f = onSubscribeHandler;
        if (f == null) {
            return subscriber;
        }
        return (Subscriber)((Function)f).apply(subscriber);
    }

    /**
     * Removes all handlers and resets the default behavior.
     */
    public static void reset() {
        setCreateHandler(null);
        setErrorHandler(null);
        setScheduleHandler(null);
        setSubscribeHandler(null);
        
        setComputationSchedulerHandler(null);
        setInitComputationSchedulerHandler(null);
        setInitIOSchedulerHandler(null);
        setIOSchedulerHandler(null);
        setInitSingleSchedulerHandler(null);
        setSingleSchedulerHandler(null);
    }

    public static void setComputationSchedulerHandler(Function<Scheduler, Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onComputationHandler = handler;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> void setCreateHandler(Function<Publisher<T>, Publisher<T>> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onCreateHandler = (Function)handler;
    }

    public static void setErrorHandler(Consumer<Throwable> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        errorHandler = handler;
    }

    public static void setInitComputationSchedulerHandler(Function<Scheduler, Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onInitComputationHandler = handler;
    }

    public static void setInitIOSchedulerHandler(Function<Scheduler, Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onInitIOHandler = handler;
    }

    public static void setInitSingleSchedulerHandler(Function<Scheduler, Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onInitSingleHandler = handler;
    }

    public static void setIOSchedulerHandler(Function<Scheduler, Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onIOHandler = handler;
    }

    public static void setScheduleHandler(Function<Runnable, Runnable> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onScheduleHandler = handler;
    }

    public static void setSingleSchedulerHandler(Function<Scheduler, Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onSingleHandler = handler;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> void setSubscribeHandler(Function<Subscriber<T>, Subscriber<T>> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onSubscribeHandler = (Function)handler;
    }

    /**
     * Rewokes the lockdown, only for testing purposes.
     */
    /* test. */void unlock() {
        lockdown = false;
    }
    
    private RxJavaPlugins() {
        throw new IllegalStateException("No instances!");
    }
}