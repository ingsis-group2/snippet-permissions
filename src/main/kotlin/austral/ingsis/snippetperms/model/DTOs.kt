package austral.ingsis.snippetperms.model

data class SnippetCreate(
    val writer: String,
    val name: String,
    val language: String,
    val extension: String,
)

data class SnippetLocation(val id: Long, val container: String)

data class SnippetUpdate(val reader: String)
