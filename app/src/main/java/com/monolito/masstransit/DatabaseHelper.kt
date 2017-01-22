package com.monolito.masstransit

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import com.monolito.masstransit.domain.Calendar
import com.monolito.masstransit.domain.Route
import com.monolito.masstransit.domain.Schedule
import com.monolito.masstransit.domain.Shape
import com.monolito.masstransit.domain.Stop

import java.sql.SQLException


class DatabaseHelper(context: Context) :
        OrmLiteSqliteOpenHelper(context,
                DatabaseHelper.DATABASE_NAME,
                null,
                DatabaseHelper.DATABASE_VERSION) {

    val stopDao: Dao<Stop, Int> = getDao<Dao<Stop, Int>, Stop>(Stop::class.java)
    val routeDao: Dao<Route, Int> = getDao<Dao<Route, Int>, Route>(Route::class.java)
    val calendarDao: Dao<Calendar, Int> =
        getDao<Dao<Calendar, Int>, Calendar>(Calendar::class.java)
    val shapeDao: Dao<Shape, Int> =
        getDao<Dao<Shape, Int>, Shape>(Shape::class.java)
    val scheduleDao: Dao<Schedule, Int> =
            getDao<Dao<Schedule, Int>, Schedule>(Schedule::class.java)

    override fun onCreate(sqliteDatabase: SQLiteDatabase, connectionSource: ConnectionSource) {
        try {
            TableUtils.createTable(connectionSource, Stop::class.java)
            TableUtils.createTable(connectionSource, Calendar::class.java)
            TableUtils.createTable(connectionSource, Shape::class.java)
            TableUtils.createTable(connectionSource, Route::class.java)
            TableUtils.createTable(connectionSource, Schedule::class.java)
        } catch (e: SQLException) {
            Log.e(DatabaseHelper::class.java.name, "Could not create a database", e)
        }

    }

    override fun onUpgrade(sqliteDatabase: SQLiteDatabase,
                           connectionSource: ConnectionSource,
                           oldVer: Int,
                           newVer: Int) {
        // handles database upgrade
        try {
            TableUtils.dropTable<Stop, Any>(connectionSource, Stop::class.java, true)
            TableUtils.dropTable<Calendar, Any>(connectionSource, Calendar::class.java, true)
            TableUtils.dropTable<Shape, Any>(connectionSource, Shape::class.java, true)
            TableUtils.dropTable<Route, Any>(connectionSource, Route::class.java, true)
            TableUtils.dropTable<Schedule, Any>(connectionSource, Schedule::class.java, true)
            onCreate(sqliteDatabase, connectionSource)
        } catch (e: SQLException) {
            Log.e("DbHelper", e.message, e)
        }

    }

    companion object {
        private val DATABASE_NAME = "masstransit.db"
        private val DATABASE_VERSION = 6
    }
}