package online.pins24.remotestartengine;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;

//Первая реализация таймера и события что делать по его окончанию
public class TimerWork {

    // счетчик времени
    private long mTime = 0L;
    Context context;

    // обработчик потока - обновляет сведения о времени
    // Создаётся в основном UI-потоке
    private Handler mHandler;

    public TimerWork(Context currContext) {
        context = currContext;
        mHandler = new Handler();
    }

    public void startTimerWork() {
        if (mTime == 0L) {
            mTime = SystemClock.uptimeMillis();
            mHandler.removeCallbacks(timeUpdaterRunnable);
            // Добавляем Runnable-объект timeUpdaterRunnable в очередь
            // сообщений, объект должен быть запущен после задержки в 100 мс
            mHandler.postDelayed(timeUpdaterRunnable, 100);
        }
    }

    // Описание Runnable-объекта
    private Runnable timeUpdaterRunnable = new Runnable() {
        public void run() {
            // вычисляем время
            final long start = mTime;
            long millis = SystemClock.uptimeMillis() - start;
            int second = (int) (millis / 1000);
            int min = second / 60;
            int hours = min / 60;
            second = second % 60;
            // повторяем через каждые 200 миллисекунд
            mHandler.postDelayed(this, 200);
            checkTimer(min);
        }
    };

    private void checkTimer(int minCount) {

    }

    public void stopTimerWork() {
        // Удаляем Runnable-объект для прекращения задачи
        mHandler.removeCallbacks(timeUpdaterRunnable);
    }
}