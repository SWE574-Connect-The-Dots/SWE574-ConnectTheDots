package com.yybb.myapplication.data.model

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.yybb.myapplication.data.network.dto.NodePropertyResponse
import com.yybb.myapplication.data.network.dto.NodeWikidataPropertyResponse

data class NodeProperty(
    val statementId: String,
    val propertyId: String,
    val propertyLabel: String,
    val valueText: String,
    val isEntity: Boolean,
    val entityId: String?,
    val display: String
) {
    val formattedLabel: String
        get() = "$propertyLabel ($propertyId)"
}

fun NodePropertyResponse.toNodeProperty(): NodeProperty {
    val parsedValue = parsePropertyValue(propertyValue)
    return NodeProperty(
        statementId = statementId,
        propertyId = propertyId,
        propertyLabel = propertyLabel,
        valueText = parsedValue.first,
        isEntity = parsedValue.second,
        entityId = parsedValue.third,
        display = display ?: buildDisplay(propertyLabel, parsedValue.first)
    )
}

fun NodeWikidataPropertyResponse.toNodeProperty(): NodeProperty {
    val parsedValue = parsePropertyValue(propertyValue)
    return NodeProperty(
        statementId = statementId,
        propertyId = propertyId,
        propertyLabel = propertyLabel,
        valueText = parsedValue.first,
        isEntity = parsedValue.second,
        entityId = parsedValue.third,
        display = display ?: buildDisplay(propertyLabel, parsedValue.first)
    )
}

private fun parsePropertyValue(element: JsonElement?): Triple<String, Boolean, String?> {
    if (element == null || element.isJsonNull) {
        return Triple("", false, null)
    }

    return when {
        element.isJsonPrimitive -> {
            Triple(element.asString, false, null)
        }

        element.isJsonObject -> {
            val obj = element.asJsonObject
            val text = if (obj.has("text") && obj.get("text").isJsonPrimitive) {
                obj.get("text").asString
            } else {
                obj.toString()
            }
            val id = if (obj.has("id") && obj.get("id").isJsonPrimitive) {
                obj.get("id").asString
            } else {
                null
            }
            Triple(text, true, id)
        }

        else -> Triple(element.toString(), false, null)
    }
}

private fun buildDisplay(propertyLabel: String, valueText: String): String {
    return if (valueText.isNotBlank()) {
        "$propertyLabel: $valueText"
    } else {
        propertyLabel
    }
}


