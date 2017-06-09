package online.pins24.remotestartengine;

import android.os.Handler;
import android.widget.TextView;
import java.util.concurrent.TimeUnit;

//Вторая реализация таймера и события что делать по его окончанию
public class TimerWorkDelay {

    private TimerWorkDelayCallBack timerWorkDelayCallBack;
    private Handler handler = new Handler();
    private int COUNTER = 15;
    private static long ONE_MIN = TimeUnit.MINUTES.toMillis(1);

    public interface TimerWorkDelayCallBack {
        void doSomethingCallBackTWD();
        void changeViewFragmentTWD();
    }

    public void setTimerWorkDelayCallBack(TimerWorkDelayCallBack timerWorkDelayCallBack) {
        this.timerWorkDelayCallBack = timerWorkDelayCallBack;
    }

    Runnable tickTimer = new Runnable() {
        @Override
        public void run() {
            tickTimer();
        }
    };

    public void startTimer() {
        tickTimer();
    }

    public void stopTimer() {
        // Удаляем Runnable-объект для прекращения задачи
        handler.removeCallbacks(tickTimer);
    }

    public void tickTimer() {
        COUNTER--;
        if (COUNTER > 0) {
            timerWorkDelayCallBack.changeViewFragmentTWD();
            scheduleNextMinute();
        }
        else {
            doSomething();
        }
    }

    //Таймер на минуту
    public void scheduleNextMinute() {
        handler.postDelayed(tickTimer, ONE_MIN);
    }

    private void doSomething() {
        if (timerWorkDelayCallBack == null) {
            return;
        }
        else {
            timerWorkDelayCallBack.doSomethingCallBackTWD();
        }
    }

    public void changeTextViewVal(TextView tv) {
        tv.setText(String.format("%02d", COUNTER));
    }
}


