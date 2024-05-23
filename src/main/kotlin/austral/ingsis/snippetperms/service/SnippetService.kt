package austral.ingsis.snippetperms.service

import austral.ingsis.snippetperms.model.Snippet
import austral.ingsis.snippetperms.model.dto.SnippetCreate
import austral.ingsis.snippetperms.model.dto.SnippetDTO
import austral.ingsis.snippetperms.model.dto.SnippetLocation
import austral.ingsis.snippetperms.repository.SnippetRepository
import austral.ingsis.snippetperms.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SnippetService {

    @Autowired
    private lateinit var snippetRepository: SnippetRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    /*
        Creates a snippet for an existing user
     */
    fun createSnippet(body: SnippetCreate): ResponseEntity<SnippetDTO> {
        if (userRepository.existsById(body.writer)) {
            val snippet = Snippet()
            snippet.writer = body.writer
            val container = body.writer.hashCode() % 3   //how to create container
            snippet.container =  container.toString()
            val creation = snippetRepository.save(snippet)
            return ResponseEntity(
                SnippetDTO(
                    creation.id,
                    body.title,
                    creation.writer,
                    LocalDateTime.now(),
                    null,
                    creation.container
                ),
                HttpStatus.CREATED
            )
        }
        return ResponseEntity(HttpStatus.CONFLICT)

    }

    /*
        Returns snippets that user can read
     */
    fun getAllSnippetsAuthToRead(userId: String): ResponseEntity<List<SnippetLocation>> {
        if (userRepository.existsById(userId)) {
            val user = userRepository.findById(userId).get()
            val snippets = mutableListOf<SnippetLocation>()
            for (snippet in user.snippets) { // the ones that he can read
                snippets.add(
                    SnippetLocation(snippet.id, snippet.container)
                )
            }
            return ResponseEntity(snippets.toList(), HttpStatus.OK)
        }
        return ResponseEntity(HttpStatus.BAD_REQUEST)
    }

    /*
        Returns snippets that someone shared to user
     */
    fun getSnippetsThatHaveBeenSharedToUser(userId: String): ResponseEntity<List<SnippetLocation>> {
        if (userRepository.existsById(userId)) {
            val user = userRepository.findById(userId).get()
            val snippets = mutableListOf<SnippetLocation>()
            for (snippet in user.snippets) { // the ones that he can read
                when {
                    userId.equals(snippet.writer) -> snippets.add(SnippetLocation(snippet.id, snippet.container))
                    else -> continue
                }
            }
            return ResponseEntity(snippets.toList(), HttpStatus.OK)
        }
        return ResponseEntity(HttpStatus.BAD_REQUEST)
    }

    /*
        Returns all snippets that user wrote
     */
    fun getSnippetsWritten(userId: String): ResponseEntity<List<SnippetLocation>> {
        if (userRepository.existsById(userId)) {
            val user = userRepository.findById(userId).get()
            val snippets = mutableListOf<SnippetLocation>()
            for (snippet in snippetRepository.findSnippetsByWriter(user.id)) {
                snippets.add(
                    SnippetLocation(snippet.id, snippet.container)
                )
            }
            return ResponseEntity(snippets.toList(), HttpStatus.OK)
        }
        return ResponseEntity(HttpStatus.BAD_REQUEST)
    }


    /*
        Returns snippet reference just if user can read it
     */
    fun getSnippet(userId: String, snippetId: Long): ResponseEntity<SnippetLocation> {
        if (userRepository.existsById(userId) && snippetRepository.existsById(snippetId)) {
            val user = userRepository.findById(userId).get()
            val snippet = snippetRepository.findById(snippetId).get()
            return when {
                snippet.readers.contains(user) -> ResponseEntity(SnippetLocation(snippet.id, snippet.container), HttpStatus.OK)
                else -> ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }
        return ResponseEntity(HttpStatus.BAD_REQUEST)
    }
}
