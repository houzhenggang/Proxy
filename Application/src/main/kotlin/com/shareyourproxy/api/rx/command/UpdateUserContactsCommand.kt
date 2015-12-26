package com.shareyourproxy.api.rx.command

import android.app.Service
import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxUserContactSync.checkContacts
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import java.util.*

/**
 * Update a user's contacts.
 */
class UpdateUserContactsCommand(val user: User, val contacts: ArrayList<String>, val userGroups: HashMap<String, Group>) : BaseCommand() {
    @Suppress("UNCHECKED_CAST")
    private constructor(parcel: Parcel) : this(parcel.readValue(CL) as User, parcel.readValue(CL) as ArrayList<String>, parcel.readValue(CL) as HashMap<String, Group>)

    override fun execute(service: Service): EventCallback {
        return checkContacts(service, user, contacts, userGroups)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(user)
        dest.writeValue(contacts)
        dest.writeValue(userGroups)
    }

    companion object {
        private val CL = UpdateUserContactsCommand::class.java.classLoader
        val CREATOR: Parcelable.Creator<UpdateUserContactsCommand> = object : Parcelable.Creator<UpdateUserContactsCommand> {
            override fun createFromParcel(parcel: Parcel): UpdateUserContactsCommand {
                return UpdateUserContactsCommand(parcel)
            }

            override fun newArray(size: Int): Array<UpdateUserContactsCommand?> {
                return arrayOfNulls(size)
            }
        }
    }
}
