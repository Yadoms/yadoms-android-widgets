package com.yadoms.yadroid.statedisplay.preferences;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RawRowMapper;
import com.yadoms.yadroid.statedisplay.InvalidConfigurationException;
import com.yadoms.yadroid.statedisplay.Widget;
import com.yadoms.yadroid.statedisplay.preferences.ormLiteImplementation.OrmLiteHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseHelper
{
    private final Dao<OrmLiteHelper.WidgetDto, Integer> widgetDao;

    public DatabaseHelper(Context context) throws SQLException
    {
        OrmLiteHelper ormLiteHelper = new OrmLiteHelper(context);
        widgetDao = ormLiteHelper.getWidgetDao();
    }

    public void saveWidget(Widget widget) throws SQLException
    {
        widgetDao.createOrUpdate(new OrmLiteHelper.WidgetDto(widget));
    }

    public List<Widget> getAllWidgets()
    {
        try
        {
            List<Widget> widgetList = new ArrayList<>();
            for (OrmLiteHelper.WidgetDto widgetDto : widgetDao.queryForAll())
            {
                widgetList.add(widgetDto.toWidget());
            }
            return widgetList;
        }
        catch (SQLException e)
        {
            return Collections.emptyList();
        }
    }

    public Widget getWidget(int widgetId) throws InvalidConfigurationException
    {
        try
        {
            return widgetDao.queryForId(widgetId).toWidget();
        }
        catch (Exception e)
        {
            throw new InvalidConfigurationException("Widget not found in database");
        }
    }


    public void deleteWidget(int appWidgetId) throws SQLException
    {
        widgetDao.deleteById(appWidgetId);
    }

    public Set<Integer> getAllKeywords()
    {
        try
        {
            //TODO nom de colonne en dur dans la requête, à corriger
            return new HashSet<>(widgetDao.queryRaw(
                    "select distinct keywordId from " + OrmLiteHelper.WidgetDto.TABLE_NAME_WIDGETS,
                    new RawRowMapper<Integer>() {
                        @Override
                        public Integer mapRow(String[] columnNames, String[] resultColumns) {
                            return Integer.parseInt(resultColumns[0]);
                        }
                    }).getResults());
        }
        catch (SQLException e)
        {
            return Collections.emptySet();
        }
    }

    public Set<Integer> getWidgetsFromKeyword(int keywordId)
    {
        try
        {
            //TODO nom de colonne en dur dans la requête, à corriger
            return new HashSet<>(widgetDao.queryRaw(
                    "select distinct widgetId from " + OrmLiteHelper.WidgetDto.TABLE_NAME_WIDGETS + " where keywordId=" + Integer.toString(keywordId),
                    new RawRowMapper<Integer>() {
                        @Override
                        public Integer mapRow(String[] columnNames, String[] resultColumns) {
                            return Integer.parseInt(resultColumns[0]);
                        }
                    }).getResults());
        }
        catch (SQLException e)
        {
            return Collections.emptySet();
        }
    }
}
