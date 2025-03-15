package com.android.aegentcam.model

import android.media.MediaMetadataRetriever
import android.os.Parcel
import android.os.Parcelable
import java.io.File

class GalleryItem : Parcelable {
    var mViewType: Int
    var filePath: String? = null
        private set
    var fileName: String?
        private set
    var thumbnailPath: String?
        private set
    var duration: Long = 0
        private set
    private var mSelected = false

    constructor(viewType: Int, fileName: String?, thumbNailPath: String?) {
        mViewType = viewType
        this.fileName = fileName
        thumbnailPath = thumbNailPath
    }

    constructor(viewType: Int, file: File, thumbNailPath: String?) {
        mViewType = viewType
        filePath = file.path
        fileName = file.name
        thumbnailPath = thumbNailPath

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(file.path)
        } catch (e: Exception) {
            duration = 0
            return
        }

        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        if (duration != null) {
            this.duration = duration.toLong()
        } else {
            this.duration = 0
        }
    }

    protected constructor(`in`: Parcel) {
        mViewType = `in`.readInt()
        filePath = `in`.readString()
        fileName = `in`.readString()
        thumbnailPath = `in`.readString()
        duration = `in`.readLong()
        mSelected = `in`.readByte().toInt() != 0
    }

    val durationText: String
        get() {
            var totalSeconds = duration / 1000
            val hours = totalSeconds / (60 * 60)

            totalSeconds = totalSeconds % (60 * 60)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val durationText = if (hours > 0) {
                String.format(
                    "%s:%s:%s",
                    get2xStringFromInt(hours),
                    get2xStringFromInt(minutes),
                    get2xStringFromInt(seconds)
                )
            } else {
                String.format(
                    "%s:%s",
                    get2xStringFromInt(minutes),
                    get2xStringFromInt(seconds)
                )
            }
            return durationText
        }

    private fun get2xStringFromInt(number: Long): String {
        return if (number == 0L) {
            "00"
        } else if (number < 10) {
            String.format("0%d", number)
        } else {
            String.format("%d", number)
        }
    }

    fun ismSelected(): Boolean {
        return mSelected
    }

    fun selectToggle() {
        mSelected = !mSelected
    }

    fun select() {
        mSelected = true
    }

    fun unselect() {
        mSelected = false
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(mViewType)
        parcel.writeString(filePath)
        parcel.writeString(fileName)
        parcel.writeString(thumbnailPath)
        parcel.writeLong(duration)
        parcel.writeByte((if (mSelected) 1 else 0).toByte())
    }

    companion object {
        const val VIEW_TYPE_VIDEO: Int = 1
        const val VIEW_TYPE_PHOTO: Int = 2

        @JvmField
        val CREATOR: Parcelable.Creator<GalleryItem?> = object : Parcelable.Creator<GalleryItem?> {
            override fun createFromParcel(`in`: Parcel): GalleryItem {
                return GalleryItem(`in`)
            }

            override fun newArray(size: Int): Array<GalleryItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}
