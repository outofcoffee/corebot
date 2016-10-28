package com.gatehill.rundeckbot.action

import com.gatehill.rundeckbot.config.ConfigService
import com.gatehill.rundeckbot.config.JobConfig
import java.util.*

enum class ActionType {
    TRIGGER,
    ENABLE,
    DISABLE,
    LOCK,
    UNLOCK,
    STATUS
}

interface ActionTemplate {
    val action: ActionType
    val job: JobConfig
    val tokens: Queue<String>
    val placeholderValues: Map<String, String>

    fun accept(input: String): Boolean

    /**
     * The response message sent when this actionType is fired.
     */
    fun buildMessage(): String {
        return "Just a min :clock1: I'm working on *${job.name}*..."
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
 * Represents a simple operation for a job.
 */
abstract class GenericActionTemplate : AbstractActionTemplate() {
    protected val jobNamePlaceholder = "job name"

    override lateinit var job: JobConfig

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
    override val action: ActionType = ActionType.TRIGGER
    override var job: JobConfig
    override val tokens: Queue<String>

    constructor(job: JobConfig) {
        this.job = job
        tokens = LinkedList(job.template!!.split("\\s".toRegex()))
    }

    override fun buildMessage(): String {
        val msg = StringBuilder()
        msg.append("Just a min :clock1: I'm running *${job.name}*")

        if (placeholderValues.size > 0) {
            msg.append(" with these options:")
            placeholderValues.forEach { arg -> msg.append("\r\n- ${arg.key}: _${arg.value}_") }
        } else {
            msg.append(".")
        }

        return msg.toString()
    }
}

class EnableActionTemplate : GenericActionTemplate() {
    override val action: ActionType = ActionType.ENABLE
    override val tokens: Queue<String> = LinkedList(listOf("enable", "{${jobNamePlaceholder}}"))
}

class DisableActionTemplate : GenericActionTemplate() {
    override val action: ActionType = ActionType.DISABLE
    override val tokens: Queue<String> = LinkedList(listOf("disable", "{${jobNamePlaceholder}}"))
}

class LockActionTemplate : GenericActionTemplate() {
    override val action: ActionType = ActionType.LOCK
    override val tokens: Queue<String> = LinkedList(listOf("lock", "{${jobNamePlaceholder}}"))
}

class UnlockActionTemplate : GenericActionTemplate() {
    override val action: ActionType = ActionType.UNLOCK
    override val tokens: Queue<String> = LinkedList(listOf("unlock", "{${jobNamePlaceholder}}"))
}

class StatusActionTemplate : GenericActionTemplate() {
    override val action: ActionType = ActionType.STATUS
    override val tokens: Queue<String> = LinkedList(listOf("status", "{${jobNamePlaceholder}}"))
}
