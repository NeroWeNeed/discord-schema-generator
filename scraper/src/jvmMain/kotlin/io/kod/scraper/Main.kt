package io.kod.scraper

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import java.io.File


fun main(args: Array<String>) {

    scrape(
        listOf(
            "https://discord.com/developers/docs/resources/application",
            "https://discord.com/developers/docs/resources/guild",
            "https://discord.com/developers/docs/resources/channel",
            "https://discord.com/developers/docs/resources/emoji",
            "https://discord.com/developers/docs/resources/guild-template",
            "https://discord.com/developers/docs/resources/invite",
            "https://discord.com/developers/docs/resources/stage-instance",
            "https://discord.com/developers/docs/resources/sticker",
            "https://discord.com/developers/docs/resources/user",
            "https://discord.com/developers/docs/resources/voice",
            "https://discord.com/developers/docs/resources/webhook",
            "https://discord.com/developers/docs/topics/permissions",
            "https://discord.com/developers/docs/topics/teams",
            "https://discord.com/developers/docs/interactions/application-commands",
            "https://discord.com/developers/docs/interactions/message-components",
            "https://discord.com/developers/docs/interactions/receiving-and-responding"
        ),
        File(args[0])
    )
}

fun scrape(urls: List<String>, output: File) = runBlocking {
    println(System.getProperty("webdriver.gecko.driver"))
    val driver = FirefoxDriver()
    val resources = ArrayList<Resource?>()
    try {
        urls.forEach { arg ->
            driver.get(arg)
            driver.findElements(By.tagName("table")).map { it.findElement(By.xpath("./..")) }.distinct()
                .forEach { a: WebElement ->
                    var currentH6: WebElement? = null
                    a.findElements(By.ByXPath("*")).forEachIndexed { index, webElement ->
                        when (webElement.tagName) {
                            "h6" -> currentH6 = webElement
                            "table" -> {
                                if (!currentH6!!.getAttribute("id").contains("json-params")) {
                                    resources.add(parseObject(currentH6!!, webElement))
                                }
                            }
                        }
                    }

                }

        }
    } finally {
        driver.quit()
    }
    val json = Json {
        prettyPrint = true
    }
    resources.filterNotNull().apply {
        println(size)
    }.forEach {
        println(it.name)
    }

    output.writer().use {
        it.write(json.encodeToString(DiscordSchema(resources = resources.filterNotNull())))
    }


}