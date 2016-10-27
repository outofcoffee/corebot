package com.gatehill.rundeckbot.chat

import com.gatehill.rundeckbot.config.ConfigService
import java.util.*

interface ActionTemplate {
    val action: ConfigService.TaskAction
    val job: ConfigService.JobConfig
    val tokens: Queue<String>
    val placeholderValues: Map<String, String>

    fun accept(input: String): Boolean

    /**
     * The response message sent when this action is fired.
     */
    fun buildMessage(): String {
        return "Just a min :clock1:, I'm working on ${job.name}..."
    }
}

/**
 * Parses tokens into placeholder values.
 */
abstract class AbstractActionTemplate : ActionTemplate {
    override val placeholderValues = HashMap<String, String>()

    override fun accept(input: String): Boolean {
        if (tokens.size == 0) return false
        val token = tokens.poll()

        val match = "\\{(.*)\\}".toRegex().matchEntire(token)
        if (null == match) {
            // syntactic sugar
            return (token == input)

        } else {
            // option placeholder
            placeholderValues[match.groupValues[1]] = input
            return true
        }
    }
}

/**
 * Represents a boolean operation (on/off) for a job.
 */
abstract class TogglableActionTemplate : AbstractActionTemplate() {
    protected val jobNamePlaceholder = "job name"

    override lateinit var job: ConfigService.JobConfig

    override fun accept(input: String): Boolean {
        val accepted = super.accept(input)

        // has job been set?
        if (accepted && tokens.isEmpty()) {
            val jobConfig = ConfigService().loadJobs()[placeholderValues[jobNamePlaceholder]]
            if (null != jobConfig) {
                job = jobConfig
            } else {
                return false
            }
        }

        return accepted
    }
}

/**
 * Template to trigger job execution.
 */
class TriggerActionTemplate : AbstractActionTemplate {
    override val action: ConfigService.TaskAction = ConfigService.TaskAction.TRIGGER
    override var job: ConfigService.JobConfig
    override val tokens: Queue<String>

    constructor(job: ConfigService.JobConfig) {
        this.job = job
        tokens = LinkedList(job.template!!.split("\\s".toRegex()))
    }

    override fun buildMessage(): String {
        val msg = StringBuilder()
        msg.append("OK :thumbsup: I'm running the *${job.name}* job")

        if (placeholderValues.size > 0) {
            msg.append(" with these options:")
            placeholderValues.forEach { arg -> msg.append("\r\n- ${arg.key}: _${arg.value}_") }
        } else {
            msg.append(".")
        }

        return msg.toString()
    }
}

class EnableActionTemplate : TogglableActionTemplate() {
    override val action: ConfigService.TaskAction = ConfigService.TaskAction.ENABLE
    override val tokens: Queue<String> = LinkedList(listOf("enable", "{${jobNamePlaceholder}}"))
}

class DisableActionTemplate : TogglableActionTemplate() {
    override val action: ConfigService.TaskAction = ConfigService.TaskAction.DISABLE
    override val tokens: Queue<String> = LinkedList(listOf("disable", "{${jobNamePlaceholder}}"))
}

class LockActionTemplate : TogglableActionTemplate() {
    override val action: ConfigService.TaskAction = ConfigService.TaskAction.LOCK
    override val tokens: Queue<String> = LinkedList(listOf("lock", "{${jobNamePlaceholder}}"))
}

class UnlockActionTemplate : TogglableActionTemplate() {
    override val action: ConfigService.TaskAction = ConfigService.TaskAction.UNLOCK
    override val tokens: Queue<String> = LinkedList(listOf("unlock", "{${jobNamePlaceholder}}"))
}
