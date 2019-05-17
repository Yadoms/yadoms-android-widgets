package com.yadoms.widgets.statedisplay;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.yadoms.widgets.statedisplay.preferences.DatabaseHelper;

import java.sql.SQLException;

/**
 * The configuration screen for the {@link SwitchAppWidget SwitchAppWidget} AppWidget.
 */
public class SwitchAppWidgetConfigureActivity
        extends Activity {
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Keyword selectedKeyword;

    ViewHolder viewHolder;

    View.OnClickListener onSubmitButtonClick = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = SwitchAppWidgetConfigureActivity.this;

            Widget widget = new Widget(mAppWidgetId,
                    selectedKeyword.getId(),
                    viewHolder.labelEditText.getText().toString());

            // Save
            try {
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                databaseHelper.saveWidget(widget);
            } catch (SQLException e) {
                Toast.makeText(context,
                        context.getString(R.string.unable_to_save_configuration),
                        Toast.LENGTH_LONG).show();
            }

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            SwitchAppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);

            // Start monitoring widget state (if not already started)
            if (ScreenStateReceiver.userIsPresent()) {
                ReadWidgetsStateWorker.startService();
            }

            finish();
        }
    };

    public SwitchAppWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.switch_app_widget_configure);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        viewHolder = new ViewHolder();
        viewHolder.deviceSelectionSpinner = findViewById(R.id.deviceSelectionSpinner);
        viewHolder.keywordSelectionSpinner = findViewById(R.id.keywordSelectionSpinner);
        viewHolder.labelEditText = findViewById(R.id.label);

        viewHolder.submitButton = findViewById(R.id.button);
        viewHolder.submitButton.setOnClickListener(onSubmitButtonClick);
        viewHolder.submitButton.setEnabled(false);

        try {
            final YadomsRestClient client = new YadomsRestClient(getApplicationContext());

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    client.getDevicesWithCapacity(EKeywordAccessMode.GetSet,
                            "switch",
                            new YadomsRestGetResponseHandler() {
                                @Override
                                void onSuccess(final Object[] objects) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            final Device[] devices = (Device[]) objects;
                                            ArrayAdapter<Device> aa = new ArrayAdapter<>(getApplicationContext(),
                                                    android.R.layout.simple_spinner_dropdown_item,
                                                    devices);
                                            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            viewHolder.deviceSelectionSpinner.setAdapter(aa);
                                            viewHolder.deviceSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> adapterView,
                                                                           View view,
                                                                           final int i,
                                                                           long l) {
                                                    onDeviceSelected(client, devices[i]);
                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> adapterView) {
                                                    viewHolder.submitButton.setEnabled(false);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                }
            });
        } catch (InvalidConfigurationException e) {
            Log.e("onCreate", e.getMessage());
            //TODO ajouter modal indiquant que le serveur doit auparavant être configué et lancer l'activité de configuration du serveur
            finish();
        }
    }

    void onDeviceSelected(final YadomsRestClient client, final Device deviceSelected) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                client.getDeviceKeywords(deviceSelected.getId(),
                        new YadomsRestGetResponseHandler() {
                            @Override
                            void onSuccess(final Object[] objects) {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        final Keyword[] keywords = (Keyword[]) objects;
                                        ArrayAdapter<Keyword> aa = new ArrayAdapter<>(
                                                getApplicationContext(),
                                                android.R.layout.simple_spinner_dropdown_item,
                                                keywords);
                                        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        viewHolder.keywordSelectionSpinner.setAdapter(aa);
                                        viewHolder.keywordSelectionSpinner.setOnItemSelectedListener(
                                                new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> adapterView,
                                                                               View view,
                                                                               int i,
                                                                               long l) {
                                                        onKeywordSelected(keywords[i]);
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> adapterView) {
                                                        selectedKeyword = null;
                                                        viewHolder.submitButton.setEnabled(false);
                                                    }
                                                });
                                    }
                                });

                            }
                        });
            }
        });
    }

    private void onKeywordSelected(Keyword keyword) {
        Log.d("KeywordSelected", "keyword Id=" + keyword.getId() + " " + keyword.getFriendlyName());
        selectedKeyword = keyword;
        viewHolder.submitButton.setEnabled(true);
    }

    protected class ViewHolder {
        Spinner deviceSelectionSpinner;
        Spinner keywordSelectionSpinner;
        EditText labelEditText;
        Button submitButton;
    }
}

