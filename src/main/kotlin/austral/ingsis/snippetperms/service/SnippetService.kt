package austral.ingsis.snippetperms.service

import austral.ingsis.snippetperms.model.Snippet
import austral.ingsis.snippetperms.model.SnippetCreate
import austral.ingsis.snippetperms.model.SnippetDTO
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
    fun createSnippet(body: SnippetCreate): ResponseEntity<SnippetDTO> {
        val snippet =
            Snippet().apply {
                this.container = "snippet"
                this.writer = body.writer
                this.language = body.language
                this.extension = body.extension
            }

        val creation = snippetRepository.save(snippet)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(this.dto(creation))
    }

    fun getSnippet(snippetId: Long): ResponseEntity<SnippetDTO> {
        return when {
            snippetRepository.existsById(snippetId) -> {
                val snippet = snippetRepository.findById(snippetId).get()
                ResponseEntity
                    .ok()
                    .body(this.dto(snippet))
            }
            else -> ResponseEntity.notFound().build()
        }
    }

    fun getSnippetLocation(snippetId: Long): ResponseEntity<SnippetLocation> {
        return when {
            this.snippetRepository.existsById(snippetId) -> {
                val snippet = this.snippetRepository.findById(snippetId).get()
                return ResponseEntity.ok()
                    .body(SnippetLocation(snippet.id, snippet.container))
            }
            else -> ResponseEntity.notFound().build()
        }
    }

    fun deleteSnippet(snippetId: Long): ResponseEntity<Void> {
        return when {
            snippetRepository.existsById(snippetId) -> {
                this.snippetRepository.deleteById(snippetId)
                return ResponseEntity.ok().build()
            }
            else -> ResponseEntity.notFound().build()
        }
    }

    private fun dto(snippet: Snippet): SnippetDTO {
        return SnippetDTO(
            snippet.id,
            snippet.container,
            snippet.writer,
            snippet.name,
            snippet.language,
            snippet.extension,
            snippet.readers.toList(),
            snippet.creationDate,
            snippet.updateDate,
        )
    }
}
