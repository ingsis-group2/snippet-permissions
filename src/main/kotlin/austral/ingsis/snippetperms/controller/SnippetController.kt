package austral.ingsis.snippetperms.controller

import austral.ingsis.snippetperms.model.SnippetCreate
import austral.ingsis.snippetperms.model.SnippetLocation
import austral.ingsis.snippetperms.service.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
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
    ): ResponseEntity<SnippetLocation> {
        val resp = snippetService.createSnippet(snippet)
        return resp
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
