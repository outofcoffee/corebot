package com.gatehill.rundeckbot.chat

import com.gatehill.rundeckbot.Config
import com.gatehill.rundeckbot.deployment.DeploymentService
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener
import org.apache.logging.log4j.LogManager

class ChatService {
    data class Task(val jobName: String,
                    val version: String,
                    val environment: String)

    private val logger = LogManager.getLogger(ChatService::class.java)!!
    private val config = Config()
    private val deploymentService = DeploymentService()

    fun listenForEvents() {
        val session = SlackSessionFactory.createWebSocketSlackSession(config.chat.authToken)
        session.connect()
        session.addMessagePostedListener(SlackMessagePostedListener { event, theSession ->
            // filter out messages from other channels
            val theChannel = theSession.findChannelByName(config.chat.channelName)
            if (theChannel.id != event.channel.id) {
                return@SlackMessagePostedListener
            }

            // ignore own messages
            if (theSession.sessionPersona().id == event.sender.id) {
                return@SlackMessagePostedListener
            }

            try {
                val messageContent = event.messageContent
                val splitCmd = messageContent.split("\\s".toRegex())

                // is it addressed to the bot?
                if (splitCmd.size > 0 && splitCmd[0] == "<@${session.sessionPersona().id}>") {
                    val task = parseMessage(splitCmd)
                    if (null != task) {
                        logger.info("Handling command '{}' from {}", messageContent, event.sender.userName)
                        handleTask(theSession, event, task)
                    } else {
                        logger.warn("Skipped handling command '{}' from {}", messageContent, event.sender.userName)
                        printUsage(event, session)
                    }
                }

            } catch(e: Exception) {
                printUsage(event, session)
                logger.error("Error parsing message event: {}", event, e)
                return@SlackMessagePostedListener
            }
        })
    }

    private fun handleTask(session: SlackSession, event: SlackMessagePosted, task: Task) {
        logger.info("Handling task: {}", task)

        session.sendMessage(event.channel,
                "OK, I'm deploying ${task.jobName} version ${task.version} to ${task.environment}.")

        val jobArgs = mapOf(
                Pair("environment", task.environment),
                Pair("version", task.version)
        )

        val future = deploymentService.triggerJob(task.jobName, jobArgs)

        future.whenComplete { executionDetails, throwable ->
            if (future.isCompletedExceptionally) {
                session.sendMessage(event.channel,
                        "Hmm, something went wrong :(\r\n```${throwable.message}```")
            } else {
                session.sendMessage(event.channel,
                        "Status of job is: ${executionDetails.status}\r\nDetails: ${executionDetails.permalink}")
            }
        }
    }

    private fun printUsage(event: SlackMessagePosted, session: SlackSession) {
        session.sendMessage(event.channel, """Sorry, I didn't understand :slightly_frowning_face: Try something like this:
_@${session.sessionPersona().userName} *deploy* <project name> <version> *to* <environment>_
""")
    }

    fun parseMessage(splitCmd: List<String>): Task? {
        if (splitCmd.size < 6) {
            return null
        }

        if (splitCmd[1] != "deploy") {
            return null
        }

        val jobName = splitCmd[2]
        val version = splitCmd[3]

        if (splitCmd[4] != "to") {
            return null
        }

        val environment = splitCmd[5]

        return Task(jobName, version, environment)
    }
}
