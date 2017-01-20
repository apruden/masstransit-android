package com.monolito.masstransit;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.monolito.masstransit.domain.Calendar;
import com.monolito.masstransit.domain.Route;
import com.monolito.masstransit.domain.Schedule;
import com.monolito.masstransit.domain.Shape;
import com.monolito.masstransit.domain.Stop;

import java.sql.SQLException;


public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "masstransit.db";
    private static final int DATABASE_VERSION = 6;

    private Dao<Stop, Integer> stopDao;
    private Dao<Route, Integer> routeDao;
    private Dao<Calendar, Integer> calendarDao;
    private Dao<Shape, Integer> shapeDao;
    private Dao<Schedule, Integer> scheduleDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Stop.class);
            TableUtils.createTable(connectionSource, Calendar.class);
            TableUtils.createTable(connectionSource, Shape.class);
            TableUtils.createTable(connectionSource, Route.class);
            TableUtils.createTable(connectionSource, Schedule.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Could not create a database", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase,
                          ConnectionSource connectionSource,
                          int oldVer,
                          int newVer) {
        // handles database upgrade
        try {
            TableUtils.dropTable(connectionSource, Stop.class, true);
            TableUtils.dropTable(connectionSource, Calendar.class, true);
            TableUtils.dropTable(connectionSource, Shape.class, true);
            TableUtils.dropTable(connectionSource, Route.class, true);
            TableUtils.dropTable(connectionSource, Schedule.class, true);
            onCreate(sqliteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e("DbHelper", e.getMessage(), e);
        }
    }

    public Dao<Stop, Integer> getStopDao() throws SQLException {
        if (stopDao == null) {
            stopDao = getDao(Stop.class);
        }

        return stopDao;
    }

    public Dao<Route, Integer> getRouteDao() throws SQLException {
        if (routeDao == null) {
            routeDao = getDao(Route.class);
        }

        return routeDao;
    }

    public Dao<Calendar, Integer> getCalendarDao() throws SQLException {
        if (calendarDao == null) {
            calendarDao = getDao(Calendar.class);
        }

        return calendarDao;
    }

    public Dao<Shape, Integer> getShapeDao() throws SQLException {
        if (shapeDao == null) {
            shapeDao = getDao(Shape.class);
        }

        return shapeDao;
    }


    public Dao<Schedule, Integer> getScheduleDao() throws SQLException {
        if (scheduleDao == null) {
            scheduleDao = getDao(Schedule.class);
        }

        return scheduleDao;
    }
}