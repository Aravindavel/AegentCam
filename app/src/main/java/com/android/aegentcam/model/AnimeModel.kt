package com.android.aegentcam.model

import com.google.gson.annotations.SerializedName

data class AnimeModel (

    @SerializedName("pagination" ) var pagination : Pagination     = Pagination(),
    @SerializedName("data"       ) var data       : ArrayList<Data> = arrayListOf()

){
    data class Pagination (

        @SerializedName("last_visible_page" ) var lastVisiblePage : Int     = 0,
        @SerializedName("has_next_page"     ) var hasNextPage     : Boolean = false,
        @SerializedName("current_page"      ) var currentPage     : Int     = 0,
        @SerializedName("items"             ) var items           : Items   = Items()

    )

    data class Items (

        @SerializedName("count"    ) var count   : Int = 0,
        @SerializedName("total"    ) var total   : Int = 0,
        @SerializedName("per_page" ) var perPage : Int = 0

    )


    data class Data (

        @SerializedName("mal_id"          ) var malId          : Int                    = 0,
        @SerializedName("images"          ) var images         : Images                 = Images(),
        @SerializedName("trailer"         ) var trailer       : Trailer                = Trailer(),
        @SerializedName("title"           ) var title          : String                 = "",
        @SerializedName("episodes"        ) var episodes       : Int                    = 0,
        @SerializedName("rating"          ) var rating         : String                 = "",
        @SerializedName("score"           ) var score          : Double                 = 0.0,
        @SerializedName("synopsis"        ) var synopsis       : String                 = "",
        @SerializedName("source"          ) var source         : String?                 = "",
        @SerializedName("producers"       ) var producers      : ArrayList<Producers>    = arrayListOf(),
        @SerializedName("genres"          ) var genres         : ArrayList<Genres>       = arrayListOf()

    )

    data class Trailer (

        @SerializedName("youtube_id" ) var youtubeId : String = "",
        @SerializedName("url"        ) var url       : String = "",
        @SerializedName("embed_url"  ) var embedUrl  : String = "",
        @SerializedName("images"     ) var images    : Images = Images()

    )

    data class Images (

        @SerializedName("jpg"  ) var jpg  : Jpg  = Jpg(),

    )

    data class Jpg (

        @SerializedName("image_url"       ) var imageUrl      : String = "",
        @SerializedName("small_image_url" ) var smallImageUrl : String = "",
        @SerializedName("large_image_url" ) var largeImageUrl : String = ""

    )

    data class Genres (

        @SerializedName("mal_id" ) var malId : Int    = 0,
        @SerializedName("type"   ) var type  : String = "",
        @SerializedName("name"   ) var name  : String = "",
        @SerializedName("url"    ) var url   : String = ""

    )

    data class Producers (

        @SerializedName("mal_id" ) var malId : Int    = 0,
        @SerializedName("type"   ) var type  : String = "",
        @SerializedName("name"   ) var name  : String = "",
        @SerializedName("url"    ) var url   : String = ""

    )
}