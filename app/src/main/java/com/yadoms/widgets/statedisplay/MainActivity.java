package com.yadoms.widgets.statedisplay;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

//TODO ajouter le support du français

public class MainActivity
        extends AppCompatActivity {
    YadomsRestClient client = null;

    Handler checkConnectionTimerHandler = new Handler();
    Runnable checkConnectionTimerRunnable = new Runnable() {
        @Override
        public void run() {
            checkConnected();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScreenStateReceiver.start(getApplicationContext());

        checkConnected();

        ReadWidgetsStateWorker.startService();
    }

    private void checkConnected() {
        // TODO ne se connecte pas après données de l'appli effacées
        if (client == null) {
            try {
                client = new YadomsRestClient(this);
            } catch (InvalidConfigurationException ignored) {
                onConnectionEvent(false);
                return;
            }
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                client.getLastEvent(new YadomsRestGetResponseHandler() {
                    @Override
                    void onSuccess(Object[] objects) {
                        onConnectionEvent(true);
                    }

                    @Override
                    void onFailure() {
                        onConnectionEvent(false);
                    }
                });
            }
        });
    }

    public void openSettingsActivity(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void onConnectionEvent(final boolean connected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(R.id.connectionStateText);
                textView.setTextColor(connected ? Color.GREEN : Color.RED);
                textView.setText(connected ? R.string.connection_ok : R.string.connection_failed);//TODO corriger les couleurs

                checkConnectionTimerHandler.postDelayed(checkConnectionTimerRunnable, 1000);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkConnectionTimerHandler.removeCallbacks(checkConnectionTimerRunnable);
    }
}
