package com.example.can301_cw.data

import com.example.can301_cw.model.*

val mockMemoDetailData = ApiResponse(
    mostPossibleCategory = "Email",
    schedule = Schedule(
        title = "Work placement follow - up task",
        category = "Academic Task",
        tasks = listOf(
            ScheduleTask(
                startTime = "2025-12-01T19:00:00+08:00",
                endTime = "",
                people = listOf("Shuo Ma"),
                theme = "Check work placement comments on e - Bridge",
                coreTasks = listOf(
                    "Access e - Bridge platform",
                    "View academic adviser/work placement officer comments"
                ),
                position = emptyList(),
                tags = listOf("work placement", "e - Bridge", "academic"),
                category = "Academic Task",
                suggestedActions = listOf(
                    "Log in to e - Bridge",
                    "Navigate to the work placement section",
                    "Review comments from adviser/officer"
                )
            )
        )
    ),
    information = Information(
        title = "Work placement status updated notification",
        informationItems = listOf(
            InformationItem(
                id = 1,
                header = "Sender",
                content = "Student Records and Progression Team, Registry Office, Xi'an Jiaotong - Liverpool University",
                node = null
            ),
            InformationItem(
                id = 2,
                header = "Recipient",
                content = "Shuo Ma (马硕)",
                node = null
            ),
            InformationItem(
                id = 3,
                header = "Subject",
                content = "Work placement status updated",
                node = null
            ),
            InformationItem(
                id = 4,
                header = "Content",
                content = "Your work placement report has been approved and your work placement status has been updated to 'Completed'. You may find the comments from your academic adviser / work placement officer on e - Bridge. This is an automatically generated email. Please do not reply to it.",
                node = null
            )
        ),
        relatedItems = emptyList(),
        summary = "Notification from Xi'an Jiaotong - Liverpool University's Registry Office informing Shuo Ma that their work placement report is approved, with status updated to 'Completed'. Advises checking e - Bridge for comments from academic adviser/work placement officer. Email is auto - generated, do not reply.",
        tags = listOf(
            "work placement",
            "approved",
            "completed",
            "university",
            "email",
            "notification",
            "academic",
            "e - Bridge"
        )
    )
)
