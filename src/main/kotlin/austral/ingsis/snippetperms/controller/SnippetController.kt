package austral.ingsis.snippetperms.controller

import austral.ingsis.snippetperms.model.dto.SnippetCreate
import austral.ingsis.snippetperms.model.dto.SnippetDTO
import austral.ingsis.snippetperms.model.dto.SnippetLocation
import austral.ingsis.snippetperms.service.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Controller("/permissions/snippet")
class SnippetController {
    @Autowired
    private lateinit var snippetService: SnippetService

    @PostMapping("")
    fun snippet(
        @RequestBody snippet: SnippetCreate,
    ): ResponseEntity<SnippetDTO> {
        val resp = snippetService.createSnippet(snippet)
        return resp
    }

    @GetMapping("/{userId}")
    fun authToRead(
        @PathVariable("userId") userId: String,
    ): ResponseEntity<List<SnippetLocation>> {
        val resp = snippetService.getAllSnippetsAuthToRead(userId)
        return resp
    }

    @GetMapping("/shared/{userId}")
    fun sharedSnippets(
        @PathVariable("userId") userId: String,
    ): ResponseEntity<List<SnippetLocation>> {
        val resp = snippetService.getSnippetsThatHaveBeenSharedToUser(userId)
        return resp
    }

    @GetMapping("/written/{userId}")
    fun writtenSnippet(
        @PathVariable("userId") userId: String,
    ): ResponseEntity<List<SnippetLocation>> {
        val resp = snippetService.getSnippetsWritten(userId)
        return resp
    }

    @GetMapping("/single")
    fun getSnippet(
        @RequestParam(value = "snippetId") snippetId: Long,
        @RequestParam(value = "userId") userId: String,
    ): ResponseEntity<SnippetLocation> {
        val resp = snippetService.getSnippet(userId, snippetId)
        return resp
    }
}
