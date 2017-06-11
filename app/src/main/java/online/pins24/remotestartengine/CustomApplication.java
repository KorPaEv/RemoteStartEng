package online.pins24.remotestartengine;

import android.app.Application;

public class CustomApplication extends Application {

    private PhoneCaller phoneCaller;
    private TimerWorkDelay timerWorkDelay;

    public PhoneCaller getPhoneCaller() {
        if (phoneCaller == null) {
            phoneCaller = new PhoneCaller(this);
        }
        return phoneCaller;
    }

    public TimerWorkDelay getTimerWorkDelay() {
        if (timerWorkDelay == null) {
            timerWorkDelay = new TimerWorkDelay();
        }
        return timerWorkDelay;
    }
}
