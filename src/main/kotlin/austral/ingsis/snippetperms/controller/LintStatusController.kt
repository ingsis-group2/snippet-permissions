package austral.ingsis.snippetperms.controller

import austral.ingsis.snippetperms.model.LintStatusDTO
import austral.ingsis.snippetperms.model.UpdateLintingStatusDTO
import austral.ingsis.snippetperms.service.LintStatusService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/lintStatus")
class LintStatusController {
    @Autowired lateinit var lintStatusService: LintStatusService

    @PostMapping("")
    fun updateLintStatus(
        @RequestBody updateLintStatus: UpdateLintingStatusDTO,
    ): ResponseEntity<LintStatusDTO> {
        println("recieved request:")
        println(updateLintStatus)
        val resp =
            lintStatusService.modifyLintStatus(
                updateLintStatus.snippetId,
                updateLintStatus.reportList,
                updateLintStatus.errorList,
            )
        println("updating lint status for snippet ${updateLintStatus.snippetId} to ${resp.body?.status}")
        return resp
    }

    @GetMapping("/{id}")
    fun getLintStatusFromSnippetId(
        @PathVariable("id") id: Long,
    ): ResponseEntity<LintStatusDTO> {
        val resp = lintStatusService.getLintStatusBySnippetId(id)
        return resp
    }
}
