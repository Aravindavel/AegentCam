package com.android.aegentcam.helper

open class BaseBTItem {
    var mViewType: Int
    var mTitle: Int = 0
    var isProgressing: Boolean = false
        private set

    constructor(viewType: Int) {
        mViewType = viewType
    }

    constructor(viewType: Int, title: Int) {
        mViewType = viewType
        mTitle = title
    }

    constructor(viewType: Int, title: Int, progress: Boolean) {
        mViewType = viewType
        mTitle = title
        isProgressing = progress
    }

    fun setProgressState(state: Boolean) {
        isProgressing = state
    }

    companion object {
        const val VIEW_TYPE_DEVICE: Int = 12
    }
}
