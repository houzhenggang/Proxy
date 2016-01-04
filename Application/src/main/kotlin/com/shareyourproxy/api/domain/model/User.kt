package com.shareyourproxy.api.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.util.BaseParcelable
import java.util.*

/**
 * Users have a basic profile that contains their specific [Channel]s, [User]s, and [Group]s.
 */
internal data class User(val id: String, val first: String, val last: String, val fullName: String, val email: String, val profileURL: String, val coverURL: String, val channels: HashMap<String, Channel>, val contacts: HashSet<String>, val groups: HashMap<String, Group>, val androidVersion: Int) : BaseParcelable {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(first)
        dest.writeString(last)
        dest.writeString(fullName)
        dest.writeString(email)
        dest.writeString(profileURL)
        dest.writeString(coverURL)
        dest.writeSerializable(channels)
        dest.writeSerializable(contacts)
        dest.writeSerializable(groups)
        dest.writeInt(androidVersion)
    }

    constructor() : this("", "", "", "", "", "", "", HashMap(), HashSet(), HashMap(), 0)
    companion object {
        val CREATOR = object : Parcelable.Creator<User> {
            override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
            override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
        }

        @Suppress("UNCHECKED_CAST")
        private fun readParcel(parcel: Parcel) = User(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readSerializable() as HashMap<String, Channel>, parcel.readSerializable() as HashSet<String>, parcel.readSerializable() as HashMap<String, Group>, parcel.readInt())
    }
}
