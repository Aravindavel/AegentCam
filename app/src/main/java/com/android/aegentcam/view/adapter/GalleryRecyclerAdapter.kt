package com.android.aegentcam.view.adapter

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.aegentcam.R
import com.android.aegentcam.model.GalleryItem
import com.android.aegentcam.view.activity.FullScreenSliderActivity
import com.bumptech.glide.Glide

class GalleryRecyclerAdapter(
    private val mContext: Context,
    private val mListener: ItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    private val mItems: ArrayList<GalleryItem> = ArrayList<GalleryItem>()

    fun setAllList(items: ArrayList<GalleryItem>) {
        mItems.clear()
        mItems.addAll(items)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): GalleryItem {
        return mItems[position]
    }

    val items: ArrayList<GalleryItem>
        get() = mItems

    fun removeItem(position: Int) {
        mItems.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemViewType(position: Int): Int {
        return mItems[position].mViewType
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
        if (i == VIEW_TYPE_VIDEO) {
            return VideoHolder(
                LayoutInflater.from(mContext).inflate(R.layout.holder_video, viewGroup, false),
                mListener
            )
        } else {
            return PhotoHolder(
                LayoutInflater.from(mContext).inflate(R.layout.holder_photo, viewGroup, false),
                mListener
            )
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        if (viewHolder is VideoHolder) {
            val item: GalleryItem = mItems[i]
            Glide.with(mContext).load(item.thumbnailPath).into(viewHolder.mThumbIv)
            val title: String = item.fileName.toString()
            /*viewHolder.mThumbIv.setOnClickListener {
                val mediaList = mItems.map { it.thumbnailPath }.toCollection(ArrayList())
                val intent = Intent(mContext, FullScreenSliderActivity::class.java).apply {
                    putStringArrayListExtra("mediaList", mediaList)
                    putExtra("position", i)
                }
                // Start Activity with Zoom-in Animation
                val options = ActivityOptions.makeCustomAnimation(mContext, R.anim.zoom_in, R.anim.zoom_out)
                mContext.startActivity(intent, options.toBundle())
            }*/
        } else if (viewHolder is PhotoHolder) {
            val item: GalleryItem = mItems[i]
            Glide.with(mContext).load(item.thumbnailPath).into(viewHolder.mThumbIv)
            /*viewHolder.mThumbIv.setOnClickListener {
                val mediaList = mItems.map { it.thumbnailPath }.toCollection(ArrayList())
                val intent = Intent(mContext, FullScreenSliderActivity::class.java).apply {
                    putStringArrayListExtra("mediaList", mediaList)
                    putExtra("position", i)
                }
                // Start Activity with Zoom-in Animation
                val options = ActivityOptions.makeCustomAnimation(mContext, R.anim.zoom_in, R.anim.zoom_out)
                mContext.startActivity(intent, options.toBundle())
            }*/
        }
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    private class VideoHolder(itemView: View, private val mListener: ItemClickListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var mThumbIv: ImageView =
            itemView.findViewById<ImageView>(R.id.iv_thumbnail)
        var mDownloadTv: ImageView =
            itemView.findViewById<ImageView>(R.id.iv_download)
        var mDeleteTv: ImageView =
            itemView.findViewById<ImageView>(R.id.iv_delete)

        init {
            mDownloadTv.setOnClickListener(this)
            mDeleteTv.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            mListener.clickedItem(layoutPosition, v.id == R.id.iv_delete)
        }
    }

    private class PhotoHolder(itemView: View, private val mListener: ItemClickListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var mThumbIv: ImageView =
            itemView.findViewById<ImageView>(R.id.iv_thumbnail)
        var mDownloadTv: ImageView =
            itemView.findViewById<ImageView>(R.id.iv_download)
        var mDeleteTv: ImageView =
            itemView.findViewById<ImageView>(R.id.iv_delete)

        init {
            mDownloadTv.setOnClickListener(this)
            mDeleteTv.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            mListener.clickedItem(layoutPosition, v.id == R.id.iv_delete)
        }
    }

    interface ItemClickListener {
        fun clickedItem(position: Int, isDelete: Boolean)
    }

    companion object {
        const val VIEW_TYPE_VIDEO: Int = 1
        const val VIEW_TYPE_PHOTO: Int = 2
    }
}
