package com.android.aegentcam.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.aegentcam.R
import com.android.aegentcam.databinding.FragmentRecordSettingBinding
import com.android.aegentcam.helper.visible
import com.android.aegentcam.model.SettingItem
import com.android.aegentcam.view.adapter.SettingAdapter


class RecordSettingFragment : Fragment() {

    lateinit var binding: FragmentRecordSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentRecordSettingBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initView()
        //initRecyclerView()
        return binding.root
    }

    /*private fun initRecyclerView() {
        val settingsList = listOf(
            SettingItem.SegmentedControl("Recording Length", listOf("1min", "3min", "5min", "10min"), 0),
            SettingItem.SwitchSetting("Phone GPS sync", false),
            SettingItem.SwitchSetting("Start Recording On Boot", false),
            SettingItem.SwitchSetting("Recording Notification Sound", false),
            SettingItem.SwitchSetting("Selective Save Mode", false),
        )

        val settingsAdapter = SettingAdapter(settingsList, { position, isChecked ->
            (settingsList[position] as SettingItem.SwitchSetting).isEnabled = isChecked
        }, {index, position ->

        })

        binding.settingsRecyclerView.adapter = settingsAdapter
    }*/

    private fun initView() {
        binding.header.tvTitle.text = getString(R.string.record_setting)
        binding.header.ivBack.visible()
        binding.header.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }


}