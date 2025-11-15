package com.yybb.myapplication.data.model

import com.yybb.myapplication.data.network.dto.SpaceNodeResponse

data class SpaceNode(
    val id: Int,
    val label: String,
    val wikidataId: String?,
    val country: String?,
    val city: String?,
    val district: String?,
    val street: String?,
    val latitude: String?,
    val longitude: String?,
    val locationName: String?,
    val connectionCount: Int = 0
)

fun SpaceNodeResponse.toSpaceNode(connectionCount: Int = 0): SpaceNode {
    return SpaceNode(
        id = id,
        label = label,
        wikidataId = wikidataId,
        country = country,
        city = city,
        district = district,
        street = street,
        latitude = latitude,
        longitude = longitude,
        locationName = locationName,
        connectionCount = connectionCount
    )
}

