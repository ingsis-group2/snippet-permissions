package austral.ingsis.snippetperms

import austral.ingsis.snippetperms.model.Snippet
import austral.ingsis.snippetperms.model.SnippetCreate
import austral.ingsis.snippetperms.model.SnippetDTO
import austral.ingsis.snippetperms.model.SnippetLocation
import austral.ingsis.snippetperms.repository.SnippetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class SnippetService(
    @Autowired var snippetRepository: SnippetRepository,
    @Autowired var lintStatusService: LintStatusService,
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
                this.name = body.name
            }

        val creation = snippetRepository.save(snippet)
        lintStatusService.createLintStatus(snippet)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(this.dto(creation))
    }

    fun addReader(
        snippetId: Long,
        userId: String,
        readerId: String,
    ): ResponseEntity<Boolean> {
        if (this.snippetRepository.existsById(snippetId)) {
            val snippet = this.snippetRepository.findById(snippetId).get()
            if (snippet.writer.equals(userId) && !snippet.writer.equals(readerId)) { // User must be writer and not a reader
                val readers = snippet.readers
                if (!readers.contains(readerId)) { // if reader is not in there
                    readers.add(readerId)
                    this.snippetRepository.save(snippet)
                }
                return ResponseEntity.ok().build()
            }
        }
        return ResponseEntity.badRequest().build()
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

    fun getSnippetFromWriterById(
        writerId: String,
        page: Int,
        size: Int,
    ): ResponseEntity<List<SnippetDTO>> {
        return when {
            page < 0 || size < 0 -> ResponseEntity.badRequest().build()
            else -> {
                val pageRequest: PageRequest = PageRequest.of(page, size, Sort.Direction.ASC, "id")
                val snippets = snippetRepository.findSnippetsByWriter(writerId, pageRequest)
                val snippetDTOs = snippets.map { s -> this.dto(s) }
                return ResponseEntity.ok(snippetDTOs.content)
            }
        }
    }

    fun getSnippetByReaderById(
        readerId: String,
        page: Int,
        size: Int,
    ): ResponseEntity<List<SnippetDTO>> {
        return when {
            page < 0 || size < 0 -> ResponseEntity.badRequest().build()
            else -> {
                val pageRequest: PageRequest = PageRequest.of(page, size, Sort.Direction.ASC, "id")
                val snippets = snippetRepository.findSnippetsByReader(readerId, pageRequest)
                val snippetDTOs = snippets.map { s -> this.dto(s) }
                return ResponseEntity.ok(snippetDTOs.content)
            }
        }
    }

    fun getSnippetByReadeAndWriterById(
        userId: String,
        page: Int,
        size: Int,
    ): ResponseEntity<List<SnippetDTO>> {
        return when {
            page < 0 || size < 0 -> ResponseEntity.badRequest().build()
            else -> {
                val pageRequest: PageRequest = PageRequest.of(page, size, Sort.Direction.ASC, "id")
                val snippets = snippetRepository.findSnippetsByReaderAndWriter(userId, pageRequest)
                val snippetDTOs = snippets.map { s -> this.dto(s) }
                return ResponseEntity.ok(snippetDTOs.content)
            }
        }
    }

    fun deleteSnippet(snippetId: Long): ResponseEntity<SnippetLocation> {
        return when {
            snippetRepository.existsById(snippetId) -> {
                val snippet = this.snippetRepository.findById(snippetId).get()
                this.snippetRepository.deleteById(snippetId)
                this.lintStatusService.deleteLintStatusBySnippetId(snippetId)
                return ResponseEntity
                    .ok(SnippetLocation(snippet.id, snippet.container))
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
