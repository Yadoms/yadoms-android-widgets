package com.yadoms.widgets.statedisplay;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * The configuration screen for the {@link SwitchAppWidget SwitchAppWidget} AppWidget.
 */
public class SwitchAppWidgetConfigureActivity
        extends Activity
{
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    ViewHolder viewHolder;

    View.OnClickListener onSubmitButtonClick = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            final Context context = SwitchAppWidgetConfigureActivity.this;

            // Save prefs
            Log.d("onClick", "mAppWidgetId = " + mAppWidgetId);
            widgetPref prefs = new widgetPref(context, mAppWidgetId);
            prefs.keyword = viewHolder.keywordEditText.getText().toString();
            prefs.label = viewHolder.labelEditText.getText().toString();
            prefs.save();

            prefs = new widgetPref(context, mAppWidgetId);
            Log.d("onClick", "prefs.keyword = " + prefs.keyword);
            Log.d("onClick", "prefs.label = " + prefs.label);


            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            SwitchAppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
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
        viewHolder.keywordEditText = findViewById(R.id.keyword);
        viewHolder.labelEditText = findViewById(R.id.label);

        viewHolder.submitButton = findViewById(R.id.button);
        viewHolder.submitButton.setOnClickListener(onSubmitButtonClick);
    }

    protected class ViewHolder
    {
        EditText keywordEditText;
        EditText labelEditText;
        Button submitButton;
    }
}

