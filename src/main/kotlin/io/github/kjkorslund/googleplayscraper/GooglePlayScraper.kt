package io.github.kjkorslund.googleplayscraper

import io.github.kjkorslund.googleplayscraper.model.App
import io.github.kjkorslund.googleplayscraper.model.deserialize
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun main() { // this: CoroutineScope
    val scraper = GooglePlayScraper(HttpClient())
    val rawData = measureTimedResultMillis {
        runBlocking {
            scraper.scrapeApp("com.google.android.apps.translate", "en", "us")
        }
    }.record {
        println("Scrape time (ms): $it")
    }
//        .also(::println)

    val jsonData = measureTimedResultMillis {
        rawData.mapValues { Json.parseToJsonElement(it.value) }
    }.record {
        println("Json parse time (ms): $it")
    }
//        .also(::println)

    val app = measureTimedResultMillis {
        App.deserialize(jsonData)
    }.record {
        println("App deserialize time (ms): $it")
    }

    println(Json{ prettyPrint=true }.encodeToString(app))
}

private const val PLAYSTORE_BASE_URL = "https://play.google.com/store/apps/details";
private val REGEX_AF_INITDATACALLBACK_PARAM = Regex(
    """<script[\s\S]*?>AF_initDataCallback\(([\s\S]*?)\);<\/script>"""
)
private val REGEX_KEY = Regex("""key:\s*'([\s\S]*?)'""")
private val REGEX_DATA = Regex("""data:\s*(\[[\s\S]*\])\s*,\s*sideChannel""")

class GooglePlayScraper(private val client: HttpClient) {
    suspend fun scrapeApp(id: String, lang: String, country: String): Map<String, String> {
        val url = URLBuilder(PLAYSTORE_BASE_URL).apply {
            parameters["id"] = id
            parameters["hl"] = lang
            parameters["gl"] = country
        }.build()
//            .also(::println)

        val text = measureTimedResultMillis {
            client.get<String>(url)
        }.record {
            println("HTTP request time (ms): $it")
        }
//            .also(::println)

        return measureTimedResultMillis {
            REGEX_AF_INITDATACALLBACK_PARAM.findAll(text)
                .map {
                    val key = REGEX_KEY.find(it.value)?.groupValues?.get(1)
                    val data = REGEX_DATA.find(it.value)?.groupValues?.get(1)
                    if (key != null && data != null) {
                        key to data
                    } else null
                }
                .filterNotNull()
                .toMap()
        }.record {
            println("HTTP Response parse time (ms): $it")
        }
    }
}