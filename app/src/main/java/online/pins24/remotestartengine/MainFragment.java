package online.pins24.remotestartengine;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

public class MainFragment extends BaseFragment implements TimerWorkDelay.TimerWorkDelayCallBack, PhoneCaller.PhoneCallerCallBack
{
    //region КОНСТАНТЫ
    private final String SHAREDPREF = "SharedPref";
    private final String PHONESHAREDPREF = "PhoneSharedPref";
    private final String TEMPVALSHAREDPREF = "TempValSharedPref";
    private final String RESET = "reset";
    private final String CELSIUS = "\u2103";
    //endregion
    //region ОБЪЯВЛЯЕМ ОБЪЕКТЫ
    SharedPreferences sharedPref;
    private Button bStartEngine, bStopEngine, bResetDevice, bActivateDevice;
    private EditText etPhone;
    private TextView tvTimer;
    private TextView tvTempNotifIsActive;
    private LinearLayout imgLayout;
    Context appContext;
    private MediaPlayer mp;
    private CustomApplication customApplication;

    //endregion
    //region ПЕРЕМЕННЫЕ
    private String currentPhone;
    private boolean startFlag = false;
    private String currTempStringValue = null;
    //endregion

    //region Вызов жизненных циклов фрагментов
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //получаем контекст приложения - ссылку на сам объект приложения (читай что такое контекст)
        appContext = getContext().getApplicationContext();
        customApplication = (CustomApplication)getContext().getApplicationContext();
        //ищем наши вьюхи на активити
        findViews();
        //загрузка данных в переменные
        fillData();
        //работаем с таймером через Application, цепляем интерфейс
        customApplication.getTimerWorkDelay().setTimerWorkDelayCallBack(this);
        //менеджер звонков
        customApplication.getPhoneCaller().setPhoneCallerCallBack(this);
    }
    //endregion

    @Override
    public void onDestroyView() {
        //Убиваем наши объекты звонилки и таймера при сворачивании приложения
        //При этом фоном таймер продолжает свою работу
        customApplication.getTimerWorkDelay().setTimerWorkDelayCallBack(null);
        customApplication.getPhoneCaller().setPhoneCallerCallBack(null);
        super.onDestroyView();
    }

    //region findViews() Поиск вьюх определенных в R.id
    private void findViews()
    {
        View rootView = getView();
        bActivateDevice = (Button) rootView.findViewById(R.id.bActivateDevice);
        bResetDevice = (Button) rootView.findViewById(R.id.bResetDevice);
        bStartEngine = (Button) rootView.findViewById(R.id.bStartEngine);
        bStopEngine = (Button) rootView.findViewById(R.id.bStopEngine);
        etPhone = (EditText) rootView.findViewById(R.id.etPhone);
        tvTimer = (TextView) rootView.findViewById(R.id.tvTimer);
        tvTempNotifIsActive = (TextView) rootView.findViewById(R.id.tvTempNotif);
        imgLayout = (LinearLayout) rootView.findViewById(R.id.imageLayout);

        bStartEngine.setTextColor(Color.WHITE);
        bStopEngine.setTextColor(Color.WHITE);
        bActivateDevice.setTextColor(Color.WHITE);
        bResetDevice.setTextColor(Color.WHITE);

        bStartEngine.setOnClickListener(bClickListener);
        bStopEngine.setOnClickListener(bClickListener);
        bActivateDevice.setOnClickListener(bClickListener);
        bResetDevice.setOnClickListener(bClickListener);

    }
    //endregion

    //region OnClickListener листенер на клик кнопок
    public View.OnClickListener bClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bStartEngine: startButtonClick();
                    break;
                case R.id.bStopEngine: stopButtonClick();
                    break;
                case R.id.bActivateDevice: activateButtonClick();
                    break;
                case R.id.bResetDevice: resetButtonClick();
                    break;
            }
        }
    };
    //endregion

    //region fillData() Загрузка данных при старте приложения
    private void fillData() {
        Toast.makeText(appContext, "Загрузка...", Toast.LENGTH_SHORT).show();
        //Используем созданный файл данных SharedPreferences:
        sharedPref = appContext.getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);
        currentPhone = sharedPref.getString(PHONESHAREDPREF, null);
        currTempStringValue = sharedPref.getString(TEMPVALSHAREDPREF, null);
        etPhone.setText(currentPhone);

        //устанавливаем видимость объектов на экране
        setViewStates(true);

        //если таймер = 0, но телефон забит куда звоним, то режим ожидания
        if (customApplication.getTimerWorkDelay().getCounter() == 0 && !TextUtils.isEmpty(currentPhone)) {
            //смена изображения машины на экране
            imgChangeCar(R.drawable.car_waiting);
            //Устанавливаем кнопку активации не активной, номер не радактируемый
            setViewStates(false);
        } else if (customApplication.getTimerWorkDelay().getCounter() > 0) {
            //если же таймер существует и работает, то рисуем его на экране фрагмента
            updateTextViewVal();
            imgChangeCar(R.drawable.car_started);
            bResetDevice.setEnabled(false);
            bStartEngine.setEnabled(false);
            setViewStates(false);
        }

        if(TextUtils.isEmpty(currTempStringValue) || TextUtils.equals(currTempStringValue, "0")) {
            tvTempNotifIsActive.setVisibility(View.GONE);
        } else {
            String res = getString(R.string.temp_notification) + " " + currTempStringValue + CELSIUS;
            tvTempNotifIsActive.setVisibility(View.VISIBLE);
            tvTempNotifIsActive.setText(res);
        }
    }
    //endregion

    //region setViewStates Видимость объектов на экране
    private void setViewStates(Boolean state) {
        etPhone.setEnabled(state);
        bActivateDevice.setEnabled(state);
    }
    //endregion

    //region SaveSharedPref() Читаем сохраненные настройки
    private void saveSharedPref() {
        Context context = getContext();
        Toast.makeText(context, "Сохраняем...", Toast.LENGTH_SHORT).show();
        //Создаем объект Editor для создания пар имя-значение:
        sharedPref = context.getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);
        //Создаем объект Editor для создания пар имя-значение:
        SharedPreferences.Editor shpEditor = sharedPref.edit();
        currentPhone = etPhone.getText().toString();
        shpEditor.putString(PHONESHAREDPREF, currentPhone);
        shpEditor.commit();
    }
    //endregion

    //region activateButtonClick Кнопка активации устройства
    public void activateButtonClick() {
        //функция звонилка
        generalCallFunc();
    }
    //endregion

    //region startButtonClick Обработка нажатия кнопки Старт
    public void startButtonClick() {
        //Спрашиваем надо ли нам это
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());

        alertDialog.setTitle("Старт авто...");
        alertDialog.setMessage("Заводим?");

        //region YES CLICK
        alertDialog.setPositiveButton("ДА", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                //Стартуем двигло если надо
                doStartEngine();
            }
        });
        //endregion

        //region NO CLICK
        alertDialog.setNegativeButton("НЕТ", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        //endregion
        alertDialog.show();
    }
    //endregion

    //region doStartEngine() Выполняем старт двигла если захотели все таки
    private void doStartEngine()
    {
        startFlag = true;
        //Вызываем звонилку
        generalCallFunc();
    }
    //endregion

    //region stopButtonClick Нажатие кнопки Стоп
    public void stopButtonClick()
    {
        //Спросили хотим ли застопить принудительно
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());

        alertDialog.setTitle("Стоп машина...");
        alertDialog.setMessage("Глушим?");

        //region YES CLICK
        alertDialog.setPositiveButton("ДА", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                //Если захотели все же то застопили
                doStopEngine();
            }
        });
        //endregion

        //region NO CLICK
        alertDialog.setNegativeButton("НЕТ", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        //endregion
        alertDialog.show();
    }
    //endregion

    //region doStopEngine() Выполняем стоп двигла по звонку
    private void doStopEngine()
    {
        startFlag = false;
        generalCallFunc();
    }
    //endregion

    //region resetButtonClick Обработка нажатия кнопки сброса устройтва
    public void resetButtonClick()
    {
        //Задаем вопрос стоит ли нам сбрасывать
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());

        alertDialog.setTitle("Сброс устройства...");
        alertDialog.setMessage("Вы уверены?");

        //region YES CLICK
        alertDialog.setPositiveButton("ДА", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                //Если ДА то сбрасываем устройство
                doReset(currentPhone);
            }
        });
        //endregion

        //region NO CLICK
        alertDialog.setNegativeButton("НЕТ", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        //endregion
        alertDialog.show();
    }
    //endregion

    //region doReset Сброс устройства
    private void doReset(String number)
    {
        currentPhone = etPhone.getText().toString();
        if (TextUtils.isEmpty(number))
        {
            etPhone.requestFocus();
            etPhone.setError("Что сбрасываем?");
            return;
        }
        //Посылаем команду на сброс устройству
        sendSms(number, RESET);
        //вертаем все на место
        setViewStates(true);
        etPhone.setText(null);
        //сохраняемся
        saveSharedPref();
        //картинку в начальное состояние предактивации
        imgChangeCar(R.drawable.car_background);
    }
    //endregion

    //region generalCallFunc() Главная функция звонилка
    private void generalCallFunc() {
        currentPhone = etPhone.getText().toString();
        if (TextUtils.isEmpty(currentPhone))
        {
            etPhone.requestFocus();
            etPhone.setError("Куда звоним?");
            return;
        }
        //Сохранили настройки приложухи в память
        saveSharedPref();
        //куда именно звоним
        callToDevice(currentPhone);
    }
    //endregion

    //region callToDevice Куда именно звоним
    private void callToDevice(String phoneNum)
    {
        customApplication.getPhoneCaller().callTo(phoneNum);
    }
    //endregion

    //region onChoiseRunOrStopTimer() Переопределяем функцию из PhoneCaller
    @Override
    public void onChoiseRunOrStopTimer() {
        //либо мы стартуем двигло - тогда запускаем таймер прогрева и возвращаем наш экран приложухи обратно со всеми установками
        //либо его глушим - тогда стопим таймер и выставляем все вьюхи на экране согласно функционалу стоп
        if (startFlag) runTimerAndReturnActivity();
        else stopTimerAndReturnActivity();
    }
    //endregion

    //region runTimerAndReturnActivity() Стартуем таймер прогрева и запускаем приложуние обратно после звонилки
    private void runTimerAndReturnActivity() {
        //меняем картинку что двигло начало греться
        imgChangeCar(R.drawable.car_started);
        //отрисовали на экране кнопки как надо - выключили чтобы лишнего не натыкать
        bResetDevice.setEnabled(false);
        bStartEngine.setEnabled(false);

        //стартуем ивент по таймеру
        customApplication.getTimerWorkDelay().startTimer();
    }
    //endregion

    //region onTimerStop() и onTimerChanged() Переопределяем функции из TimerWorkDelay - связь класса и активити
    @Override
    public void onTimerStop() {
        tvTimer.setText(getString(R.string.timerDefault));
        imgChangeCar(R.drawable.car_waiting);
        bResetDevice.setEnabled(true);
        bStartEngine.setEnabled(true);
        playEngineReady();
    }

    @Override
    public void onTimerChanged() {
        updateTextViewVal();
    }

    public void updateTextViewVal() {
        tvTimer.setText(String.format("%02d", customApplication.getTimerWorkDelay().getCounter()));
    }
    //endregion

    //region stopTimerAndReturnActivity() Стоп таймера
    private void stopTimerAndReturnActivity() {
        customApplication.getTimerWorkDelay().stopTimerManual();
        //возвращаем начальные установки
        tvTimer.setText(getString(R.string.timerDefault));
        imgChangeCar(R.drawable.car_waiting);
        bResetDevice.setEnabled(true);
        bStartEngine.setEnabled(true);
        fillData();
    }
    //endregion

    //region playEngineReady() Будильник для таймера
    private void playEngineReady()
    {
        //Выбрали мелодию и проиграли ее
        mp = MediaPlayer.create(appContext, R.raw.ready_long);
        try
        {
            if (mp.isPlaying())
            {
                mp.stop();
                mp.release();
                mp = MediaPlayer.create(appContext, R.raw.ready_long);
            }
            mp.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    //endregion

    //region SendSms Функция отправки смс
    public void sendSms(String number, String message)
    {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(number, null, message, null, null);
    }
    //endregion

    //region imgChangeCar Функция смены изображений - старт или стоп или сброс
    private void imgChangeCar(int idDrawable)
    {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
        {
            imgLayout.setBackgroundDrawable(getResources().getDrawable(idDrawable));
        }
        else
        {
            imgLayout.setBackground(getResources().getDrawable(idDrawable));
        }
    }
    //endregion

    @Override
    public void onResume()
    {
        super.onResume();
        //fillData();
    }

    //region onPause() Чего делаем если свернули приложуху - сохраняемся же!
    @Override
    public void onPause()
    {
        super.onPause();
        saveSharedPref();
        //и еще убрали фокус с номеранаберателя чтобы глаза клава не мозолила
        etPhone.clearFocus();
    }
    //endregion
}