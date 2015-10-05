package com.shareyourproxy.app.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.AddUserChannelCommand;

import java.util.UUID;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

import static com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance;
import static com.shareyourproxy.util.ObjectUtils.getSimpleName;
import static com.shareyourproxy.util.ViewUtils.hideSoftwareKeyboard;

/**
 * Add a new Reddit {@link Channel} to a {@link User}.
 */
public class AddRedditChannelDialog extends BaseDialogFragment {


    private static final String ARG_CHANNEL_TYPE = "AddRedditChannelDialog.ChannelType";
    private static final String TAG = getSimpleName(AddRedditChannelDialog.class);
    @Bind(R.id.dialog_reddit_channel_action_address_edittext)
    protected EditText editTextActionAddress;
    private final DialogInterface.OnClickListener _negativeClicked =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideSoftwareKeyboard(editTextActionAddress);
                dismiss();
            }
        };
    @Bind(R.id.dialog_reddit_channel_label_edittext)
    protected EditText editTextLabel;
    @Bind(R.id.dialog_reddit_channel_label_floatlabel)
    protected TextInputLayout floatLabelChannelLabel;
    @Bind(R.id.dialog_reddit_channel_action_address_floatlabel)
    protected TextInputLayout floatLabelAddress;
    @Bind(R.id.dialog_reddit_channel_linktype_header)
    protected TextView linkTypeHeader;
    @Bind(R.id.dialog_reddit_channel_radiobutton_profile)
    protected RadioButton linkTypeProfile;
    @Bind(R.id.dialog_reddit_channel_radiobutton_subreddit)
    protected RadioButton linkTypeSub;
    @BindColor(R.color.common_text)
    protected int _text;
    @BindColor(R.color.common_blue)
    protected int _blue;
    @BindString(R.string.required)
    protected String _required;
    private ChannelType _channelType;
    /**
     * EditorActionListener that detects when the software keyboard's done or enter button is
     * pressed.
     */
    private final TextView.OnEditorActionListener _onEditorActionListener =
        new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // KeyEvent.KEYCODE_ENDCALL is the actionID of the Done button when this
                // FixedDecimalEditText's inputType is Decimal
                if (actionId == KeyEvent.KEYCODE_ENTER
                    || actionId == KeyEvent.KEYCODE_ENDCALL) {
                    saveChannelAndExit();
                    return true;
                }
                return false;
            }
        };
    private final View.OnClickListener _positiveClicked =
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChannelAndExit();
            }
        };
    private String _dialogTitle;
    private String _channelAddressHint;
    private String _channelLabelHint;

    /**
     * Constructor.
     */
    public AddRedditChannelDialog(){
    }
    /**
     * Create a new instance of a {@link AddRedditChannelDialog}.
     *
     * @return A {@link AddRedditChannelDialog}
     */
    public static AddRedditChannelDialog newInstance(ChannelType channelType) {
        //Bundle arguments
        Bundle bundle = new Bundle();
        bundle.putString(ARG_CHANNEL_TYPE, channelType.getLabel());
        //create dialog instance
        AddRedditChannelDialog dialog = new AddRedditChannelDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @OnCheckedChanged(R.id.dialog_reddit_channel_radiobutton_subreddit)
    protected void onSubRedditChecked(boolean checked) {
        if (checked) {
            _channelAddressHint = getString(R.string
                .dialog_addchannel_hint_address_reddit_subreddit);
            floatLabelAddress.setHint(_channelAddressHint);

        }
    }

    @OnCheckedChanged(R.id.dialog_reddit_channel_radiobutton_profile)
    protected void onProfileChecked(boolean checked) {
        if (checked) {
            _channelLabelHint = getString(R.string
                .dialog_addchannel_hint_address_reddit_username);
            floatLabelAddress.setHint(_channelLabelHint);
        }
    }

    public void saveChannelAndExit() {
        boolean addressHasText = editTextActionAddress.getText().toString().trim().length() > 0;
        if (!addressHasText) {
            floatLabelAddress.setError(_required);
        } else {
            floatLabelAddress.setErrorEnabled(false);
            addUserChannel();
            dismiss();
        }
    }

    /**
     * Dispatch a Channel Added Event
     */
    private void addUserChannel() {
        String actionContent = getActionAddress();
        String labelContent = editTextLabel.getText().toString().trim();
        if (!TextUtils.isEmpty(actionContent.trim())) {
            String id = UUID.randomUUID().toString();
            Channel channel =
                createModelInstance(id, labelContent, _channelType, actionContent);
            getRxBus().post(new AddUserChannelCommand(getRxBus(), getLoggedInUser(), channel));
        }
    }

    private String getActionAddress() {
        String action = editTextActionAddress.getText().toString();
        if (linkTypeProfile.isChecked()) {
            return getString(R.string.reddit_linktype_profile, action);
        } else if (linkTypeSub.isChecked()) {
            return getString(R.string.reddit_linktype_subreddit, action);
        } else {
            return action;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _channelType = ChannelType.valueOfLabel(getArguments().getString(ARG_CHANNEL_TYPE));
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_add_reddit_channel, null, false);
        ButterKnife.bind(this, view);
        initializeDisplayValues();

        editTextActionAddress.setOnEditorActionListener(_onEditorActionListener);
        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
            R.style.Base_Theme_AppCompat_Light_Dialog)
            .setTitle(_dialogTitle)
            .setView(view)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(android.R.string.cancel, _negativeClicked)
            .create();

        dialog.setCanceledOnTouchOutside(false);
        // Show the SW Keyboard on dialog start. Always.
        dialog.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;

        // Setup Button Colors
        initializeEditTextColors();
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), _blue);
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), _text);
        //Alert Dialogs dismiss by default because of an internal handler... this bypasses that.
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(_positiveClicked);
    }

    private void initializeDisplayValues() {
        String name = _channelType.getLabel();
        _dialogTitle = getString(R.string.dialog_addchannel_title_add_blank, name);
        _channelAddressHint = getString(R.string.dialog_addchannel_hint_address_reddit_username);
        _channelLabelHint = getString(R.string.dialog_addchannel_hint_label_blank_label);
    }

    /**
     * Initialize color and hints for edit text.
     */
    private void initializeEditTextColors() {
        floatLabelAddress.setHint(_channelAddressHint);
        floatLabelChannelLabel.setHint(_channelLabelHint);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    /**
     * Use the private string TAG from this class as an identifier.
     *
     * @param fragmentManager manager of fragments
     * @return this dialog
     */
    public AddRedditChannelDialog show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
        return this;
    }
}



