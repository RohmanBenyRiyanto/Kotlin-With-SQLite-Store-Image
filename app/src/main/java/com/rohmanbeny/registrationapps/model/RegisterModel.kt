package com.rohmanbeny.registrationapps.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RegisterModel(
    var id: Int = 0,
    var name: String? = null,
    var alamat: String? = null,
    var phone: String? = null,
    var jk: String? = null,
    var location: String? = null,
    var image: ByteArray? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegisterModel

        if (id != other.id) return false
        if (name != other.name) return false
        if (alamat != other.alamat) return false
        if (phone != other.phone) return false
        if (jk != other.jk) return false
        if (location != other.location) return false
        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (alamat?.hashCode() ?: 0)
        result = 31 * result + (phone?.hashCode() ?: 0)
        result = 31 * result + (jk?.hashCode() ?: 0)
        result = 31 * result + (location?.hashCode() ?: 0)
        result = 31 * result + (image?.contentHashCode() ?: 0)
        result = 31 * result + (latitude?.hashCode() ?: 0)
        result = 31 * result + (longitude?.hashCode() ?: 0)
        return result
    }

}