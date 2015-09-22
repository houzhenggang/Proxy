package com.shareyourproxy.app.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.SaveGroupContactsCommand;
import com.shareyourproxy.app.adapter.BaseRecyclerView;
import com.shareyourproxy.app.adapter.UserGroupsAdapter;
import com.shareyourproxy.util.ObjectUtils;
import com.shareyourproxy.util.ViewUtils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

/**
 * This Dialog provides a toggle selection to add a User contactId to the logged in User's various
 * saved groups.
 */
public class UserGroupsDialog extends BaseDialogFragment {

    private static final String TAG = ObjectUtils.getSimpleName(UserGroupsDialog.class);
    private static final String ARG_GROUPS = "com.shareyourproxy.app.dialog.UserGroupsList";
    private static final String ARG_USER = "com.shareyourproxy.app.dialog.User";
    private final OnClickListener _negativeClicked =
        new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        };
    @Bind(R.id.dialog_user_groups_recyclerview)
    protected BaseRecyclerView recyclerView;
    // Color
    @BindColor(R.color.common_text)
    protected int _textColor;
    @BindColor(R.color.common_blue)
    protected int _blue;
    private UserGroupsAdapter _adapter;
    private final OnClickListener _positiveClicked =
        new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dispatchUpdatedUserGroups();
            }
        };

    /**
     * Constructor
     */
    public UserGroupsDialog() {
    }

    /**
     * Create a new instance of a {@link UserGroupsDialog}.
     *
     * @param groups logged in user groups
     * @param user   this is actually the contactId of the logged in user
     * @return A {@link UserGroupsDialog}
     */
    public static UserGroupsDialog newInstance(
        @NonNull ArrayList<GroupToggle> groups, @NonNull User user) {
        //Bundle arguments
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ARG_GROUPS, groups);
        bundle.putParcelable(ARG_USER, user);
        //copy dialog instance
        UserGroupsDialog dialog = new UserGroupsDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    /**
     * Issue a save group contacts command.
     */
    private void dispatchUpdatedUserGroups() {
        User user = getUserArg();
        getRxBus().post(new SaveGroupContactsCommand(getRxBus(), getLoggedInUser(),
            _adapter.getDataArray(), user.id().value()));
    }

    /**
     * Get the logged in user
     *
     * @return user
     */
    private User getUserArg() {
        return getArguments().getParcelable(ARG_USER);
    }

    /**
     * get the groups bundled into this dialog fragment.
     *
     * @return selected groups
     */
    private ArrayList<GroupToggle> getCheckedGroups() {
        return getArguments().getParcelableArrayList(ARG_GROUPS);
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = getActivity().getLayoutInflater()
            .inflate(R.layout.dialog_user_groups, null, false);
        ButterKnife.bind(this, view);
        String title = getString(R.string.dialog_edit_user_groups) + " " + getUserArg().first();
        AlertDialog dialog = new AlertDialog.Builder(getActivity(),
            R.style.Base_Theme_AppCompat_Light_Dialog)
            .setTitle(title)
            .setView(view)
            .setPositiveButton(R.string.save, _positiveClicked)
            .setNegativeButton(android.R.string.cancel, _negativeClicked)
            .create();
        dialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        setButtonTint(dialog.getButton(Dialog.BUTTON_POSITIVE), _blue);
        setButtonTint(dialog.getButton(Dialog.BUTTON_NEGATIVE), _textColor);
        initializeRecyclerView();
    }

    /**
     * Setup the group list UI.
     */
    private void initializeRecyclerView() {
        _adapter = UserGroupsAdapter.newInstance(getCheckedGroups());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(_adapter);
        recyclerView.hasFixedSize();
        ViewGroup.LayoutParams lp = recyclerView.getLayoutParams();
        lp.height = (int) ViewUtils.dpToPx(getResources(), R.dimen.user_groups_dialog_height);
        recyclerView.setLayoutParams(lp);
    }

    /**
     * Use the private string TAG from this class as an identifier.
     *
     * @param fragmentManager manager of fragments
     * @return this dialog
     */
    public UserGroupsDialog show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
        return this;
    }

}
