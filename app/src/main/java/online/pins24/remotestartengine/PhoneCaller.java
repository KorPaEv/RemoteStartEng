package online.pins24.remotestartengine;

import android.Manifest;
import android.app.Activity;
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

    private TelephonyManager _telephonyManager;
    private PhoneCallerCallBack _phoneCallerCallBack;
    private Context _context;
    private boolean _isPhoneCalling = false;

    public PhoneCaller(Context context) {
        _context = context;
        //Сам менеджер звонков устройства - получаем сервис системный
        _telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    //region Setters
    public void setPhoneCallerCallBack(PhoneCallerCallBack phoneCallerCallBack) {
        _phoneCallerCallBack = phoneCallerCallBack;
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
                _isPhoneCalling = true;
                break;
            //если звонилка завершила свою работу
            case TelephonyManager.CALL_STATE_IDLE:
                if (_isPhoneCalling) {
                    //либо мы стартуем двигло - тогда запускаем таймер прогрева и возвращаем наш экран приложухи обратно со всеми установками
                    //либо его глушим - тогда стопим таймер и выставляем все вьюхи на экране согласно функционалу стоп
                    doSomething();
                    _isPhoneCalling = false;
                    //убиваем листенер - зачем дальше слушать что то?
                    _telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);

                    // restart app
                    //получили интент нашей запущенной приложухи
                    Intent i = _context.getPackageManager().getLaunchIntentForPackage(_context
                            .getPackageName());
                    //добавляем флаг что мы его возобновляем а не запускаем с нуля сброшенным
                    i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    //ну и стартуем приложуху
                    _context.startActivity(i);
                }
                break;
            default:
                throw new IllegalStateException("TelephonyManager STATE IS UNKNOWN");
        }
    }

    private void doSomething() {
        if (_phoneCallerCallBack == null) {
            return;
        } else {
            _phoneCallerCallBack.onChoiseRunOrStopTimer();
        }
    }

    public void callTo(String callToNum) {
        try {
            //теперь определяем что нам надо делать, а именно отслеживаем само состояние подключения если у нас идет активный вызов
            _telephonyManager.listen(this, PhoneStateListener.LISTEN_CALL_STATE);
            // создаем интент звонилку
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //парсим куда звоним и передаем на исполнение в интент
            callIntent.setData(Uri.parse(String.format("tel:%s", callToNum)));
            //проверяем права приложения на звонок
            if (ActivityCompat.checkSelfPermission(_context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //стартуем звонилку
            _context.startActivity(callIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(_context, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //region Создаем интерфейс
    public interface PhoneCallerCallBack {
        void onChoiseRunOrStopTimer();
    }
    //endregion
}
