package austral.ingsis.snippetperms.service

import austral.ingsis.snippetperms.model.Snippet
import austral.ingsis.snippetperms.model.SnippetCreate
import austral.ingsis.snippetperms.model.SnippetLocation
import austral.ingsis.snippetperms.repository.SnippetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class SnippetService(
    @Autowired var snippetRepository: SnippetRepository,
) {
    /*
        Creates a snippet for an existing user
     */
    fun createSnippet(body: SnippetCreate): ResponseEntity<SnippetLocation> {
        val container = body.writer.hashCode() % 3
        val snippet =
            Snippet().apply {
                this.container = container.toString()
                this.writer = body.writer
                this.language = body.language
                this.extension = body.extension
            }

        val creation = snippetRepository.save(snippet)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(SnippetLocation(creation.id, creation.container))
    }

    fun getSnippet(snippetId: Long): ResponseEntity<SnippetLocation> {
        return when {
            snippetRepository.existsById(snippetId) -> {
                val snippet = snippetRepository.findById(snippetId).get()
                ResponseEntity.ok().body(SnippetLocation(snippet.id, snippet.container))
            }
            else -> ResponseEntity.notFound().build()
        }
    }
}
