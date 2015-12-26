package com.shareyourproxy.app.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.design.widget.Snackbar.make
import android.support.v4.content.ContextCompat.getColor
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.graphics.Palette
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.bindView
import com.facebook.drawee.backends.pipeline.Fresco.newDraweeControllerBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.BasePostprocessor
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.shareyourproxy.Constants
import com.shareyourproxy.Constants.ARG_USER_SELECTED_PROFILE
import com.shareyourproxy.IntentLauncher.launchAddressIntent
import com.shareyourproxy.IntentLauncher.launchElloIntent
import com.shareyourproxy.IntentLauncher.launchEmailIntent
import com.shareyourproxy.IntentLauncher.launchFBMessengerIntent
import com.shareyourproxy.IntentLauncher.launchFacebookIntent
import com.shareyourproxy.IntentLauncher.launchGithubIntent
import com.shareyourproxy.IntentLauncher.launchGooglePlusIntent
import com.shareyourproxy.IntentLauncher.launchHangoutsIntent
import com.shareyourproxy.IntentLauncher.launchInstagramIntent
import com.shareyourproxy.IntentLauncher.launchLinkedInIntent
import com.shareyourproxy.IntentLauncher.launchMediumIntent
import com.shareyourproxy.IntentLauncher.launchMeerkatIntent
import com.shareyourproxy.IntentLauncher.launchNintendoNetworkIntent
import com.shareyourproxy.IntentLauncher.launchPhoneIntent
import com.shareyourproxy.IntentLauncher.launchPlaystationNetworkIntent
import com.shareyourproxy.IntentLauncher.launchRedditIntent
import com.shareyourproxy.IntentLauncher.launchSMSIntent
import com.shareyourproxy.IntentLauncher.launchSkypeIntent
import com.shareyourproxy.IntentLauncher.launchSnapChatIntent
import com.shareyourproxy.IntentLauncher.launchSoundCloudIntent
import com.shareyourproxy.IntentLauncher.launchSpotifyIntent
import com.shareyourproxy.IntentLauncher.launchSteamIntent
import com.shareyourproxy.IntentLauncher.launchTumblrIntent
import com.shareyourproxy.IntentLauncher.launchTwitchIntent
import com.shareyourproxy.IntentLauncher.launchTwitterIntent
import com.shareyourproxy.IntentLauncher.launchVenmoIntent
import com.shareyourproxy.IntentLauncher.launchWebIntent
import com.shareyourproxy.IntentLauncher.launchWhatsAppIntent
import com.shareyourproxy.IntentLauncher.launchXboxLiveIntent
import com.shareyourproxy.IntentLauncher.launchYoIntent
import com.shareyourproxy.IntentLauncher.launchYoutubeIntent
import com.shareyourproxy.R
import com.shareyourproxy.api.RestClient.userService
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusDriver.post
import com.shareyourproxy.api.rx.RxGoogleAnalytics
import com.shareyourproxy.api.rx.RxQuery.getUserContactScore
import com.shareyourproxy.api.rx.command.AddUserChannelCommand
import com.shareyourproxy.api.rx.command.SyncContactsCommand
import com.shareyourproxy.api.rx.command.eventcallback.GroupContactsUpdatedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelDeletedEventCallback
import com.shareyourproxy.api.rx.event.SelectUserChannelEvent
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent
import com.shareyourproxy.util.ViewUtils.getAlphaOverlayHierarchy
import com.shareyourproxy.util.ViewUtils.getUserImageHierarchy
import com.shareyourproxy.util.ViewUtils.getUserImageHierarchyNoFade
import rx.subscriptions.CompositeSubscription
import timber.log.Timber

/**
 * Display a User or a User Contact's Channels. Allow Users to edit their channels. Allow User Contact's to be added to be observed and added to groups logged
 * in user groups.
 */
abstract class UserProfileFragment : BaseFragment() {
    private val appBarLayout: AppBarLayout by bindView(R.id.fragment_user_profile_appbar)
    private val swipeRefreshLayout: SwipeRefreshLayout by bindView(R.id.fragment_user_profile_swiperefresh)
    private val coordinatorLayout: CoordinatorLayout by bindView(R.id.fragment_user_profile_coordinator_layout)
    private val collapsingToolbarLayout: CollapsingToolbarLayout by bindView(R.id.fragment_user_profile_collapsing_toolbar)
    private val userImage: SimpleDraweeView by bindView(R.id.fragment_user_profile_header_image)
    private val userBackground: SimpleDraweeView by bindView(R.id.fragment_user_profile_header_background)
    private val followersTextView: TextView by bindView(R.id.fragment_user_profile_header_followers)


