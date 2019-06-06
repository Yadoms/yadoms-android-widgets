package com.yadoms.widgets.application;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.yadoms.widgets.R;
import com.yadoms.widgets.shared.ResourceHelper;
import com.yadoms.widgets.shared.restClient.Client;
import com.yadoms.widgets.shared.restClient.GetResponseHandler;

//TODO ajouter le support du français

public class MainActivity
        extends AppCompatActivity {
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

        ReadWidgetsStateWorker.startService(false);
    }

    private void checkConnected() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!NetworkStateReceiver.networkIsAvailable()) {
                        onConnectionEvent(false);
                        return;
                    }

                    Client client = new Client(getApplicationContext());
                    client.withTimeout(2000);
                    client.getLastEvent(new GetResponseHandler() {
                        @Override
                        public void onSuccess(Object[] objects) {
                            onConnectionEvent(true);
                        }

                        @Override
                        public void onFailure() {
                            onConnectionEvent(false);
                        }
                    });
                } catch (InvalidConfigurationException ignored) {
                    onConnectionEvent(false);
                }
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
                textView.setTextColor(connected ? ResourceHelper.getColorFromResource(getApplicationContext(), R.color.yadomsOfficial) : Color.RED);
                textView.setText(connected ? R.string.connection_ok : R.string.connection_failed);

                checkConnectionTimerHandler.postDelayed(checkConnectionTimerRunnable, 2000);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainPreferences.invalid();
        checkConnected();
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkConnectionTimerHandler.removeCallbacks(checkConnectionTimerRunnable);
    }
}
