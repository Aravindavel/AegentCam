package com.android.aegentcam.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.aegentcam.R
import com.android.aegentcam.model.SettingItem

class SettingAdapter(
    private val settingsList: List<SettingItem>,
    private val onSwitchToggle: (Int, Boolean) -> Unit,
    private val onSegmentedSelect: (Int,Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SEGMENTED = 0
        private const val TYPE_SWITCH = 1
        private const val TYPE_NAVIGATION = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (settingsList[position]) {
            is SettingItem.SegmentedControl -> TYPE_SEGMENTED
            is SettingItem.SwitchSetting -> TYPE_SWITCH
            is SettingItem.NavigationSetting -> TYPE_NAVIGATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SEGMENTED -> {
                val view = inflater.inflate(R.layout.item_segmented_control, parent, false)
                SegmentedViewHolder(view)
            }
            TYPE_SWITCH -> {
                val view = inflater.inflate(R.layout.item_switch_setting, parent, false)
                SwitchViewHolder(view)
            }
            TYPE_NAVIGATION -> {
                val view = inflater.inflate(R.layout.item_navigation_setting, parent, false)
                NavigationViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid View Type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val setting = settingsList[position]) {
            is SettingItem.SegmentedControl -> {
                val segmentedHolder = holder as SegmentedViewHolder
                segmentedHolder.title.text = setting.title
                segmentedHolder.optionsGroup.removeAllViews()

                setting.options.forEachIndexed { index, option ->
                    val button = TextView(holder.itemView.context).apply {
                        text = option
                        setPadding(24, 12, 24, 12)
                        if (index == setting.selectedOptionPosition) {  // ✅ Compare index instead of string
                            setTextColor(resources.getColor(R.color.txt_primary))
                            setBackgroundResource(R.drawable.selected_option_bg)
                        } else {
                            setTextColor(resources.getColor(R.color.bg_grey))
                        }
                        setOnClickListener {
                            setting.selectedOptionPosition = index  // ✅ Update selected index
                            onSegmentedSelect(index, position) // ✅ Pass selected index instead of string
                            notifyDataSetChanged()
                        }
                    }
                    segmentedHolder.optionsGroup.addView(button)
                }

            }

            is SettingItem.SwitchSetting -> {
                val switchHolder = holder as SwitchViewHolder
                switchHolder.title.text = setting.title
                switchHolder.switch.isChecked = setting.isEnabled
                switchHolder.switch.setOnCheckedChangeListener { _, isChecked ->
                    onSwitchToggle(position, isChecked)
                }
            }

            is SettingItem.NavigationSetting -> {
                val navHolder = holder as NavigationViewHolder
                navHolder.title.text = setting.title
                navHolder.value.text = setting.value
            }
        }
    }

    override fun getItemCount(): Int = settingsList.size

    class SegmentedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.settingTitle)
        val optionsGroup: LinearLayout = view.findViewById(R.id.optionsGroup)
    }

    class SwitchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.settingTitle)
        val switch: SwitchCompat = view.findViewById(R.id.settingSwitch)
    }

    class NavigationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.settingTitle)
        val value: TextView = view.findViewById(R.id.settingValue)
    }
}
