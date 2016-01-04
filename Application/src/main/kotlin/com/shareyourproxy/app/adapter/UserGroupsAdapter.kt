package com.shareyourproxy.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.StringUtils.capitalize
import java.util.*

/**
 * Adapts the contactGroups that a user belongs to in a dialog.
 */
internal final class UserGroupsAdapter(recyclerView: BaseRecyclerView, groups: ArrayList<GroupToggle>) : SortedRecyclerAdapter<GroupToggle>(GroupToggle::class.java, recyclerView), ItemClickListener {
    init {
        refreshData(groups)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_user_groups_checklist, parent, false)
        return ContentViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        bindContentView(holder as ContentViewHolder, position)
    }

    private fun bindContentView(holder: ContentViewHolder, position: Int) {
        holder.checkedTextView.text = capitalize(string = getItemData(position).group.label)
        holder.checkedTextView.isChecked = getItemData(position).isChecked
    }

    override fun compare(item1: GroupToggle, item2: GroupToggle): Int {
        val label1 = item1.group.label
        val label2 = item2.group.label
        return label1.compareTo(label2, ignoreCase = true)
    }

    override fun areContentsTheSame(item1: GroupToggle, item2: GroupToggle): Boolean {
        return item1.group == item2.group && item1.isChecked == item2.isChecked
    }

    override fun areItemsTheSame(item1: GroupToggle, item2: GroupToggle): Boolean {
        return item1.group == item2.group
    }

    override fun onItemClick(view: View, position: Int) {
        val text: CheckedTextView = view.findViewById(R.id.adapter_user_groups_textview) as CheckedTextView
        text.isChecked = !text.isChecked
        val group = getItemData(position)
        group.isChecked = text.isChecked
    }

    /**
     * ViewHolder for the entered [Group] data.
     */
    private final class ContentViewHolder(view: View, listener: ItemClickListener) : BaseViewHolder(view, listener) {
        val checkedTextView: CheckedTextView by bindView(R.id.adapter_user_groups_textview)
    }
}
