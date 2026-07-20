package com.example.c001apk.compose.logic.model

import com.google.gson.annotations.SerializedName

data class EventDetailResponse(
    val status: Int?,
    val error: Int?,
    val message: String?,
    val data: Data?,
) {
    data class Data(
        val id: String?,
        val title: String?,
        @SerializedName("notice_rule")
        val noticeRule: String?,
        @SerializedName("action_url")
        val actionUrl: String?,
        val logo: String?,
        @SerializedName("stage_status")
        val stageStatus: Int?,
        @SerializedName("time_reg_start")
        val registrationStart: Long?,
        @SerializedName("time_reg_end")
        val registrationEnd: Long?,
        @SerializedName("time_end")
        val eventEnd: Long?,
        val sponsorUser: List<SponsorUser>?,
        val sponsorPrize: List<SponsorPrize>?,
        val tabList: List<Tab>?,
    )

    data class SponsorUser(
        val uid: String?,
        val displayUsername: String?,
        val userAvatar: String?,
    )

    data class SponsorPrize(
        val title: String?,
        val logo: String?,
    )

    data class Tab(
        val title: String?,
        val url: String?,
    )
}
