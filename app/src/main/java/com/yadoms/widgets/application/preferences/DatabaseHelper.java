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

public class DatabaseHelper {
    private final Dao<OrmLiteHelper.WidgetDto, Integer> widgetDao;

    public DatabaseHelper(Context context) throws SQLException {
        OrmLiteHelper ormLiteHelper = new OrmLiteHelper(context);
        widgetDao = ormLiteHelper.getWidgetDao();
    }

    public void saveWidget(Widget widget) throws SQLException {
        widgetDao.createOrUpdate(new OrmLiteHelper.WidgetDto(widget));
    }

    public List<Widget> getAllWidgets() {
        try {
            List<Widget> widgetList = new ArrayList<>();
            for (OrmLiteHelper.WidgetDto widgetDto : widgetDao.queryForAll()) {
                widgetList.add(widgetDto.toWidget());
            }
            return widgetList;
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public int[] getAllWidgetIds() {
        List<Widget> widgets = getAllWidgets();
        int[] appWidgetIds = new int[widgets.size()];
        int i = 0;
        for (Widget widget : widgets) {
            appWidgetIds[i++] = widget.id;
        }
        return appWidgetIds;
    }

    public Widget getWidget(int widgetId) throws InvalidConfigurationException {
        try {
            return widgetDao.queryForId(widgetId).toWidget();
        } catch (Exception e) {
            throw new InvalidConfigurationException("Widget not found in database");
        }
    }


    public void deleteWidget(int appWidgetId) throws SQLException {
        widgetDao.deleteById(appWidgetId);
    }

    public Set<Integer> getKeywords(int[] widgetsId) {
        try {
            //TODO nom de colonne en dur dans la requête, à corriger
            return new HashSet<>(widgetDao.queryRaw(
                    "select distinct " + OrmLiteHelper.WidgetDto.FIELD_KEYWORD_ID +
                            " from " + OrmLiteHelper.WidgetDto.TABLE_NAME_WIDGETS +
                            " where " + OrmLiteHelper.WidgetDto.FIELD_ID + " in (" + TextUtils.join(", ", Collections.singleton(widgetsId)) + ")",
                    new RawRowMapper<Integer>() {
                        @Override
                        public Integer mapRow(String[] columnNames, String[] resultColumns) {
                            return Integer.parseInt(resultColumns[0]);
                        }
                    }).getResults());
        } catch (SQLException e) {
            return Collections.emptySet();
        }
    }


    public Set<Integer> getAllKeywords() {
        try {
            //TODO nom de colonne en dur dans la requête, à corriger
            return new HashSet<>(widgetDao.queryRaw(
                    "select distinct " + OrmLiteHelper.WidgetDto.FIELD_KEYWORD_ID + " from " + OrmLiteHelper.WidgetDto.TABLE_NAME_WIDGETS,
                    new RawRowMapper<Integer>() {
                        @Override
                        public Integer mapRow(String[] columnNames, String[] resultColumns) {
                            return Integer.parseInt(resultColumns[0]);
                        }
                    }).getResults());
        } catch (SQLException e) {
            return Collections.emptySet();
        }
    }

    public Set<Widget> getWidgetsFromKeyword(int keywordId) {
        try {
            List<OrmLiteHelper.WidgetDto> widgets = widgetDao.queryForEq(OrmLiteHelper.WidgetDto.FIELD_KEYWORD_ID, keywordId);
            HashSet<Widget> set = new HashSet<>();
            for (OrmLiteHelper.WidgetDto widget : widgets) {
                set.add(widget.toWidget());
            }
            return set;
        } catch (SQLException e) {
            return Collections.emptySet();
        }
    }
}
