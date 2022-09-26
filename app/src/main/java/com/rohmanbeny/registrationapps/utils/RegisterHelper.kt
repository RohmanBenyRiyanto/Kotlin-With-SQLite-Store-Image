package com.rohmanbeny.registrationapps.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.rohmanbeny.registrationapps.utils.DatabaseRegsiter.RegisterColumns.Companion.ID
import com.rohmanbeny.registrationapps.utils.DatabaseRegsiter.RegisterColumns.Companion.TABLE_NAME

class RegisterHelper(context: Context) {
    companion object {
        private lateinit var databaseHelper: DatabaseHelper
        private var INSTANCE: RegisterHelper? = null
        private lateinit var database: SQLiteDatabase

        fun getInstance(context: Context): RegisterHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RegisterHelper(context)
            }
    }

    init {
        databaseHelper= DatabaseHelper(context)
    }
    @Throws(SQLException::class)
    fun open() {
        database = databaseHelper.writableDatabase
    }

    fun close() {
        databaseHelper.close()
        if (database.isOpen)
            database.close()
    }

    fun queryAll(): Cursor {
        return database.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "$ID ASC"
        )
    }

    fun queryById(id: String): Cursor {
        return database.query(
            TABLE_NAME,
            null,
            "$ID = ?",
            arrayOf(id),
            null,
            null,
            null,
            null
        )
    }

    fun insert(values: ContentValues?): Long {
        return database.insert(TABLE_NAME, null, values)
    }

    fun update(id: String, values: ContentValues?): Int {
        return database.update(TABLE_NAME, values, "$ID = ?", arrayOf(id))
    }


    fun deleteById(id: String): Int {
        return database.delete(TABLE_NAME, "$ID = '$id'", null)
    }
}