package online.pins24.remotestartengine;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

//В этом классе реализуется абстрактный метод onReceive(), который вызывается системой каждый раз при получении сообщения.
public class MessageReceiver extends WakefulBroadcastReceiver {
    InputSms inputSms;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Проверяем что нам пришла смс
        inputSms = SmsUtils.extractFromIntent(context, intent);

        if (inputSms != null) {
            String message = inputSms.getMessage();
            if (message != null && (message.startsWith("RSE", 0))) {
                //Стартуем сервис для обработки смс
                Intent service = MessageService.getIntentForLongSms(context, inputSms);
                startWakefulService(context, service);
                //прерываем обработку смс стандартным манагером
                abortBroadcast();
            }
        }
    }
}
