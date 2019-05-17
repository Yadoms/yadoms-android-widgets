package com.yadoms.widgets.statedisplay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

//TODO ajouter le support du français

public class MainActivity
        extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScreenStateReceiver.start(getApplicationContext());

        ReadWidgetsStateWorker.startService();
    }

    public void openSettingsActivity(View view)
    {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }
}
