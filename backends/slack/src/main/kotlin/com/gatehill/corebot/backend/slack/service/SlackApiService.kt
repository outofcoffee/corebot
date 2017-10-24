package com.gatehill.corebot.backend.slack.service

import com.gatehill.corebot.backend.slack.config.SlackSettings
import com.gatehill.corebot.util.jsonMapper
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.charset.Charset
import java.util.ArrayList

/**
 * Interacts with the Slack API.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackApiService {
    private val logger: Logger = LogManager.getLogger(SlackApiService::class.java)

    inline fun <reified R> invokeSlackCommand(commandName: String, params: Map<String, String> = emptyMap()) =
            invokeSlackCommand(commandName, params, R::class.java)

    fun <R> invokeSlackCommand(commandName: String, params: Map<String, String>, responseClass: Class<R>): R {
        HttpClientBuilder.create().build().use { httpClient ->
            // build payload from params
            val payload = ArrayList<NameValuePair>()
            for ((key, value) in params) {
                if ("token" != key) {
                    payload.add(BasicNameValuePair(key, value))
                }
            }
            payload.add(BasicNameValuePair("token", SlackSettings.slackUserToken))

            // invoke command
            try {
                val request = HttpPost("https://slack.com/api/$commandName")
                request.entity = UrlEncodedFormEntity(payload, "UTF-8")

                httpClient.execute(request).let { response ->
                    response.entity.content.use {
                        val jsonResponse = String(it.readBytes(), Charset.forName("UTF-8"))
                        logger.debug("Slack API: $commandName returned HTTP status: ${response.statusLine.statusCode}")
                        logger.trace("Slack API: $commandName returned: $jsonResponse")
                        return jsonMapper.readValue(jsonResponse, responseClass)
                    }
                }

            } catch (e: Exception) {
                throw RuntimeException("Error calling Slack API: $commandName", e)
            }
        }
    }

    fun checkReplyOk(replyOk: Boolean) {
        if (!replyOk) {
            throw RuntimeException("Response 'ok' field was: $replyOk - expected: true")
        }
    }

    fun checkReplyOk(reply: Map<String, Any>) {
        val replyOk = reply["ok"]
        checkReplyOk(replyOk == true)
    }
}
