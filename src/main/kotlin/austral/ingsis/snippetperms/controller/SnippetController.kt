package austral.ingsis.snippetperms.controller

import austral.ingsis.snippetperms.model.GetterForm
import austral.ingsis.snippetperms.model.NewReaderForm
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
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/snippet")
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

    @PostMapping("/addReader")
    fun addReaderIntoSnippet(
        @RequestBody form: NewReaderForm,
    ): ResponseEntity<Boolean> {
        return this.snippetService.addReader(form.snippetId, form.userId, form.readerId)
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

    @PostMapping("/byWriter")
    fun getSnippetByWriter(
        @RequestBody form: GetterForm,
    ): ResponseEntity<Page<SnippetDTO>> {
        return this.snippetService.getSnippetFromWriterById(form.userId, form.page, form.size)
    }

    @PostMapping("/byReader")
    fun getSnippetByReader(
        @RequestBody form: GetterForm,
    ): ResponseEntity<Page<SnippetDTO>> {
        return this.snippetService.getSnippetByReaderById(form.userId, form.page, form.size)
    }

    @PostMapping("/byReaderAndWriter")
    fun getSnippetByReaderOrWriter(
        @RequestBody form: GetterForm,
    ): ResponseEntity<Page<SnippetDTO>> {
        return this.snippetService.getSnippetByReadeAndWriterById(form.userId, form.page, form.size)
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
