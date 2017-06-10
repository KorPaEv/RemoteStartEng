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

public class PhoneCaller extends PhoneStateListener {

    private boolean isPhoneCalling = false;

    //region Создаем интерфейс
    public interface PhoneCallerCallBack {
        void doSomethingCallBackPhC();
    }

    private PhoneCallerCallBack phoneCallerCallBack;
    //endregion
    //region Setters
    public void setPhoneCallerCallBack(PhoneCallerCallBack phoneCallerCallBack) {
        this.phoneCallerCallBack = phoneCallerCallBack;
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
        if (phoneCallerCallBack == null) return;
        else phoneCallerCallBack.doSomethingCallBackPhC();
    }

    public void callTo(Context context, String callToNum) {
        try
        {
            //Сам менеджер звонков устройства - получаем сервис системный
            TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context
                    .TELEPHONY_SERVICE);
            //теперь определяем что нам надо делать, а именно отслеживаем само состояние подключения если у нас идет активный вызов
            telephonyManager.listen(this, PhoneStateListener.LISTEN_CALL_STATE);
            // создаем интент звонилку
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //парсим куда звоним и передаем на исполнение в интент
            callIntent.setData(Uri.parse(String.format("tel:%s", callToNum)));
            //проверяем права приложения на звонок
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
            //стартуем звонилку
            context.startActivity(callIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
