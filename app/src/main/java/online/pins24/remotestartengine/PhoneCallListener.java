package online.pins24.remotestartengine;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class PhoneCallListener extends PhoneStateListener {

    private boolean isPhoneCalling = false;

    public PhoneCallListener() {

    }

    //region Создаем интерфейс
    public interface PhoneCallListenerCallBack {
        void doSomethingCallBackPCL();
    }

    private PhoneCallListenerCallBack phoneCallListenerCallBack;
    //endregion
    //region Setters
    public void setPhoneCallListenerCallBack(PhoneCallListenerCallBack phoneCallListenerCallBack) {
        this.phoneCallListenerCallBack = phoneCallListenerCallBack;
    }
    //endregion

    //тут остлеживаем текущее состояние активного вызова и его изменение
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        switch (state) {
            //если сейчас идет звонок исходящий или входящий пофиг
            case TelephonyManager.CALL_STATE_RINGING:
                break; // phone ringing
            //если положили трубу или скинули
            case TelephonyManager.CALL_STATE_OFFHOOK:
                isPhoneCalling = true;
                break;
            //если звонилка завершила свою работу
            case TelephonyManager.CALL_STATE_IDLE:
                if (isPhoneCalling) {
                    //либо мы стартуем двигло - тогда запускаем таймер прогрева и возвращаем наш экран приложухи обратно со всеми установками
                    //либо его глушим - тогда стопим таймер и выставляем все вьюхи на экране согласно функционалу стоп
                    doSomething();
                    isPhoneCalling = false;
                }
                break;
            default:
                throw new IllegalStateException("TelephonyManager STATE IS UNKNOWN");
        }
    }

    private void doSomething() {
        if (phoneCallListenerCallBack == null) return;
        else phoneCallListenerCallBack.doSomethingCallBackPCL();
    }
}
