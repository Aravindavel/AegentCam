package com.android.aegentcam.helper

import android.os.Looper
import com.android.aegentcam.model.GalleryItem
import com.android.aegentcam.view.adapter.GalleryRecyclerAdapter
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.linkflow.blackboxsdk.NeckbandRestApiClient
import com.linkflow.blackboxsdk.helper.media.DownloadHelper
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

class MediaModel(private val mListener: Listener) {
    private val mIsWorking = BooleanArray(3)

    @Throws(Exception::class)
    fun getMediaList(accessToken: String?, skip: Int, take: Int) {
        if (!mIsWorking[0]) {
            mIsWorking[0] = true
            val service = NeckbandRestApiClient.getInstance().create(
                Service::class.java
            )
            val callback = service.getMediaList2(accessToken, take, skip)
            callback.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                    mIsWorking[0] = false
                    var success = false
                    val items: ArrayList<GalleryItem> = ArrayList<GalleryItem>()
                    val body = response.body()
                    val childId = 0
                    if (body != null) {
                        success = body["success"].asBoolean
                        if (success) {
                            val beforeDate: String? = null
                            val allItems: ArrayList<GalleryItem> = ArrayList<GalleryItem>()
                            val result = body.getAsJsonObject("result")
                            if (result.has("next")) {
                                val hasNext = result["next"].asBoolean
                            }
                            val files = result.getAsJsonArray("files")
                            for (file in files) {
                                val name = file.asString
                                items.add(
                                    GalleryItem(
                                        if (name.contains(".mp4")) GalleryRecyclerAdapter.VIEW_TYPE_VIDEO else GalleryRecyclerAdapter.VIEW_TYPE_PHOTO,
                                        name,
                                        NeckbandRestApiClient.getThumbnailPath(accessToken, name)
                                    )
                                )
                            }
                        }
                    }
                    mListener.completedGetMediaList(true, items)
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    mIsWorking[0] = false
                    mListener.completedGetMediaList(false, null)
                }
            })
        }
    }

    @Throws(Exception::class)
    fun delete(accessToken: String?, filenames: Array<String?>?) {
        if (!mIsWorking[1]) {
            mIsWorking[1] = true
            val gson = Gson()
            val params = JSONObject()
            try {
                params.put("access_token", accessToken)
                params.put("files", gson.toJson(filenames))
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val service = NeckbandRestApiClient.getInstance().create(
                Service::class.java
            )
            val body = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                params.toString()
            )
            val callback = service.delete(body)
            callback.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                    mIsWorking[1] = false
                    var success = false
                    val body = response.body()
                    if (body != null) {
                        success = body["success"].asBoolean
                    }
                    mListener.completedDelete(success, filenames, null)
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    mIsWorking[1] = false
                    mListener.completedDelete(false, filenames, null)
                }
            })
        }
    }

    @Throws(Exception::class)
    fun download(
        accessToken: String?,
        item: GalleryItem?,
        listener: DownloadHelper.DownloadListener?
    ) {
        if (item != null) {
            val service = NeckbandRestApiClient.getInstance().create(
                Service::class.java
            )
            val callback = service.downloadFile(
                NeckbandRestApiClient.getDownloadUrl(
                    accessToken,
                    item.fileName
                )
            )
            callback.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    if (response.isSuccessful) {
                        DownloadHelper(Looper.getMainLooper(), response.body(), listener).download()
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                }
            })
        }
    }

    @Throws(Exception::class)
    fun downloadGPS(
        accessToken: String?,
        filename: String?,
        listener: DownloadHelper.DownloadListener
    ) {
        if (filename != null) {
            val service = NeckbandRestApiClient.getInstance().create(
                Service::class.java
            )
            val callback =
                service.downloadFile(NeckbandRestApiClient.getGPSDownloadUrl(accessToken, filename))
            callback.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    if (response.isSuccessful) {
                        DownloadHelper(Looper.getMainLooper(), response.body(), listener).download()
                    } else {
                        listener.endDownload(false)
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    listener.endDownload(false)
                }
            })
        }
    }

    private interface Service {
        @GET("app/media/list2/videophoto/{accessToken}/{take}/{skip}")
        fun getMediaList2(
            @Path("accessToken") accessToken: String?,
            @Path("take") take: Int,
            @Path("skip") skip: Int
        ): Call<JsonObject>

        @POST("app/media/delete")
        fun delete(@Body body: RequestBody?): Call<JsonObject>

        @Streaming
        @GET
        fun downloadFile(@Url url: String?): Call<ResponseBody>
    }

    interface Listener {
        fun completedGetMediaList(success: Boolean, allItems: ArrayList<GalleryItem>?)
        fun completedDelete(success: Boolean, filenames: Array<String?>?, path: String?)
    }
}
