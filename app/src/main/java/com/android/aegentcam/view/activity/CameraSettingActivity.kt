package com.android.aegentcam.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.aegentcam.R
import com.android.aegentcam.databinding.ActivityCameraSettingBinding
import com.android.aegentcam.helper.CommonKeys.ID_ANGLE_OF_VIEW
import com.android.aegentcam.helper.CommonKeys.ID_BITRATE
import com.android.aegentcam.helper.CommonKeys.ID_BRIGHTNESS
import com.android.aegentcam.helper.CommonKeys.ID_FBS
import com.android.aegentcam.helper.CommonKeys.ID_MODE
import com.android.aegentcam.helper.CommonKeys.ID_RESOLUTION
import com.android.aegentcam.helper.ResolutionSimpleHelper
import com.android.aegentcam.helper.visible
import com.android.aegentcam.model.SettingItem
import com.android.aegentcam.network.NetworkResult
import com.android.aegentcam.view.adapter.SettingAdapter
import com.linkflow.blackboxsdk.helper.Version
import com.linkflow.blackboxsdk.manager.BTCommandManager.CommandCallback
import com.linkflow.blackboxsdk.manager.item.RecordSetItem

class CameraSettingActivity : BaseActivity() {

    lateinit var binding: ActivityCameraSettingBinding
    lateinit var settingsAdapter: SettingAdapter
    private val mVideoBitrate = intArrayOf(5, 12, 20, 30)
    private val mVideoFPS = intArrayOf(8, 15, 24, 30)
    private val mBrightness = intArrayOf(-6, 0, 6)
    private val mVideoResolutions = arrayOf("1440x480", "2160x720", "3840x2160")
    private val mVideoResolutionsFront = arrayOf("1440x720", "2160x1080", "3840x1920")
    private val mVideoResolutionsUltraWide = arrayOf("1280x720", "1920x1080", "3840x2160")
    private val mPhotoResolutions = arrayOf("2160x720", "4320x1440", "7200x2400")
    private val mPhotoResolutionsFront = arrayOf("1440x720", "2880x1440", "4800x2400")
    private val mPhotoResolutionsUltraWide = arrayOf("1280x720", "1920x1080", "3840x2160")
    private val mSensorResolution = arrayOf("1600*1600", "2400*2400")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
        initRecyclerView()
    }

    private fun initView() {
        binding.header.tvTitle.text = getString(R.string.camera_setting)
        binding.header.ivBack.visible()
        binding.header.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent()
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun initRecyclerView() {
        val settingsList = listOf(
            SettingItem.SegmentedControl(
                ID_MODE, listOf("All", "Front)", "Back"), getCurrentPosition(
                    ID_MODE
                )),
            SettingItem.SegmentedControl(
                ID_RESOLUTION, listOf("Low", "Middle", "High"), getCurrentPosition(
                    ID_RESOLUTION
                )), // Default middle
            SettingItem.SegmentedControl(
                ID_FBS, listOf("8FPS", "15FPS", "24FPS", "30FPS"), getCurrentPosition(
                    ID_FBS
                )),
            SettingItem.SegmentedControl(
                ID_BITRATE, listOf("Low", "Middle", "High", "Highest"), getCurrentPosition(
                    ID_BITRATE
                )),
            SettingItem.SegmentedControl(
                ID_BRIGHTNESS, listOf("Dark", "Middle", "Bright"), getCurrentPosition(
                    ID_BRIGHTNESS
                )),
            SettingItem.SegmentedControl(
                ID_ANGLE_OF_VIEW, listOf("Normal", "Wide", "Fish-eye"), getCurrentPosition(
                    ID_ANGLE_OF_VIEW
                )),
        )

        settingsAdapter = SettingAdapter(settingsList, { position, isChecked ->
            (settingsList[position] as? SettingItem.SwitchSetting)?.isEnabled = isChecked
        }, { tabPosition, modePosition ->
            (settingsList[modePosition] as? SettingItem.SegmentedControl)?.selectedOptionPosition = tabPosition

            when {
                (settingsList[modePosition] as? SettingItem.SegmentedControl)?.title == ID_MODE -> {
                    if (tabPosition == 2) {
                        mBTCommandManager!!.commandSetCameraSinglePosition(0,
                            CommandCallback { success, response ->
                                if (success) {
                                    mBTCommandManager!!.singleCameraPosition = 0
                                    mBTCommandManager!!.setEnabledDualMode(false)
                                }
                                commonMethods.showMessage(
                                    this,
                                    false,
                                    success,
                                    intArrayOf(R.string.applied_need_restart, R.string.not_applied)
                                )
                            })
                    } else {
                        mBTCommandManager!!.commandSetCameraDualMode(tabPosition == 1,
                            CommandCallback { success, response ->
                                if (success) {
                                    mBTCommandManager!!.singleCameraPosition = -1
                                    mBTCommandManager!!.setEnabledDualMode(tabPosition == 1)
                                }
                                commonMethods.showMessage(this, false, success, intArrayOf(R.string.applied_need_restart, R.string.not_applied))
                            })
                    }
                }
                (settingsList[modePosition] as? SettingItem.SegmentedControl)?.title == ID_RESOLUTION -> {
                    val recordSetItem: RecordSetItem = mBTCommandManager!!.getRecordSetItem()
                    if (recordSetItem != null) {
                        val mode = recordSetItem.mViewMode
                        val enabledSingleCameraMode: Boolean =
                            mBTCommandManager!!.enabledSingleCamera()
                        val enabledDualCameraMode: Boolean = mBTCommandManager!!.enabledDualMode()
                        val enabledUltraWideMode: Boolean = mBTCommandManager!!.enabledUltraWideMode()
                        val res: Array<String> = getFitResolution(
                            true,
                            enabledSingleCameraMode,
                            enabledDualCameraMode,
                            enabledUltraWideMode
                        ).get(tabPosition).split("x".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        val resolution =
                            if (enabledSingleCameraMode) intArrayOf(720, 720) else intArrayOf(
                                res[0].toInt(), res[1].toInt()
                            )
                        val camera = arrayOf(
                            recordSetItem.mSingle,
                            recordSetItem.mSideBySide,
                            recordSetItem.mStitching
                        )
                        var bitrate =
                            arrayOf(recordSetItem.mBitrate.toString(), recordSetItem.mBitrateMode)
                        val iFrameInterval = recordSetItem.mIFrameInterval
                        val fps = recordSetItem.mFPS
                        var bit = recordSetItem.mBitrate

                        if (recordSetItem.mFPS == 30) {
                            if (tabPosition == 2 || tabPosition == 1) {
                                bit = mVideoBitrate.get(tabPosition)
                                bitrate = arrayOf(bit.toString(), recordSetItem.mBitrateMode)
                            }
                        } else {
                            var bitratePosition = -1
                            if (tabPosition == 0 && recordSetItem.mFPS == 8) {
                                bitratePosition = 1
                            } else if (tabPosition == 2 && recordSetItem.mBitrate == 5) {
                                bitratePosition = 1
                            }
                            if (bitratePosition != -1) {
                                bit = mVideoBitrate.get(bitratePosition)
                                bitrate = arrayOf(bit.toString(), recordSetItem.mBitrateMode)
                            }
                        }
                        val finalBit = bit
                        mBTCommandManager!!.commandSetRecordQuality(mode,
                            camera,
                            resolution,
                            fps,
                            bitrate,
                            iFrameInterval,
                            CommandCallback { success, response ->
                                if (success) {
                                    recordSetItem.setResolution(resolution[0], resolution[1])
                                    recordSetItem.setBitrate(finalBit)
                                    val photoRes: Array<String> = getFitResolution(
                                        false,
                                        enabledSingleCameraMode,
                                        enabledDualCameraMode,
                                        enabledUltraWideMode
                                    ).get(tabPosition).split("x".toRegex())
                                        .dropLastWhile { it.isEmpty() }.toTypedArray()
                                    val resolution =
                                        if (mBTCommandManager!!.enabledSingleCamera()) intArrayOf(
                                            720,
                                            720
                                        ) else intArrayOf(
                                            photoRes[0].toInt(), photoRes[1].toInt()
                                        )
                                    mBTCommandManager!!.commandSetPhotoQuality(mode,
                                        camera,
                                        resolution,
                                        CommandCallback { success, response ->
                                            if (success) {
                                                mBTCommandManager!!.getPhotoSetItem().setResolution(
                                                    resolution[0],
                                                    resolution[1]
                                                )
                                            }
                                        })
                                }
                                commonMethods.showMessage(this, false, success, intArrayOf(R.string.applied_need_restart, R.string.not_applied))
                            })
                    }
                }

                (settingsList[modePosition] as? SettingItem.SegmentedControl)?.title == ID_FBS -> {
                    val recordSetItem = mBTCommandManager!!.getRecordSetItem()
                    if (recordSetItem != null) {
                        val enabledSingleCameraMode: Boolean =
                            mBTCommandManager!!.enabledSingleCamera()
                        val mode: String = recordSetItem.mViewMode
                        val resolution =
                            if (enabledSingleCameraMode) intArrayOf(720, 720) else intArrayOf(
                                recordSetItem.getWidth(),
                                recordSetItem.getHeight()
                            )
                        val camera = arrayOf<String>(
                            recordSetItem.mSingle,
                            recordSetItem.mSideBySide,
                            recordSetItem.mStitching
                        )
                        var bitrate = arrayOf<String>(
                            recordSetItem.mBitrate.toString(),
                            recordSetItem.mBitrateMode
                        )
                        val iFrameInterval: Int = recordSetItem.mIFrameInterval
                        val fps: Int = mVideoFPS.get(tabPosition)
                        var bit: Int = recordSetItem.mBitrate

                        val cameraMode: Int = getCameraMode(
                            mBTCommandManager!!.enabledDualMode(),
                            mBTCommandManager!!.enabledUltraWideMode()
                        )
                        val simpleType: Int = ResolutionSimpleHelper.instance
                            .getSideBySideSimpleVideoType(
                                cameraMode,
                                recordSetItem.getWidth(),
                                recordSetItem.getHeight()
                            )
                        if (tabPosition == 2 || tabPosition == 3) {
                            var bitrate = arrayOf<String>(
                                recordSetItem.mBitrate.toString(),
                                recordSetItem.mBitrateMode
                            )
                            var bitratePosition = -1
                            val fps: Int = mVideoFPS.get(tabPosition)
                            var bit: Int = recordSetItem.mBitrate
                            if (simpleType == ResolutionSimpleHelper.SIMPLE_TYPE_HIGH) {
                                bitratePosition = 2
                            } else if (simpleType == ResolutionSimpleHelper.SIMPLE_TYPE_MIDDLE) {
                                bitratePosition = 1
                            }
                            if (bitratePosition != -1) {
                                bit = mVideoBitrate[bitratePosition]
                                bitrate = arrayOf<String>(
                                    bit.toString(),
                                    recordSetItem.mBitrateMode
                                )
                            }
                            val finalBit = bit
                            mBTCommandManager!!.commandSetRecordQuality(mode,
                                camera,
                                resolution,
                                fps,
                                bitrate,
                                iFrameInterval,
                                CommandCallback { success, response ->
                                    if (success) {
                                        recordSetItem.setFPS(fps)
                                        recordSetItem.setBitrate(finalBit)
                                    }
                                    commonMethods.showMessage(this, false, success, intArrayOf(R.string.applied_need_restart, R.string.not_applied))

                                })
                        } else if (tabPosition == 0 && simpleType == ResolutionSimpleHelper.SIMPLE_TYPE_LOW) {

                            bit = mVideoBitrate[1]
                            bitrate = arrayOf<String>(bit.toString(), recordSetItem.mBitrateMode)
                        } else {
                            var bitratePosition = -1
                            if (simpleType == ResolutionSimpleHelper.SIMPLE_TYPE_HIGH) {
                                bitratePosition = 2
                            } else if (simpleType == ResolutionSimpleHelper.SIMPLE_TYPE_MIDDLE) {
                                bitratePosition = 1
                            }
                            if (bitratePosition != -1) {
                                bit = mVideoBitrate[bitratePosition]
                                bitrate =
                                    arrayOf<String>(bit.toString(), recordSetItem.mBitrateMode)
                            }
                        }
                        val finalBit = bit
                        mBTCommandManager!!.commandSetRecordQuality(mode,
                            camera,
                            resolution,
                            fps,
                            bitrate,
                            iFrameInterval,
                            CommandCallback { success, response ->
                                if (success) {
                                    recordSetItem.setFPS(fps)
                                    recordSetItem.setBitrate(finalBit)
                                }
                                commonMethods.showMessage(this, false, success, intArrayOf(R.string.applied_need_restart, R.string.not_applied))
                            })
                    }
                }
                (settingsList[modePosition] as? SettingItem.SegmentedControl)?.title == ID_BITRATE -> {
                    val recordSetItem = mBTCommandManager!!.getRecordSetItem()
                    if (recordSetItem != null) {
                        val enabledSingleCameraMode: Boolean =
                            mBTCommandManager!!.enabledSingleCamera()
                        val mode: String = recordSetItem.mViewMode
                        val bit = mVideoBitrate[tabPosition]
                        val resolution =
                            if (enabledSingleCameraMode) intArrayOf(720, 720) else intArrayOf(
                                recordSetItem.getWidth(),
                                recordSetItem.getHeight()
                            )
                        val camera = arrayOf<String>(
                            recordSetItem.mSingle,
                            recordSetItem.mSideBySide,
                            recordSetItem.mStitching
                        )
                        val bitrate = arrayOf<String>(bit.toString(), recordSetItem.mBitrateMode)
                        val iFrameInterval: Int = recordSetItem.mIFrameInterval
                        val fps: Int = recordSetItem.mFPS

                        mBTCommandManager!!.commandSetRecordQuality(mode,
                            camera,
                            resolution,
                            fps,
                            bitrate,
                            iFrameInterval,
                            CommandCallback { success, response ->
                                if (success) {
                                    recordSetItem.setBitrate(bit)
                                }
                                commonMethods.showMessage(this, false, success, intArrayOf(R.string.applied_need_restart, R.string.not_applied))
                            })
                    }
                }
                (settingsList[modePosition] as? SettingItem.SegmentedControl)?.title == ID_BRIGHTNESS -> {
                    val brightness = mBrightness[tabPosition]
                    mBTCommandManager!!.commandSetBrightness(brightness,
                        CommandCallback { success, response ->
                            if (success) {
                                mBTCommandManager!!.setBrightness(brightness)
                            }
                            commonMethods.showMessage(this, false, success, intArrayOf(R.string.applied_need_restart, R.string.not_applied))

                        })
                }

                (settingsList[modePosition] as? SettingItem.SegmentedControl)?.title == ID_ANGLE_OF_VIEW -> {
                    if (Version.isOver244(mBTCommandManager!!.getFirmwareVersion())) {
                        if (tabPosition != 1) {
                            if (mBTCommandManager!!.enabledUltraWideMode()) {
                                mBTCommandManager!!.commandSetUltraWideMode(false,
                                    CommandCallback { success, response ->
                                        if (success) {
                                            mBTCommandManager!!.setUltraWideMode(false)
                                            val sensor =
                                                mSensorResolution[if (tabPosition == 2) 1 else 0]
                                            mBTCommandManager!!.commandSetSensorResolution(sensor,
                                                CommandCallback { success, response ->
                                                    if (success) {
                                                        mBTCommandManager!!.setSensorResolution(sensor)
                                                    }
                                                    commonMethods.showMessage(this, false, success, intArrayOf(R.string.applied_need_restart, R.string.not_applied))

                                                })
                                        }
                                    })
                            } else {
                                val sensor = mSensorResolution[if (tabPosition == 2) 1 else 0]
                                mBTCommandManager!!.commandSetSensorResolution(sensor,
                                    CommandCallback { success, response ->
                                        if (success) {
                                            mBTCommandManager!!.setSensorResolution(sensor)
                                        }
                                        commonMethods.showMessage(this, false, success, intArrayOf(R.string.applied_need_restart, R.string.not_applied))

                                    })
                            }
                        } else {
                            mBTCommandManager!!.commandSetUltraWideMode(true,
                                CommandCallback { success, response ->
                                    if (success) {
                                        mBTCommandManager!!.setUltraWideMode(true)
                                    }
                                    commonMethods.showMessage(this, false, success, intArrayOf(R.string.applied_need_restart, R.string.not_applied))

                                })
                        }
                    } else {
                        val sensor = mSensorResolution[tabPosition]
                        mBTCommandManager!!.commandSetSensorResolution(sensor,
                            CommandCallback { success, response ->
                                if (success) {
                                    mBTCommandManager!!.setSensorResolution(sensor)
                                }
                                commonMethods.showMessage(this, false, success, intArrayOf(R.string.applied_need_restart, R.string.not_applied))

                            })
                    }
                }
            }
        })


        binding.settingsRecyclerView.adapter = settingsAdapter

    }

    fun getCurrentPosition(id: String): Int {
        return when (id) {
            ID_MODE -> {
                val options = listOf("All", "Front)", "Back")
                val selectedOption = when {
                    mBTCommandManager!!.enabledSingleCamera() -> "Back"
                    mBTCommandManager!!.enabledDualMode() -> "Front)"
                    else -> "All"
                }
                options.indexOf(selectedOption)
            }
            ID_RESOLUTION ->{
                val enabledSingleCameraMode: Boolean = mBTCommandManager!!.enabledSingleCamera()
                val enabledDualCameraMode: Boolean = mBTCommandManager!!.enabledDualMode()
                val enabledUltraWideMode: Boolean = mBTCommandManager!!.enabledUltraWideMode()
                return findPosition(
                    mBTCommandManager!!.getRecordSetItem().getResolution(),
                    getFitResolution(
                        true,
                        enabledSingleCameraMode,
                        enabledDualCameraMode,
                        enabledUltraWideMode
                    )
                )
            }
            ID_FBS ->{
                return findPosition(mBTCommandManager!!.recordSetItem.mFPS, mVideoFPS);
            }
            ID_BITRATE ->{
                return findPosition(mBTCommandManager!!.recordSetItem.mBitrate, mVideoBitrate)
            }
            ID_BRIGHTNESS ->{
                return findPosition(mBTCommandManager!!.getBrightness(), mBrightness)
            }
            ID_ANGLE_OF_VIEW -> {
                if (mBTCommandManager!!.enabledUltraWideMode()) {
                    return 1
                } else {
                    val position =
                        findPosition(mBTCommandManager!!.sensorResolution, mSensorResolution)
                    return if (Version.isOver244(mBTCommandManager!!.firmwareVersion)) {
                        if (position == 1) 2 else 0
                    } else {
                        position
                    }
                }
            }
            else -> 0
        }
    }

    private fun findPosition(value: String, list: Array<String>): Int {
        for (i in list.indices) {
            if (list[i] == value) {
                return i
            }
        }
        return 0
    }

    private fun findPosition(value: Int, list: IntArray): Int {
        for (i in list.indices) {
            if (list[i] == value) {
                return i
            }
        }
        return 0
    }


    private fun getFitResolution(
        isVideo: Boolean,
        isSingleCameraMode: Boolean,
        isDualCameraMode: Boolean,
        isUltraWideMode: Boolean
    ): Array<String> {
        return if (isDualCameraMode && isUltraWideMode) {
            if (isVideo) mVideoResolutionsUltraWide else mPhotoResolutionsUltraWide
        } else if (isSingleCameraMode) {
            if (isVideo) mVideoResolutionsFront else mPhotoResolutionsFront
        } else if (isDualCameraMode) {
            if (isVideo) mVideoResolutionsFront else mPhotoResolutionsFront
        } else if (isUltraWideMode) {
            if (isVideo) mVideoResolutionsUltraWide else mPhotoResolutionsUltraWide
        } else {
            if (isVideo) mVideoResolutions else mPhotoResolutions
        }
    }

    private fun getCameraMode(isDualCameraMode: Boolean, isUltraWideMode: Boolean): Int {
        return if (isDualCameraMode && isUltraWideMode) {
            2
        } else if (isDualCameraMode) {
            1
        } else if (isUltraWideMode) {
            2
        } else {
            0
        }
    }


    override fun onSuccess(networkResult: NetworkResult<Any>) {
        
    }

    override fun onFailure(networkResult: NetworkResult<Any>) {
       
    }

    override fun onLoading(networkResult: NetworkResult<Any>) {
       
    }
}