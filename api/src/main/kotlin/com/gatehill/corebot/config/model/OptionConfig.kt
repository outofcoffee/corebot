package com.gatehill.corebot.config.model

/**
 * Models configuration for action options.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class OptionConfig(value: String?,
                   transformers: List<TransformType>?,
                   lockable: Boolean?) {

    val value: String
    val transformers: List<TransformType>
    val lockable: Boolean

    init {
        this.value = value ?: ""
        this.transformers = transformers ?: emptyList()
        this.lockable = lockable ?: false
    }

    override fun toString(): String {
        return "OptionConfig(value='$value', transformers=$transformers, lockable=$lockable)"
    }
}

/**
 * The supported transformers.
 */
enum class TransformType {
    LOWERCASE,
    UPPERCASE
}
