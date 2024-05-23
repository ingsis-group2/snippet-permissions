package austral.ingsis.snippetperms.model.dto

import java.time.LocalDateTime

data class UserCreateDTO(var id: String)

data class SnippetDTO(
    val id: Long,
    val title: String,
    val userId: String,
    val creationDate: LocalDateTime,
    val updateDate: LocalDateTime?,
    val container: String,
)

data class SnippetCreate(val title: String, val writer: String)

data class SnippetLocation(val id: Long, val container: String)
