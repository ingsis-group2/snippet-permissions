package austral.ingsis.snippetperms.service

import austral.ingsis.snippetperms.SnippetPermsApplication
import austral.ingsis.snippetperms.model.Snippet
import austral.ingsis.snippetperms.model.SnippetCreate
import austral.ingsis.snippetperms.model.SnippetDTO
import austral.ingsis.snippetperms.repository.SnippetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SnippetService(
    @Autowired var snippetRepository: SnippetRepository,
    private val snippetPermsApplication: SnippetPermsApplication,
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
            snippet.updateDate
        )
    }

}
