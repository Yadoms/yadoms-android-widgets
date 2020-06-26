package com.yadoms.widgets.widgets.standardSwitch;

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
import android.widget.TextView;
import android.widget.Toast;

import com.yadoms.widgets.R;
import com.yadoms.widgets.application.InvalidConfigurationException;
import com.yadoms.widgets.application.MainActivity;
import com.yadoms.widgets.application.preferences.DatabaseHelper;
import com.yadoms.widgets.shared.Device;
import com.yadoms.widgets.shared.EKeywordAccessMode;
import com.yadoms.widgets.shared.Keyword;
import com.yadoms.widgets.shared.Widget;
import com.yadoms.widgets.shared.restClient.Client;
import com.yadoms.widgets.shared.restClient.GetResponseHandler;

import java.sql.SQLException;

/**
 * The configuration screen for the {@link SwitchAppWidget SwitchAppWidget} AppWidget.
 */
public class SwitchAppWidgetConfigureActivity
        extends Activity
{
    int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Keyword selectedKeyword;

    ViewHolder viewHolder;

    View.OnClickListener onSubmitButtonClick = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            final Context context = SwitchAppWidgetConfigureActivity.this;

            Widget widget = new Widget(appWidgetId,
                                       SwitchAppWidget.class.getName(),
                                       selectedKeyword.getId(),
                                       viewHolder.labelEditText.getText().toString());

            // Save
            try
            {
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                databaseHelper.saveWidget(widget);
            }
            catch (SQLException e)
            {
                Toast.makeText(context,
                               context.getString(R.string.unable_to_save_configuration),
                               Toast.LENGTH_LONG).show();
            }

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            SwitchAppWidget.updateAppWidget(context, appWidgetManager, new int[]{appWidgetId});

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);

            finish();
        }
    };

    View.OnClickListener onConfigureButtonClick = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            startActivity(new Intent(SwitchAppWidgetConfigureActivity.this,
                                     MainActivity.class));
            finish();
        }
    };

    private String selectedDeviceName;

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
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            finish();
        }

        viewHolder = new ViewHolder();
        viewHolder.deviceSelectionSpinner = findViewById(R.id.deviceSelectionSpinner);
        viewHolder.keywordSelectionSpinner = findViewById(R.id.keywordSelectionSpinner);
        viewHolder.labelEditText = findViewById(R.id.label);
        viewHolder.warningTextView = findViewById(R.id.warning);

        viewHolder.submitButton = findViewById(R.id.addWidgetButton);
        viewHolder.submitButton.setOnClickListener(onSubmitButtonClick);

        viewHolder.configureButton = findViewById(R.id.configureButton);
        viewHolder.configureButton.setOnClickListener(onConfigureButtonClick);

        try
        {
            final Client client = new Client(getApplicationContext());

            AsyncTask.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        client.getDevicesWithCapacity(EKeywordAccessMode.GetSet,
                                                      "switch",
                                                      new GetResponseHandler()
                                                      {
                                                          @Override
                                                          public void onSuccess(final Object[] objects)
                                                          {
                                                              runOnUiThread(new Runnable()
                                                              {
                                                                  @Override
                                                                  public void run()
                                                                  {
                                                                      final Device[] devices = (Device[]) objects;
                                                                      ArrayAdapter<Device> aa = new ArrayAdapter<>(
                                                                              getApplicationContext(),
                                                                              android.R.layout.simple_spinner_dropdown_item,
                                                                              devices);
                                                                      aa.setDropDownViewResource(
                                                                              android.R.layout.simple_spinner_dropdown_item);
                                                                      viewHolder.deviceSelectionSpinner
                                                                              .setAdapter(
                                                                                      aa);
                                                                      viewHolder.deviceSelectionSpinner
                                                                              .setOnItemSelectedListener(
                                                                                      new AdapterView.OnItemSelectedListener()
                                                                                      {
                                                                                          @Override
                                                                                          public void onItemSelected(AdapterView<?> adapterView,
                                                                                                                     View view,
                                                                                                                     final int i,
                                                                                                                     long l)
                                                                                          {
                                                                                              onDeviceSelected(
                                                                                                      client,
                                                                                                      devices[i]);
                                                                                              selectedDeviceName = devices[i]
                                                                                                      .getFriendlyName();
                                                                                          }

                                                                                          @Override
                                                                                          public void onNothingSelected(AdapterView<?> adapterView)
                                                                                          {
                                                                                              viewHolder.submitButton
                                                                                                      .setEnabled(
                                                                                                              false);
                                                                                              selectedDeviceName = "";
                                                                                          }
                                                                                      });
                                                                  }
                                                              });
                                                          }

                                                          @Override
                                                          public void onFailure()
                                                          {
                                                              runOnUiThread(new Runnable()
                                                              {
                                                                  @Override
                                                                  public void run()
                                                                  {
                                                                      showWarning();
                                                                  }
                                                              });
                                                          }
                                                      });
                    }
                    catch (Exception e)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                showWarning();
                            }
                        });
                    }
                }
            });
        }
        catch (InvalidConfigurationException e)
        {
            showWarning();
        }
    }

    private void showWarning()
    {
        viewHolder.warningTextView.setVisibility(View.VISIBLE);
        viewHolder.submitButton.setEnabled(false);
    }

    void onDeviceSelected(final Client client,
                          final Device deviceSelected)
    {
        AsyncTask.execute(new Runnable()
        {
            @Override
            public void run()
            {
                client.getDeviceKeywords(deviceSelected.getId(),
                                         new GetResponseHandler()
                                         {
                                             @Override
                                             public void onSuccess(final Object[] objects)
                                             {
                                                 runOnUiThread(new Runnable()
                                                 {

                                                     @Override
                                                     public void run()
                                                     {
                                                         final Keyword[] keywords = (Keyword[]) objects;
                                                         ArrayAdapter<Keyword> aa = new ArrayAdapter<>(
                                                                 getApplicationContext(),
                                                                 android.R.layout.simple_spinner_dropdown_item,
                                                                 keywords);
                                                         aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                         viewHolder.keywordSelectionSpinner.setAdapter(
                                                                 aa);
                                                         viewHolder.keywordSelectionSpinner.setOnItemSelectedListener(
                                                                 new AdapterView.OnItemSelectedListener()
                                                                 {
                                                                     @Override
                                                                     public void onItemSelected(AdapterView<?> adapterView,
                                                                                                View view,
                                                                                                int i,
                                                                                                long l)
                                                                     {
                                                                         onKeywordSelected(keywords[i]);
                                                                     }

                                                                     @Override
                                                                     public void onNothingSelected(AdapterView<?> adapterView)
                                                                     {
                                                                         selectedKeyword = null;
                                                                         viewHolder.submitButton.setEnabled(
                                                                                 false);
                                                                     }
                                                                 });
                                                     }
                                                 });

                                             }
                                         });
            }
        });
    }

    private void onKeywordSelected(Keyword keyword)
    {
        Log.d("KeywordSelected", "keyword Id=" + keyword.getId() + " " + keyword.getFriendlyName());
        selectedKeyword = keyword;

        // Default value
        viewHolder.labelEditText.setText(selectedDeviceName);
        viewHolder.labelEditText.selectAll();

        viewHolder.submitButton.setEnabled(true);
    }

    protected class ViewHolder
    {
        TextView warningTextView;
        Spinner deviceSelectionSpinner;
        Spinner keywordSelectionSpinner;
        EditText labelEditText;
        Button submitButton;
        Button configureButton;
    }
}

