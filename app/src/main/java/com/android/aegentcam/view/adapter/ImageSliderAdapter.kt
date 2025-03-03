package com.android.aegentcam.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.android.aegentcam.R
import com.bumptech.glide.Glide

class ImageSliderAdapter(val context: Context, val imageList: List<String>) : PagerAdapter() {

    lateinit var iv_banner: ImageView

    override fun getCount(): Int {
        return imageList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }


    @SuppressLint("MissingInflatedId")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = layoutInflater.inflate(R.layout.item_slider, null)
        iv_banner = view.findViewById(R.id.iv_banner_image)
        val viewPager = container as ViewPager
        viewPager.addView(view, 0)

        Glide.with(context).load(imageList[position])
            .placeholder(R.drawable.ic_default_banner)
            .error(R.drawable.ic_default_banner).into(iv_banner)


        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val viewPager = container as ViewPager
        val view = `object` as View
        viewPager.removeView(view)
    }
}