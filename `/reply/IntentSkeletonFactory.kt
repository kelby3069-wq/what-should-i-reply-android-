package com.replysense.app.ui.reply

import com.replysense.app.domain.emotion.GuidanceType

object IntentSkeletonFactory {

    fun create(guidance: GuidanceType): List<ReplyIntent> =
        when (guidance) {

            GuidanceType.MAINTAIN_MOMENTUM ->
                listOf(
                    ReplyIntent(
                        acknowledge = true,
                        boundary = false,
                        close = true
                    )
                )

            GuidanceType.DISENGAGE_AND_PROTECT ->
                listOf(
                    ReplyIntent(
                        acknowledge = false,
                        boundary = true,
                        close = true
                    )
                )

            else -> emptyList()
        }
}

data class ReplyIntent(
    val acknowledge: Boolean,
    val boundary: Boolean,
    val close: Boolean
)
