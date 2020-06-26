package com.yadoms.widgets.application.preferences;

import android.content.Context;
import android.text.TextUtils;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RawRowMapper;
import com.yadoms.widgets.application.InvalidConfigurationException;
import com.yadoms.widgets.application.preferences.ormLiteImplementation.OrmLiteHelper;
import com.yadoms.widgets.shared.Widget;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseHelper
{
    private static Dao<OrmLiteHelper.WidgetDto, Integer> getWidgetDao(Context context) throws
                                                                                       SQLException
    {
        OrmLiteHelper ormLiteHelper = new OrmLiteHelper(context);
        return ormLiteHelper.getWidgetDao();
    }

    private static Dao<OrmLiteHelper.WidgetDto, Integer> getWidgetDaoNoThrow(Context context)
    {
        try
        {
            return getWidgetDao(context);
        }
        catch (SQLException e)
        {
            return null;
        }
    }

    static public void saveWidget(Context context,
                                  Widget widget) throws SQLException
    {
        Dao<OrmLiteHelper.WidgetDto, Integer> widgetDao = getWidgetDao(context);
        widgetDao.createOrUpdate(new OrmLiteHelper.WidgetDto(widget));
    }

    static public List<Widget> getAllWidgets(Context context)
    {
        final Dao<OrmLiteHelper.WidgetDto, Integer> widgetDao = getWidgetDaoNoThrow(context);
        if (widgetDao == null)
        {
            return Collections.emptyList();
        }

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

    static public int[] getAllWidgetIds(Context context)
    {
        List<Widget> widgets = getAllWidgets(context);
        int[] appWidgetIds = new int[widgets.size()];
        int i = 0;
        for (Widget widget : widgets)
        {
            appWidgetIds[i++] = widget.id;
        }
        return appWidgetIds;
    }

    static public Widget getWidget(Context context,
                                   int widgetId) throws InvalidConfigurationException
    {
        try
        {
            return getWidgetDao(context).queryForId(widgetId).toWidget();
        }
        catch (Exception e)
        {
            throw new InvalidConfigurationException("Widget not found in database");
        }
    }


    static public void deleteWidget(Context context,
                                    int appWidgetId) throws SQLException
    {
        getWidgetDao(context).deleteById(appWidgetId);
    }

    static public Set<Integer> getKeywords(Context context,
                                           int[] widgetsId)
    {
        try
        {
            Dao<OrmLiteHelper.WidgetDto, Integer> widgetDao = getWidgetDaoNoThrow(context);
            if (widgetDao == null)
            {
                return Collections.emptySet();
            }

            //TODO nom de colonne en dur dans la requête, à corriger
            return new HashSet<>(widgetDao.queryRaw(
                    "select distinct " + OrmLiteHelper.WidgetDto.FIELD_KEYWORD_ID +
                    " from " + OrmLiteHelper.WidgetDto.TABLE_NAME_WIDGETS +
                    " where " + OrmLiteHelper.WidgetDto.FIELD_ID + " in (" + TextUtils.join(", ",
                                                                                            Collections
                                                                                                    .singleton(
                                                                                                            widgetsId)) + ")",
                    new RawRowMapper<Integer>()
                    {
                        @Override
                        public Integer mapRow(String[] columnNames,
                                              String[] resultColumns)
                        {
                            return Integer.parseInt(resultColumns[0]);
                        }
                    }).getResults());
        }
        catch (SQLException e)
        {
            return Collections.emptySet();
        }
    }


    static public Set<Integer> getAllKeywords(Context context)
    {
        try
        {
            Dao<OrmLiteHelper.WidgetDto, Integer> widgetDao = getWidgetDaoNoThrow(context);
            if (widgetDao == null)
            {
                return Collections.emptySet();
            }

            //TODO nom de colonne en dur dans la requête, à corriger
            return new HashSet<>(widgetDao.queryRaw(
                    "select distinct " + OrmLiteHelper.WidgetDto.FIELD_KEYWORD_ID + " from " + OrmLiteHelper.WidgetDto.TABLE_NAME_WIDGETS,
                    new RawRowMapper<Integer>()
                    {
                        @Override
                        public Integer mapRow(String[] columnNames,
                                              String[] resultColumns)
                        {
                            return Integer.parseInt(resultColumns[0]);
                        }
                    }).getResults());
        }
        catch (SQLException e)
        {
            return Collections.emptySet();
        }
    }

    static public Set<Widget> getWidgetsFromKeyword(Context context, int keywordId)
    {
        try
        {
            Dao<OrmLiteHelper.WidgetDto, Integer> widgetDao = getWidgetDaoNoThrow(context);
            if (widgetDao == null)
            {
                return Collections.emptySet();
            }

            List<OrmLiteHelper.WidgetDto> widgets = widgetDao.queryForEq(OrmLiteHelper.WidgetDto.FIELD_KEYWORD_ID,
                                                                         keywordId);
            HashSet<Widget> set = new HashSet<>();
            for (OrmLiteHelper.WidgetDto widget : widgets)
            {
                set.add(widget.toWidget());
            }
            return set;
        }
        catch (SQLException e)
        {
            return Collections.emptySet();
        }
    }
}
