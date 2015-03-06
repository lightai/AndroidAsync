import android.os.Handler;
import android.os.Looper;

/**
 * simple Synchronous or Asynchronous task execution for android
 * <p/>
 * Created by wangpeng on 15-1-19.
 */
public class Async {
    private final static String TAG = "Async";

    private static Handler sMainHandler = new Handler(Looper.getMainLooper());

    /**
     * run on background thread
     */
    public static void runOnBgThread(final Runnable runnable) {
        if (runnable == null) {
            return;
        }
        new SimpleAsyncTask<Void>() {

            @Override
            protected Void doInBackground() {
                runnable.run();
                return null;
            }
        }.execute();
    }

    /**
     * run on main thread
     */
    public static void runOnUiThread(final Runnable runnable) {
        if (runnable == null || sMainHandler == null) {
            return;
        }

        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
            return;
        }

        sMainHandler.post(runnable);
    }

    private abstract static class SimpleAsyncTask<T> extends AsyncTask<Object, Object, T> {

        @Override
        protected T doInBackground(Object[] params) {
            return doInBackground();
        }

        protected abstract T doInBackground();
    }

}
