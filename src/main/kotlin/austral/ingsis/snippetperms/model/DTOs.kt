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

data class SnippetUpdate(val reader: String)
