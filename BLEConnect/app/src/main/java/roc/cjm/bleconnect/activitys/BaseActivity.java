package roc.cjm.bleconnect.activitys;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import roc.cjm.bleconnect.R;


public class BaseActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        if(getSupportActionBar() != null) {
            //getSupportActionBar().hide();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getColor(R.color.colorPrimary));
        }
    }


}