    internal val colorBlue: Int = getColor(context, R.color.common_blue)
    internal var svgLarge: Int = resources.getDimensionPixelSize(R.dimen.common_svg_large)
    internal var stringCalculating: String = getString(R.string.calculating)
    internal var stringErrorCalculating: String = getString(R.string.error_calculating)
    private var _subscriptions: CompositeSubscription = CompositeSubscription()
    private var deletedChannel: Channel? = null
    internal var contact: User =arguments.getParcelable<User>(ARG_USER_SELECTED_PROFILE)
        private set
    private var userChannelsFragment: UserChannelsFragment? = null
    internal var _refreshListener: SwipeRefreshLayout.OnRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        post(SyncContactsCommand(loggedInUser))
        getUserContactScore(contact.id).subscribe(contactScoreObserver)
    }
    private val analytics : RxGoogleAnalytics = RxGoogleAnalytics(activity)
    private val loggedInUserId = arguments.getString(Constants.ARG_LOGGEDIN_USER_ID)


    /**
     * Get a click listener to add a deleted channel.

     * @return click listener
     */
    private val addChannelClickListener: View.OnClickListener
        get() = View.OnClickListener { rxBus.post(AddUserChannelCommand(loggedInUser, deletedChannel!!)) }

    private val offsetListener: AppBarLayout.OnOffsetChangedListener
        get() = AppBarLayout.OnOffsetChangedListener { appBarLayout, offset -> swipeRefreshLayout.isEnabled = offset == 0 }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        checkLoggedInUserValue(loggedInUserId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_user_profile, container, false)
        onCreateView(rootView)
        return rootView
    }

    internal open fun onCreateView(rootView: View) {
        followersTextView.text = getString(R.string.user_profile_followers, stringCalculating)
        appBarLayout.addOnOffsetChangedListener(offsetListener)
        initializeSwipeRefresh(swipeRefreshLayout, _refreshListener)
        initializeUserChannels()
        //followers score
        getUserContactScore(contact.id).subscribe(contactScoreObserver)
    }

    override fun onResume() {
        super.onResume()
        _subscriptions.add(rxBus.toObservable().subscribe(onNextEvent()))
    }

    override fun onPause() {
        super.onPause()
        _subscriptions.unsubscribe()
        //if we're refreshing data, get rid of the UI
        swipeRefreshLayout.isRefreshing = false
    }

    private fun initializeUserChannels() {
        userChannelsFragment = UserChannelsFragment.newInstance(contact)
        childFragmentManager.beginTransaction().replace(R.id.fragment_user_profile_user_channels, userChannelsFragment).commit()
    }

    /**
     * If we entered this activity fragment through a notification, make sure the logged in user has a value.
     * @param loggedInUserId logged in user id.
     */
    private fun checkLoggedInUserValue(loggedInUserId: String) {
        try {
            //set the shared preferences user if it matches the logged in user id
            if (sharedPrefJsonUser.id.equals(loggedInUserId)) {
                loggedInUser = sharedPrefJsonUser
            } else {
                loggedInUser = userService.getUser(loggedInUserId).toBlocking().single()
            }
        } catch (e: Exception) {
            Timber.e(Log.getStackTraceString(e))
        }
    }

    private fun showDeletedChannelSnackBar(coordinatorLayout: CoordinatorLayout) {
        val snackbar = make(coordinatorLayout, getString(R.string.undo_delete), LENGTH_INDEFINITE)
        snackbar.setAction(getString(R.string.undo), addChannelClickListener)
        snackbar.setActionTextColor(colorBlue)
        snackbar.show()
    }


    private fun onNextEvent(): JustObserver<Any> {
        return object : JustObserver<Any>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(event: Any?) {
                if (event is GroupContactsUpdatedEventCallback) {
                    groupContactsUpdatedEvent(event)
                } else if (event is UserChannelAddedEventCallback) {
                    addUserChannel(event)
                } else if (event is UserChannelDeletedEventCallback) {
                    deleteUserChannel(event)
                } else if (event is SyncContactsCommand) {
                    swipeRefreshLayout.isRefreshing = true
                } else if (event is SyncAllContactsSuccessEvent) {
                    swipeRefreshLayout.isRefreshing = false
                } else if (event is SyncAllContactsErrorEvent) {
                    swipeRefreshLayout.isRefreshing = false
                } else if (event is SelectUserChannelEvent) {
                    onChannelSelected(event)
                }
            }
        }
    }

    private fun groupContactsUpdatedEvent(event: GroupContactsUpdatedEventCallback) {
        if (event.contactGroups.size > 0) {
            analytics.userContactAdded(event.user)
        } else {
            analytics.userContactRemoved(event.user)
        }
        getUserContactScore(contact.id).subscribe(contactScoreObserver)
    }

    private fun deleteUserChannel(event: UserChannelDeletedEventCallback) {
        deletedChannel = event.channel
        showDeletedChannelSnackBar(coordinatorLayout)
    }

    private fun addUserChannel(event: UserChannelAddedEventCallback) {
        if (event.oldChannel != null) {
            showChangesSavedSnackBar(coordinatorLayout)
        }
    }

    /**
     * Handle channel selected events to launch the correct android process.

     * @param event data
     */
    internal fun onChannelSelected(event: SelectUserChannelEvent) {
        val channelType = event.channel.channelType
        val actionAddress = event.channel.actionAddress
        when (channelType) {
            ChannelType.Phone -> launchPhoneIntent(activity, actionAddress)
            ChannelType.SMS -> launchSMSIntent(activity, actionAddress)
            ChannelType.Email -> launchEmailIntent(activity, actionAddress)
            ChannelType.Web, ChannelType.URL -> launchWebIntent(activity, actionAddress)
            ChannelType.Facebook -> launchFacebookIntent(activity, actionAddress)
            ChannelType.Twitter -> launchTwitterIntent(activity, actionAddress)
            ChannelType.Meerkat -> launchMeerkatIntent(activity, actionAddress)
            ChannelType.Snapchat -> launchSnapChatIntent(activity, actionAddress)
            ChannelType.Spotify -> launchSpotifyIntent(activity, actionAddress)
            ChannelType.Reddit -> launchRedditIntent(activity, actionAddress)
            ChannelType.Linkedin -> launchLinkedInIntent(activity, actionAddress)
            ChannelType.FBMessenger -> launchFBMessengerIntent(activity, actionAddress)
            ChannelType.Googleplus -> launchGooglePlusIntent(activity, actionAddress)
            ChannelType.Github -> launchGithubIntent(activity, actionAddress)
            ChannelType.Address -> launchAddressIntent(activity, actionAddress)
            ChannelType.Youtube -> launchYoutubeIntent(activity, actionAddress)
            ChannelType.Instagram -> launchInstagramIntent(activity, actionAddress)
            ChannelType.Tumblr -> launchTumblrIntent(activity, actionAddress)
            ChannelType.Ello -> launchElloIntent(activity, actionAddress)
            ChannelType.Venmo -> launchVenmoIntent(activity, actionAddress)
            ChannelType.Medium -> launchMediumIntent(activity, actionAddress)
            ChannelType.Soundcloud -> launchSoundCloudIntent(activity, actionAddress)
            ChannelType.Skype -> launchSkypeIntent(activity, actionAddress)
            ChannelType.Yo -> launchYoIntent(activity, actionAddress)
            ChannelType.Custom -> {
            }
            ChannelType.Slack -> {
            }
            ChannelType.Hangouts -> launchHangoutsIntent(activity, actionAddress)
            ChannelType.Whatsapp -> launchWhatsAppIntent(activity, actionAddress)
            ChannelType.Periscope -> {
            }
            ChannelType.PlaystationNetwork -> launchPlaystationNetworkIntent(activity, actionAddress)
            ChannelType.NintendoNetwork -> launchNintendoNetworkIntent(activity, actionAddress)
            ChannelType.Steam -> launchSteamIntent(activity, actionAddress)
            ChannelType.Twitch -> launchTwitchIntent(activity, actionAddress)
            ChannelType.XboxLive -> launchXboxLiveIntent(activity, actionAddress)
            ChannelType.LeagueOfLegends -> {
            }
            else -> {
            }
        }

    }

    /**
     * Async returns when palette has been loaded.
     * @return palette listener
     */
    private val paletteAsyncListener: Palette.PaletteAsyncListener
        get() {
            return Palette.PaletteAsyncListener { palette ->
                val offColor = palette.getMutedColor(colorBlue)
                val color = palette.getVibrantColor(offColor)
                collapsingToolbarLayout.setContentScrimColor(color)
                collapsingToolbarLayout.setStatusBarScrimColor(color)
                if (contact.coverURL.isEmpty()) {
                    collapsingToolbarLayout.setBackgroundColor(color)
                }
            }
        }

    /**
     * Initialize the Header view data and state.
     */
    internal fun initializeHeader() {
        //update profile user image
        val profileURL = contact.profileURL
        if (this is MainUserProfileFragment) {
            userImage.hierarchy = getUserImageHierarchy(activity)
        } else {
            userImage.hierarchy = getUserImageHierarchyNoFade(activity)
        }

        val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(profileURL)).setPostprocessor(paletteProcessor).build()
        userImage.controller = newDraweeControllerBuilder().setImageRequest(request).build()


        //update profile background
        val coverURL = contact.coverURL
        userBackground.hierarchy = getAlphaOverlayHierarchy(collapsingToolbarLayout, resources)
        userBackground.controller = newDraweeControllerBuilder().setUri(Uri.parse(coverURL)).setAutoPlayAnimations(true).build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        userChannelsFragment!!.onActivityResult(requestCode, resultCode, data)
    }

    private val paletteProcessor: BasePostprocessor
        get() {
            return object : BasePostprocessor() {
                override fun process(bitmap: Bitmap?) {
                    Palette.Builder(bitmap).generate(paletteAsyncListener)
                }

                override fun getName(): String {
                    return "datPostProcessor"
                }
            }
        }

    private val contactScoreObserver: JustObserver<Int>
        get() = object : JustObserver<Int>() {
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            override fun next(integer: Int?) {
                if (activity != null) {
                    followersTextView.text = getString(R.string.user_profile_followers,
                            integer)
                }
            }

            override fun error(e: Throwable) {
                if (activity != null) {
                    followersTextView.text = getString(R.string.user_profile_followers,
                            stringErrorCalculating)
                }
            }
        }
}
