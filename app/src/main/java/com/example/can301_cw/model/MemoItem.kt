package com.example.can301_cw.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.UUID
import java.util.Date

/**
 * Data model for MemoItem, corresponding to iOS MemoItemModel.
 */
@Entity(tableName = "memos")
data class MemoItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    var imagePath: String? = null,
    var recognizedText: String = "",
    var userInputText: String = "",
    var title: String = "",
    var tags: MutableList<String> = mutableListOf(),
    var createdAt: Date = Date(),
    var scheduledDate: Date? = null,
    var source: String = "",
    
    // API Response related fields
    // In iOS this is Data?, here we can store the parsed object directly or the raw string/bytes if needed.
    // Keeping it simple with the object for now as per "data structure" request.
    var apiResponse: ApiResponse? = null,
    var isAPIProcessing: Boolean = false,
    var apiProcessedAt: Date? = null,
    var hasAPIResponse: Boolean = false
) {
    @Ignore
    var imageData: ByteArray? = null

    // Helper to check equality similar to iOS areEqual
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MemoItem

        if (id != other.id) return false
        if (imagePath != other.imagePath) return false
        if (recognizedText != other.recognizedText) return false
        if (userInputText != other.userInputText) return false
        if (title != other.title) return false
        if (tags != other.tags) return false
        if (createdAt != other.createdAt) return false
        if (scheduledDate != other.scheduledDate) return false
        if (source != other.source) return false
        if (apiResponse != other.apiResponse) return false
        if (isAPIProcessing != other.isAPIProcessing) return false
        if (apiProcessedAt != other.apiProcessedAt) return false
        if (hasAPIResponse != other.hasAPIResponse) return false
        
        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (imagePath?.hashCode() ?: 0)
        result = 31 * result + recognizedText.hashCode()
        result = 31 * result + userInputText.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (scheduledDate?.hashCode() ?: 0)
        result = 31 * result + source.hashCode()
        result = 31 * result + (apiResponse?.hashCode() ?: 0)
        result = 31 * result + isAPIProcessing.hashCode()
        result = 31 * result + (apiProcessedAt?.hashCode() ?: 0)
        result = 31 * result + hasAPIResponse.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * Main API Response model
 */
data class ApiResponse(
    val mostPossibleCategory: String,
    val information: Information,
    var schedule: Schedule
) {
    val allTags: Set<String>
        get() {
            val tags = mutableSetOf<String>()
            tags.addAll(information.tags)
            schedule.tasks.forEach { task ->
                tags.addAll(task.tags)
            }
            return tags
        }

    val allTasks: List<ScheduleTask>
        get() = schedule.tasks
}

/**
 * Information model
 */
data class Information(
    val title: String,
    val informationItems: List<InformationItem>,
    val relatedItems: List<String>,
    val summary: String,
    val tags: List<String>
)

/**
 * Information Item model
 */
data class InformationItem(
    val id: Int,
    val header: String,
    val content: String,
    val node: InformationNode?
)

/**
 * Information Node model
 */
data class InformationNode(
    val targetId: Int,
    val relationship: String
)

/**
 * Schedule model
 */
data class Schedule(
    val title: String,
    val category: String,
    var tasks: List<ScheduleTask>
)

/**
 * Schedule Task model
 */
data class ScheduleTask(
    val startTime: String,
    val endTime: String,
    val people: List<String>,
    val theme: String,
    val coreTasks: List<String>,
    val position: List<String>,
    val tags: List<String>,
    val category: String,
    val suggestedActions: List<String>,
    val id: String = UUID.randomUUID().toString(),
    var taskStatus: TaskStatus = TaskStatus.PENDING
)

/**
 * Task Status Enum
 */
enum class TaskStatus(val label: String) {
    PENDING("pending"),
    COMPLETED("completed"),
    IGNORED("ignored");

    companion object {
        fun fromLabel(label: String): TaskStatus {
            return entries.find { it.label == label } ?: PENDING
        }
    }
}
