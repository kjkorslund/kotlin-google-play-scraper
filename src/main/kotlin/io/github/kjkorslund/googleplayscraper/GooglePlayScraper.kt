package io.github.kjkorslund.googleplayscraper

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking

fun main(): Unit { // this: CoroutineScope
    GooglePlayScraper.testScrape()
}

object GooglePlayScraper {
    private const val PLAYSTORE_BASE_URL = "https://play.google.com/store/apps/details";
    private val REGEX_AF_INITDATACALLBACK_PARAM = Regex(
        """<script[\s\S]*?>AF_initDataCallback\(([\s\S]*?)\);<\/script>"""
    )
    private val REGEX_KEY = Regex("""key:\s*'([\s\S]*?)'""")
    private val REGEX_DATA = Regex("""data:\s*(\[[\s\S]*\])\s*,\s*sideChannel""")


    fun testScrape(): Unit {
        val results = runBlocking { scrape("com.google.android.apps.translate", "en", "us") }
        println(results.keys)
        println(results.size)
    }

    private suspend fun scrape(id: String, lang: String, country: String): Map<String, String> {
        val url = "$PLAYSTORE_BASE_URL?id=$id&hl=$lang&gl=$country"
        println(url)

        val client = HttpClient()
        val response: HttpResponse = client.get(url)
        val text = response.readText()
//        println(text)
        return REGEX_AF_INITDATACALLBACK_PARAM.findAll(text)
            .map {
                val key = REGEX_KEY.find(it.value)?.value
                val data = REGEX_DATA.find(it.value)?.value
                if (key != null && data != null) {
                    key to data
                } else null
            }
            .filterNotNull()
            .toMap()
    }
}