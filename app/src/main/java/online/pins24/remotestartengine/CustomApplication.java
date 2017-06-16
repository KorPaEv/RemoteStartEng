package online.pins24.remotestartengine;

import android.app.Application;

//поместим в Application работу с таймером и звонилкой, так же в манифесте укажем что работаем с кастомным Application
public class CustomApplication extends Application {

    public static boolean activityVisible; //Флажок видимости текущих активити

    private PhoneCaller phoneCaller;
    private CustomTimer customTimer;

    public PhoneCaller getPhoneCaller() {
        if (phoneCaller == null) {
            phoneCaller = new PhoneCaller(this);
        }
        return phoneCaller;
    }

    public CustomTimer getCustomTimer() {
        if (customTimer == null) {
            customTimer = new CustomTimer();
        }
        return customTimer;
    }

    public static boolean isActivityVisible() {
        return activityVisible; // return true or false
    }

    public static void activityResumed() {
        activityVisible = true;// this will set true when activity resumed

    }

    public static void activityPaused() {
        activityVisible = false;// this will set false when activity paused
    }
}
