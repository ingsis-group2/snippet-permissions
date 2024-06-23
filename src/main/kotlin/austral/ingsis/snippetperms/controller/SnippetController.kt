package austral.ingsis.snippetperms.controller

import austral.ingsis.snippetperms.model.SnippetCreate
import austral.ingsis.snippetperms.model.SnippetDTO
import austral.ingsis.snippetperms.model.SnippetLocation
import austral.ingsis.snippetperms.service.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.filter.RequestContextFilter
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

@Controller
@RequestMapping("/snippet")
class SnippetController {
    @Autowired
    private lateinit var requestMappingHandlerAdapter: RequestMappingHandlerAdapter

    @Autowired
    private lateinit var requestContextFilter: RequestContextFilter

    @Autowired
    private lateinit var snippetService: SnippetService

    @PostMapping("")
    fun snippet(
        @RequestBody snippet: SnippetCreate,
    ): ResponseEntity<SnippetDTO> {
        val resp = snippetService.createSnippet(snippet)
        return resp
    }

    @GetMapping("/{id}")
    fun getSnippetById(
        @PathVariable("id") snippetId: Long,
    ): ResponseEntity<SnippetDTO> {
        return this.snippetService.getSnippet(snippetId)
    }

    @GetMapping("/location/{snippetId}")
    fun getSnippetLocationById(
        @PathVariable("snippetId") snippetId: Long,
    ): ResponseEntity<SnippetLocation> {
        return this.snippetService.getSnippetLocation(snippetId)
    }

    @GetMapping("/byWriter/{userId}/{page}/{size}")
    fun getSnippetByWriter(
        @PathVariable(value = "userId", required = true) writerId: String,
        @PathVariable(value = "page", required = true) page: Int,
        @PathVariable(value = "size", required = true) size: Int,
    ): ResponseEntity<Page<SnippetDTO>> {
        return this.snippetService.getSnippetFromWriterById(writerId, page, size)
    }

    @GetMapping("/byReader/{userId}/{page}/{size}")
    fun getSnippetByReader(
        @PathVariable(value = "userId", required = true) readerId: String,
        @PathVariable(value = "page", required = true) page: Int,
        @PathVariable(value = "size", required = true) size: Int,
    ): ResponseEntity<Page<SnippetDTO>> {
        return this.snippetService.getSnippetByReaderById(readerId, page, size)
    }

    @GetMapping("/byReaderAndWriter/{userId}/{page}/{size}")
    fun getSnippetByReaderOrWriter(
        @PathVariable(value = "userId", required = true) userId: String,
        @PathVariable(value = "page", required = true) page: Int,
        @PathVariable(value = "size", required = true) size: Int,
    ): ResponseEntity<Page<SnippetDTO>> {
        return this.snippetService.getSnippetByReadeAndWriterById(userId, page, size)
    }

    @DeleteMapping("/{id}")
    fun deleteSnippetById(
        @PathVariable("id") snippetId: Long,
    ): ResponseEntity<SnippetLocation> {
        return this.snippetService.deleteSnippet(snippetId)
    }

    @GetMapping("/greet")
    @ResponseBody
    fun greet(): String {
        return "hi, you found me stranger"
    }

    @GetMapping("/greetBack")
    fun greetBack(): ResponseEntity<String> {
        return ResponseEntity("greeting back", HttpStatus.OK)
    }
}
