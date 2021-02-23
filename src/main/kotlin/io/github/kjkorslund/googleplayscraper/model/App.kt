package io.github.kjkorslund.googleplayscraper.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class App(
    val title: String,
//    val description: String,
    val descriptionHtml: String,
    val summary: String,
    val installs: String,
    val minInstalls: Int,
    val maxInstalls: Int,
    val score: Double,
    val scoreText: String,
    val ratings: Int,
    val reviews: Int,
    val histogram: Map<String, Int>,
//    val price: Double,
//    val free: Boolean,
//    val currency: String,
//    val priceText: String,
//    val offersIAP: Boolean,
//    val IAPRange: Any?,
//    val size: String,
//    val androidVersion: String,
//    val androidVersionText: String,
//    val developer: String,
//    val developerId: String,
//    val developerEmail: String,
//    val developerWebsite: String,
//    val developerAddress: String,
//    val privacyPolicy: String,
//    val developerInternalID: String,
//    val genre: String,
//    val genreId: String,
//    val familyGenre: String?,
//    val familyGenreId: String?,
//    val Icon: String,
//    val headerImage: String,
//    val screenshots: List<String>,
//    val video: String?,
//    val videoImage: String?,
//    val contentRating: String,
//    val contentRatingDescription: String?,
//    val adSupported: Boolean,
//    val released: Any?,
//    val updated: Int,
//    val version: String,
//    val recentChanges: String,
//    val comments: List<String>,
//    val editorsChoice: Boolean,
//    val features: List<Feature>,
//    val appId: String,
//    val url: String
) {
    companion object
}

data class Feature(
    val title: String,
    val description: String
)

fun App.Companion.deserialize(data: Map<String, JsonElement>): App {
    // TODO: throw custom exception if deserialization fails due to missing data / invalid path
    fun valueAt(key: String, vararg path: Int): JsonElement? {
        return data[key]?.let {
            var element = it
            for (index in path) {
                element = element.jsonArray[index]
            }
            element
        }
    }
    fun stringValueAt(key: String, vararg path: Int): String =
        valueAt(key, *path)!!.jsonPrimitive.content
    fun intValueAt(key: String, vararg path: Int): Int =
        valueAt(key, *path)!!.jsonPrimitive.int
    fun doubleValueAt(key: String, vararg path: Int): Double =
        valueAt(key, *path)!!.jsonPrimitive.double
    fun buildHistogram(root: JsonElement?): Map<String, Int> {
        val valueFn = { index: Int ->
            root?.jsonArray?.get(index)?.jsonArray?.get(1)?.jsonPrimitive?.int ?: 0
        }
        return listOf(1,2,3,4,5).map {
            it.toString() to valueFn(it)
        }.toMap()
    }

    return App(
        title = stringValueAt("ds:5", 0, 0, 0),
        descriptionHtml = stringValueAt("ds:5", 0, 10, 0, 1),
        summary = stringValueAt("ds:5", 0, 10, 1, 1),
        installs = stringValueAt("ds:5", 0, 12, 9, 0),
        minInstalls = intValueAt("ds:5", 0, 12, 9, 1),
        maxInstalls = intValueAt("ds:5", 0, 12, 9, 2),
        score = doubleValueAt("ds:6", 0, 6, 0, 1),
        scoreText = stringValueAt("ds:6", 0, 6, 0, 0),
        ratings = intValueAt("ds:6", 0, 6, 2, 1),
        reviews = intValueAt("ds:6", 0, 6, 3, 1),
        histogram = buildHistogram(valueAt("ds:6", 0, 6, 1)?.jsonArray)
    )
}
