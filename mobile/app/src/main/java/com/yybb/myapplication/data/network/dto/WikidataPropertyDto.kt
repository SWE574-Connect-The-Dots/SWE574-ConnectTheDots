package com.yybb.myapplication.data.network.dto

import com.google.gson.annotations.SerializedName
import com.yybb.myapplication.data.model.WikidataProperty

data class WikidataPropertyDto(
    @SerializedName("id") val id: String,
    @SerializedName("label") val label: String,
    @SerializedName("description") val description: String?,
    @SerializedName("url") val url: String?
)

fun WikidataPropertyDto.toWikidataProperty(): WikidataProperty {
    return WikidataProperty(
        id = id,
        label = label,
        description = description.orEmpty(),
        url = url.orEmpty()
    )
}

