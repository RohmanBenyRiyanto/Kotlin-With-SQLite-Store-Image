package com.rohmanbeny.registrationapps.utils

import android.provider.BaseColumns

internal class DatabaseRegsiter {
    internal class RegisterColumns : BaseColumns {
        companion object {
            const val TABLE_NAME = "register"
            const val ID  = "id"
            const val NAME = "name"
            const val ALAMAT = "alamat"
            const val PHONE = "phone"
            const val JK = "jk"
            const val LOCATION = "location"
            const val IMAGE = "image"
            const val LONGITUDE = "longitude"
            const val LATITUDE = "latitude"
        }
    }
}