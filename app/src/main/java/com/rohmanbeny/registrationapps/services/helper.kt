package com.rohmanbeny.registrationapps.services

import android.database.Cursor
import com.rohmanbeny.registrationapps.model.RegisterModel
import com.rohmanbeny.registrationapps.utils.DatabaseRegsiter

object helper {

    const val EXTRA_REGISTRATION = "extra_registration"
    const val EXTRA_POSITION = "extra_position"
    const val REQUEST_ADD = 100
    const val RESULT_ADD = 101
    const val REQUEST_UPDATE = 200
    const val REQUEST_LOCATION = 300
    const val RESULT_UPDATE = 201
    const val RESULT_DELETE = 301
    const val ALERT_DIALOG_CLOSE = 10
    const val ALERT_DIALOG_DELETE = 20

    fun mapCursorToArrayList(cursor: Cursor?): ArrayList<RegisterModel> {
        val registerList = ArrayList<RegisterModel>()
        cursor?.apply {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseRegsiter.RegisterColumns.ID))
                val name = getString(getColumnIndexOrThrow(DatabaseRegsiter.RegisterColumns.NAME))
                val alamat = getString(getColumnIndexOrThrow(DatabaseRegsiter.RegisterColumns.ALAMAT))
                val phone = getString(getColumnIndexOrThrow(DatabaseRegsiter.RegisterColumns.PHONE))
                val jk = getString(getColumnIndexOrThrow(DatabaseRegsiter.RegisterColumns.JK))
                val location = getString(getColumnIndexOrThrow(DatabaseRegsiter.RegisterColumns.LOCATION))
                val latitude = getString(getColumnIndexOrThrow(DatabaseRegsiter.RegisterColumns.LATITUDE))
                val longitude = getString(getColumnIndexOrThrow(DatabaseRegsiter.RegisterColumns.LONGITUDE))
                val image = getBlob(getColumnIndexOrThrow(DatabaseRegsiter.RegisterColumns.IMAGE))
                registerList.add(RegisterModel(id, name, alamat, phone, jk, location, image))
            }
        }
        return registerList
    }
}