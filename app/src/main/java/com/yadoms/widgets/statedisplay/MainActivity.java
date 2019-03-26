package com.yadoms.widgets.statedisplay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity
        extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = new Intent(getApplicationContext(), YadomsWebsocketService.class);
        // TODO potentially add data to the intent
        i.putExtra("KEY1", "Value to be used by the service");
        getApplicationContext().startService(i);
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
