import android.os.Handler;
import android.os.Looper;

import facebook.internal.Preconditions;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * utils of Asynchronous task execution</p>
 * use with carefully as inner Callable<T> class can leak object</p>
 * 分类线程池
 *
 * @author wangpeng
 */
public class Async {
    private final static String TAG = "Async";

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    // Allows for simultaneous reads and writes.
    private static final int NUM_IO_BOUND_THREADS = 2;
    private static final int NUM_CPU_BOUND_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_SECONDS = 60;

    static final ExecutorService sIoBoundExecutor = Executors.newFixedThreadPool(NUM_IO_BOUND_THREADS);
    static final ExecutorService sCpuBoundExecutor = new ThreadPoolExecutor(
            1,                     // keep at least that many threads alive
            NUM_CPU_BOUND_THREADS, // maximum number of allowed threads
            KEEP_ALIVE_SECONDS,    // amount of seconds each cached thread waits before being terminated
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    // only one thread for sqlite read or write
    static final ExecutorService sDbExecutor = Executors.newSingleThreadExecutor();

    private Async() {
    }

    /**
     * run on background thread
     */
    public static <T> Future<T> runOnBgThread(Callable<T> callable) {
        Preconditions.checkNotNull(callable);
        return sCpuBoundExecutor.submit(callable);
    }

    /**
     * run on background thread
     */
    public static void runOnBgThread(Runnable runnable) {
        Preconditions.checkNotNull(runnable);
        sCpuBoundExecutor.submit(runnable);
    }

    /**
     * run on dedicated io thread
     */
    public static <T> Future<T> runOnIoThread(Callable<T> callable) {
        Preconditions.checkNotNull(callable);
        return sIoBoundExecutor.submit(callable);
    }

    /**
     * run on dedicated io thread
     */
    public static void runOnIoThread(Runnable runnable) {
        Preconditions.checkNotNull(runnable);
        sIoBoundExecutor.submit(runnable);
    }

    /**
     * run on dedicated single db thread
     * all db operation use single thread,so we need not
     * care about synchronized
     * <p/>
     * 除非分别操作不同的数据库文件，多个线程操作同一个
     * 数据库文件弊大于利。所以这里只分配一个操作数据库的
     * 线程。
     */
    public static <T> Future<T> runOnDbThread(Callable<T> callable) {
        Preconditions.checkNotNull(callable);
        return sDbExecutor.submit(callable);
    }

    /**
     * run on dedicated single db thread
     * all db operation use single thread,so we need not
     * care about synchronized
     * <p/>
     * 除非分别操作不同的数据库文件，多个线程操作同一个
     * 数据库文件弊大于利。所以这里只分配一个操作数据库的
     * 线程。
     */
    public static void runOnDbThread(Runnable runnable) {
        Preconditions.checkNotNull(runnable);
        sDbExecutor.submit(runnable);
    }

    /**
     * run on main thread
     */
    public static void runOnUiThread(final Runnable runnable) {
        Preconditions.checkNotNull(runnable);

        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
            return;
        }

        sMainHandler.post(runnable);
    }

    /**
     * run delayed on main thread
     *
     * @param delayMillis ms
     */
    public static void runDelayedOnUiThread(final Runnable runnable, long delayMillis) {
        Preconditions.checkNotNull(runnable);
        sMainHandler.postDelayed(runnable, delayMillis);
    }

    /**
     * 从主线程handler移除runnable，
     * see：{@link #runOnUiThread(Runnable),#runDelayedOnUiThread(Runnable, long)}
     */
    public static void removeFromUiThread(final Runnable runnable) {
        if (runnable == null) return;
        sMainHandler.removeCallbacks(runnable);
    }
}
