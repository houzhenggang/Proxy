package com.shareyourproxy.api.rx.command.eventcallback

import android.app.Notification
import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by Evan on 6/18/15.
 */
class UserMessagesDownloadedEventCallback(val notifications: ArrayList<Notification>) : EventCallback() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(notifications)
    }

    companion object {
        private val CL = UserMessagesDownloadedEventCallback::class.java.classLoader
        val CREATOR: Parcelable.Creator<UserMessagesDownloadedEventCallback> = object : Parcelable.Creator<UserMessagesDownloadedEventCallback> {
            override fun createFromParcel(parcel: Parcel): UserMessagesDownloadedEventCallback {
                return UserMessagesDownloadedEventCallback(parcel.readValue(CL) as ArrayList<Notification>)
            }

            override fun newArray(size: Int): Array<UserMessagesDownloadedEventCallback?> {
                return arrayOfNulls(size)
            }
        }
    }
}
