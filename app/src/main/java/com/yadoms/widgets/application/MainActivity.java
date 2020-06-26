package com.yadoms.widgets.application;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.yadoms.widgets.R;
import com.yadoms.widgets.shared.ResourceHelper;
import com.yadoms.widgets.shared.restClient.Client;
import com.yadoms.widgets.shared.restClient.GetResponseHandler;

import androidx.appcompat.app.AppCompatActivity;

//TODO ajouter le support du français

public class MainActivity
        extends AppCompatActivity
{
    class CancellableRunnable
            implements Runnable
    {
        private boolean m_stopAsked = false;

        void stop()
        {
            m_stopAsked = true;
        }

        @Override
        public void run()
        {
            while (true)
            {
                if (m_stopAsked)
                {
                    m_stopAsked = false;
                    return;
                }

                try
                {
                    Client client = new Client(getApplicationContext());
                    client.withTimeout(2000);
                    client.getLastEvent(new GetResponseHandler()
                    {
                        @Override
                        public void onSuccess(Object[] objects)
                        {
                            onConnectionEvent(true);

                            try
                            {
                                Thread.sleep(2000);
                            }
                            catch (InterruptedException ignored)
                            {
                            }
                        }

                        @Override
                        public void onFailure()
                        {
                            onConnectionEvent(false);
                            // No need to wait to restart connection test, timeout make us already wait enough
                        }
                    });
                }
                catch (InvalidConfigurationException ignored)
                {
                    onConnectionEvent(false);
                }
            }
        }
    }

    private CancellableRunnable m_checkConnectionTask = new CancellableRunnable();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScreenStateReceiver.start(getApplicationContext());
    }

    public void openSettingsActivity(View view)
    {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void onConnectionEvent(final boolean connected)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                TextView textView = findViewById(R.id.connectionStateText);
                textView.setTextColor(connected ? ResourceHelper.getColorFromResource(
                        getApplicationContext(),
                        R.color.yadomsOfficial) : Color.RED);
                textView.setText(connected ? R.string.connection_ok : R.string.connection_failed);
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        MainPreferences.invalid();
        AsyncTask.execute(m_checkConnectionTask);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        m_checkConnectionTask.stop();
    }
}
