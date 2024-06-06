package austral.ingsis.snippetperms.model

data class SnippetCreate(val writer: String)

data class SnippetLocation(val id: Long, val container: String)

data class SnippetUpdate(val reader: String)
