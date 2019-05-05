package com.yadoms.widgets.statedisplay;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * The configuration screen for the {@link SwitchAppWidget SwitchAppWidget} AppWidget.
 */
public class SwitchAppWidgetConfigureActivity
        extends Activity
{
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Keyword selectedKeyword;

    ViewHolder viewHolder;

    View.OnClickListener onSubmitButtonClick = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            final Context context = SwitchAppWidgetConfigureActivity.this;

            // Save prefs
            widgetPrefs prefs = new widgetPrefs(context, mAppWidgetId);
            prefs.keyword = selectedKeyword.getId();
            prefs.label = viewHolder.labelEditText.getText().toString();
            prefs.save();

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            SwitchAppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);

            // Start monitoring widget state (if not already started)
            ReadWidgetsStateJobService.start(getApplicationContext());

            finish();
        }
    };

    public SwitchAppWidgetConfigureActivity()
    {
        super();
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.switch_app_widget_configure);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
        {
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
            client.getDevicesWithCapacity(EKeywordAccessMode.GetSet,
                    "switch",
                    new YadomsRestGetResponseHandler()
                    {
                        @Override
                        void onSuccess(Object[] objects)
                        {
                            final Device[] devices = (Device[]) objects;
                            ArrayAdapter<Device> aa = new ArrayAdapter<>(getApplicationContext(),
                                    android.R.layout.simple_spinner_dropdown_item,
                                    devices);
                            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            viewHolder.deviceSelectionSpinner.setAdapter(aa);
                            viewHolder.deviceSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                            {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView,
                                                           View view,
                                                           int i,
                                                           long l)
                                {
                                    client.getDeviceKeywords(devices[i].getId(),
                                            new YadomsRestGetResponseHandler()
                                            {
                                                @Override
                                                void onSuccess(Object[] objects)
                                                {
                                                    final Keyword[] keywords = (Keyword[]) objects;
                                                    ArrayAdapter<Keyword> aa = new ArrayAdapter<>(
                                                            getApplicationContext(),
                                                            android.R.layout.simple_spinner_dropdown_item,
                                                            keywords);
                                                    aa.setDropDownViewResource(
                                                            android.R.layout.simple_spinner_dropdown_item);
                                                    viewHolder.keywordSelectionSpinner
                                                            .setAdapter(aa);
                                                    viewHolder.keywordSelectionSpinner
                                                            .setOnItemSelectedListener(
                                                                    new AdapterView.OnItemSelectedListener()
                                                                    {
                                                                        @Override
                                                                        public void onItemSelected(AdapterView<?> adapterView,
                                                                                                   View view,
                                                                                                   int i,
                                                                                                   long l)
                                                                        {
                                                                            Log.d("KeywordSelected",
                                                                                    "keyword Id=" + keywords[i]
                                                                                            .getId() + " " + keywords[i]
                                                                                            .getFriendlyName());
                                                                            selectedKeyword = keywords[i];
                                                                            viewHolder.submitButton
                                                                                    .setEnabled(
                                                                                            true);
                                                                        }

                                                                        @Override
                                                                        public void onNothingSelected(AdapterView<?> adapterView)
                                                                        {
                                                                            selectedKeyword = null;
                                                                            viewHolder.submitButton
                                                                                    .setEnabled(
                                                                                            false);
                                                                        }
                                                                    });
                                                }
                                            });
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView)
                                {
                                    viewHolder.submitButton.setEnabled(false);
                                }
                            });
                        }
                    });
        }
        catch (InvalidConfigurationException e)
        {
            Log.e("onCreate", e.getMessage());
            //TODO ajouter modal indiquant que le serveur doit auparavant être configué et lancer l'activité de configuration du serveur
            finish();
        }
    }

    protected class ViewHolder
    {
        Spinner deviceSelectionSpinner;
        Spinner keywordSelectionSpinner;
        EditText labelEditText;
        Button submitButton;
    }
}

