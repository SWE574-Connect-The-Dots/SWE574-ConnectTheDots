package com.yybb.myapplication.data.model

import com.yybb.myapplication.data.network.dto.SpaceEdgeResponse

data class SpaceEdge(
    val id: Int,
    val source: Int,
    val target: Int,
    val label: String,
    val wikidataPropertyId: String?
)

fun SpaceEdgeResponse.toSpaceEdge(): SpaceEdge {
    return SpaceEdge(
        id = id,
        source = source,
        target = target,
        label = label,
        wikidataPropertyId = wikidataPropertyId
    )
}


