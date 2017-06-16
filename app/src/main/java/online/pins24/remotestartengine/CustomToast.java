package online.pins24.remotestartengine;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by admin on 14.06.2017.
 */

public class CustomToast {

    int color = 0xFFCB3E3E;
    Context _context;

    public CustomToast(Context context) {
        _context = context;
    }

    public void showToast(String str)
    {
        //создаем и отображаем текстовое уведомление
        Toast toast = Toast.makeText(_context, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(10);

        LinearLayout v = (LinearLayout)toast.getView();
        v.setBackgroundColor(Color.TRANSPARENT); //прозрачный фон

        TextView toastView = (TextView) v.getChildAt(0);
        toastView.setPadding(10, 10, 10, 10);
        toastView.setTextSize(14);

        if(Build.VERSION.SDK_INT >= 16)
            toastView.setBackground(gd);
        else toastView.setBackgroundDrawable(gd);
        toast.show();
    }
}
