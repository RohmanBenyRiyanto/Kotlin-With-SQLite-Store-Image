package com.rohmanbeny.registrationapps.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.rohmanbeny.registrationapps.utils.DatabaseRegsiter.RegisterColumns.Companion.TABLE_NAME

internal class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME,
    null, DATABASE_VERSION,
) {
    companion object {
        private const val DATABASE_NAME = "registerapp"
        private const val DATABASE_VERSION = 5
        private const val SQL_CREATE_TABLE_REGISTER = "CREATE TABLE $TABLE_NAME" +
                " (${DatabaseRegsiter.RegisterColumns.ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                " ${DatabaseRegsiter.RegisterColumns.NAME} TEXT NOT NULL," +
                " ${DatabaseRegsiter.RegisterColumns.ALAMAT} TEXT NOT NULL," +
                " ${DatabaseRegsiter.RegisterColumns.PHONE} TEXT NOT NULL," +
                " ${DatabaseRegsiter.RegisterColumns.JK} TEXT NOT NULL," +
                " ${DatabaseRegsiter.RegisterColumns.LOCATION} TEXT NOT NULL," +
                " ${DatabaseRegsiter.RegisterColumns.LONGITUDE} TEXT NOT NULL," +
                " ${DatabaseRegsiter.RegisterColumns.LATITUDE} TEXT NOT NULL," +
                " ${DatabaseRegsiter.RegisterColumns.IMAGE} BLOB NOT NULL)"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            db.execSQL(SQL_CREATE_TABLE_REGISTER)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (db != null) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        } else{
            onCreate(db)
        }
    }
}