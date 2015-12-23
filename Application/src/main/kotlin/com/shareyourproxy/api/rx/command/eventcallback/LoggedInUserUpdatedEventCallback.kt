package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcel
import android.os.Parcelable

import com.shareyourproxy.api.domain.model.User

/**
 * Created by Evan on 6/9/15.
 */
class LoggedInUserUpdatedEventCallback(user: User) : UserEventCallback(user) {
    companion object {
        private val CL = LoggedInUserUpdatedEventCallback::class.java.classLoader
        val CREATOR: Parcelable.Creator<LoggedInUserUpdatedEventCallback> = object : Parcelable.Creator<LoggedInUserUpdatedEventCallback> {
            override fun createFromParcel(parcel: Parcel): LoggedInUserUpdatedEventCallback {
                return LoggedInUserUpdatedEventCallback(parcel.readValue(CL) as User)
            }

            override fun newArray(size: Int): Array<LoggedInUserUpdatedEventCallback?> {
                return arrayOfNulls(size)
            }
        }
    }
}