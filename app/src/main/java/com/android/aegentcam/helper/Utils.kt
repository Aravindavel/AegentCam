package com.android.aegentcam.helper

import android.content.Context
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.gson.Gson

/**** Context ****/

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.loadImage(url: String?, target: ImageView) = Glide.with(this).load(url).into(target)


/**** View ****/

fun View.gone(){
    visibility = View.GONE
}
fun View.visible(){
    visibility = View.VISIBLE
}
fun View.inVisible(){
    visibility = View.INVISIBLE
}


inline infix fun <reified T : Any> Any?.convertAs(temp: Class<T>): T =
    when (this) {
        is String -> Gson().fromJson(this, T::class.java)
        else -> this as T
    }

