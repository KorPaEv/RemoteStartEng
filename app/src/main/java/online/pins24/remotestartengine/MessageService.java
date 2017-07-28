package online.pins24.remotestartengine;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MessageService extends IntentService {
    //region Переменные и массивы
    String smsFrom, smsBody;
    float latCoord, lngCoord;
    private String[] splitSmsLatLng;
    private String strCurrDateTime; //текущая дата время
    //endregion

    private SimpleDateFormat simpleDateFormat;
    private Calendar calendar;

    public MessageService() {
        super("MessageService");
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        calendar = Calendar.getInstance();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Прочитали то что передал рессивер
        Bundle extras = intent.getExtras();

        if (extras != null) {
            // получили текст и номер сообщения
            smsBody = extras.getString("sms_body");
            smsFrom = extras.getString("sms_from");

            //Допустим что по запросу нам пришли координаты
            strCurrDateTime = simpleDateFormat.format(calendar.getTime()); //получили текущее время
            splitSmsLatLng = smsBody.split(";");
            latCoord = Float.parseFloat(splitSmsLatLng[0]);
            lngCoord = Float.parseFloat(splitSmsLatLng[1]);
            //latCoord = (float) 53.31; //получили координаты
            //lngCoord = (float) 83.79;
            //saveSharedPref(); //сохранили данные последнего запроса
            //fillData(); //загрузились
        }
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    public static Intent getIntentForLongSms(Context context, InputSms inputSms) {
        Intent service = new Intent(context, MessageService.class);

        service.putExtra("sms_from", inputSms.getNumber());
        service.putExtra("sms_body", inputSms.getMessage());

        return service;
    }
}
