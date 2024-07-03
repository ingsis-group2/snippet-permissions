package austral.ingsis.snippetperms.controller

import austral.ingsis.snippetperms.model.CreateLintStatusDTO
import austral.ingsis.snippetperms.model.GetLintStatusDTO
import austral.ingsis.snippetperms.model.LintStatusDTO
import austral.ingsis.snippetperms.model.UpdateLintingStatusDTO
import austral.ingsis.snippetperms.service.LintStatusService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/lint_status")
class LintStatusController {
    @Autowired lateinit var lintStatusService: LintStatusService

    @PostMapping("/create")
    fun createLintStatus(
        @RequestBody createLintStatus: CreateLintStatusDTO,
    ): ResponseEntity<LintStatusDTO> {
        val resp = lintStatusService.createLintStatusFromSnippetId(createLintStatus.snippetId)
        return resp
    }

    @PostMapping("/update")
    fun updateLintStatus(
        @RequestBody updateLintStatus: UpdateLintingStatusDTO,
    ): ResponseEntity<LintStatusDTO> {
        val resp =
            lintStatusService.modifyLintStatus(
                updateLintStatus.id,
                updateLintStatus.reportList,
                updateLintStatus.errors,
            )
        return resp
    }

    @GetMapping("/getFromSnippetId")
    fun getLintStatusFromSnippetId(
        @RequestBody getLintStatus: GetLintStatusDTO,
    ): ResponseEntity<LintStatusDTO> {
        val resp = lintStatusService.getLintStatusBySnippetId(getLintStatus.snippetId)
        return resp
    }
}
