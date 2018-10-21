package rut.com.messagerelay;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import rut.com.messagerelay.WiFiDirect.WiFiDirectService;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup WiFiDirect
        WiFiDirectService wiFiDirectService = new WiFiDirectService(this);
        wiFiDirectService.setup();



    }

}