package austral.ingsis.snippetperms.model

import java.time.LocalDateTime

data class SnippetCreate(
    val writer: String,
    val name: String,
    val language: String,
    val extension: String,
)

data class SnippetDTO(
    val id: Long,
    val container: String,
    val writer: String,
    val name: String,
    val language: String,
    val extension: String,
    val readers: List<String>,
    val creationDate: LocalDateTime,
    val updateDate: LocalDateTime?,
)

data class SnippetLocation(
    val id: Long,
    val container: String,
)

data class NewReaderForm(
    val snippetId: Long,
    val userId: String,
    val readerId: String,
)

data class GetterForm(
    val userId: String,
    val page: Int,
    val size: Int,
)

data class SnippetUpdate(val reader: String)

data class LintStatusDTO(
    val id: Long,
    val snippetId: Long,
    val status: String,
)

data class CreateLintStatusDTO(
    val snippetId: Long,
)

data class UpdateLintingStatusDTO(
    val id: Long,
    val reportList: List<String>,
    val errors: List<String>,
)

data class GetLintStatusDTO(
    val snippetId: Long,
)
