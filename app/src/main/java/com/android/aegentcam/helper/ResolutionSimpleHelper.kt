package com.android.aegentcam.helper

import kotlin.math.min

class ResolutionSimpleHelper {
    private val mVideoResolutionsFront = arrayOf("1440x720", "2160x1080", "3840x1920")
    private val mVideoResolutionsUltraWide = arrayOf("1280x720", "1920x1080", "3840x2160")
    private val mPhotoResolutionsFront = arrayOf("1440x720", "2880x1440", "4800x2400")
    private val mPhotoResolutionsUltraWide = arrayOf("1280x720", "1920x1080", "3840x2160")

    private val mSimpleSideBySideTarget = arrayOf(
        arrayOf(
            arrayOf(
                intArrayOf(2160, 720),
                intArrayOf(1440, 480)
            ),
            arrayOf(
                intArrayOf(4320, 1440),
                intArrayOf(2160, 720)
            ),
            arrayOf(
                intArrayOf(7200, 2400),
                intArrayOf(3840, 1280)
            )
        ),
        arrayOf(
            arrayOf(
                intArrayOf(1440, 720),
                intArrayOf(1440, 720)
            ),
            arrayOf(
                intArrayOf(2880, 1440),
                intArrayOf(2160, 1080)
            ),
            arrayOf(
                intArrayOf(4800, 2400),
                intArrayOf(3840, 1920)
            )
        ),
        arrayOf(
            arrayOf(
                intArrayOf(1280, 720),
                intArrayOf(1280, 720)
            ),
            arrayOf(
                intArrayOf(1920, 1080),
                intArrayOf(1920, 1080)
            ),
            arrayOf(
                intArrayOf(3840, 2160),
                intArrayOf(3840, 2160)
            )
        )
    )

    //low, middle, high, recommend for 30fps
    private val mSimpleSideBySideBitrate = arrayOf(
        intArrayOf(5, 10, 20, 5),
        intArrayOf(5, 10, 20, 10),
        intArrayOf(-1, 10, 20, 20)
    )

    fun getSideBySideSimpleType(
        photoWidth: Int,
        photoHeight: Int,
        videoWidth: Int,
        videoHeight: Int
    ): Int {
        val videoSimpleType = sideBySideSimpleType(0, TARGET_TYPE_VIDEO, videoWidth, videoHeight)
        val photoSimpleType = sideBySideSimpleType(0, TARGET_TYPE_PHOTO, photoWidth, photoHeight)
        return decideSimpleType(videoSimpleType, photoSimpleType)
    }

    fun getSideBySideSimpleVideoType(mode: Int, videoWidth: Int, videoHeight: Int): Int {
        return sideBySideSimpleType(mode, TARGET_TYPE_VIDEO, videoWidth, videoHeight)
    }

    fun getVideoResolution(mode: Int, type: Int): IntArray {
        return intArrayOf(
            mSimpleSideBySideTarget[mode][type][TARGET_TYPE_VIDEO][0],
            mSimpleSideBySideTarget[mode][type][TARGET_TYPE_VIDEO][1]
        )
    }

    fun getPhotoResolution(mode: Int, type: Int): IntArray {
        return intArrayOf(
            mSimpleSideBySideTarget[mode][type][TARGET_TYPE_PHOTO][0],
            mSimpleSideBySideTarget[mode][type][TARGET_TYPE_PHOTO][1]
        )
    }

    fun getVideoBitrate(type: Int): IntArray {
        return mSimpleSideBySideBitrate[type]
    }

    private fun decideSimpleType(videoSimpleType: Int, photoSimpleType: Int): Int {
        return min(videoSimpleType.toDouble(), photoSimpleType.toDouble()).toInt()
    }

    private fun sideBySideSimpleType(mode: Int, type: Int, width: Int, height: Int): Int {
        var target: Array<IntArray>
        var i = 0
        while (i < mSimpleSideBySideTarget.size) {
            target = mSimpleSideBySideTarget[mode][i]
            if (target[type][0] == width && target[type][1] == height) {
                break
            }
            i++
        }
        return if (i != mSimpleSideBySideTarget.size) {
            i
        } else {
            mSimpleSideBySideTarget.size / 2
        }
    }

    companion object {
        const val SIMPLE_TYPE_LOW: Int = 0
        const val SIMPLE_TYPE_MIDDLE: Int = 1
        const val SIMPLE_TYPE_HIGH: Int = 2
        const val MODE_NORMAL: Int = 0
        const val MODE_DUAL: Int = 1
        const val MODE_ULTRA_WIDE: Int = 2
        private const val TARGET_TYPE_PHOTO = 0
        private const val TARGET_TYPE_VIDEO = 1
        private var mInstance: ResolutionSimpleHelper? = null

        val instance: ResolutionSimpleHelper
            get() {
                if (mInstance == null) {
                    mInstance = ResolutionSimpleHelper()
                }
                return mInstance!!
            }
    }
}
