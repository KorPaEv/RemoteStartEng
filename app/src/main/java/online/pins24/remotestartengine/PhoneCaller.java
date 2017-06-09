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

public class PhoneCaller {

    Context _context;

    public PhoneCaller(Context _context) {
        this._context = _context;
    }

    public void callTo(PhoneCallListener phoneCallListener, String callToNum) {
        try
        {
            //Сам менеджер звонков устройства - получаем сервис системный
            TelephonyManager telephonyManager = (TelephonyManager)_context.getSystemService(Context
                    .TELEPHONY_SERVICE);
            //теперь определяем что нам надо делать, а именно отслеживаем само состояние подключения если у нас идет активный вызов
            telephonyManager.listen(phoneCallListener, PhoneStateListener.LISTEN_CALL_STATE);
            // создаем интент звонилку
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //парсим куда звоним и передаем на исполнение в интент
            callIntent.setData(Uri.parse(String.format("tel:%s", callToNum)));
            //проверяем права приложения на звонок
            if (ActivityCompat.checkSelfPermission(_context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
            //стартуем звонилку
            _context.startActivity(callIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Toast.makeText(_context, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
