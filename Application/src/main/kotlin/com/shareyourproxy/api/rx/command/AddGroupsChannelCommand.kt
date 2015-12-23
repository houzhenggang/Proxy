package com.shareyourproxy.api.rx.command

import android.app.Service
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxGroupChannelSync
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import java.util.*

/**
 * Edit user group channels.
 * @param user    Logged in user
 * @param groups  selected groups to add channel to
 * @param channel this events group
 */
class AddGroupsChannelCommand(val user: User, val groups: ArrayList<GroupToggle>, val channel: Channel) : BaseCommand() {

    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as User, parcel.readValue(CL) as ArrayList<GroupToggle>, parcel.readValue(CL) as Channel) {
    }

    override fun execute(service: Service): EventCallback {
        return RxGroupChannelSync.addUserGroupsChannel(service, user, groups, channel)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(user)
        dest.writeValue(groups)
        dest.writeValue(channel)
    }

    companion object {
        private val CL = AddGroupsChannelCommand::class.java.classLoader
        val CREATOR: Parcelable.Creator<AddGroupsChannelCommand> = object : Parcelable.Creator<AddGroupsChannelCommand> {
            override fun createFromParcel(parcel: Parcel): AddGroupsChannelCommand {
                return AddGroupsChannelCommand(parcel)
            }

            override fun newArray(size: Int): Array<AddGroupsChannelCommand?> {
                return arrayOfNulls(size)
            }
        }
    }
}