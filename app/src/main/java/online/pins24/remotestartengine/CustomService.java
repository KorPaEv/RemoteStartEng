package online.pins24.remotestartengine;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

//Так на всякий случай может где-то сервис понадобится типа такого
//context.startService(new Intent(this.getActivity(), CustomService.class));
public class CustomService extends Service {
    final String LOG_TAG = "myLogs";

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

}
