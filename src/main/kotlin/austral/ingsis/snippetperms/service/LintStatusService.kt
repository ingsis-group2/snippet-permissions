package austral.ingsis.snippetperms.service

import austral.ingsis.snippetperms.model.LintStatus
import austral.ingsis.snippetperms.model.LintStatusDTO
import austral.ingsis.snippetperms.model.Snippet
import austral.ingsis.snippetperms.repository.LintStatusRepository
import austral.ingsis.snippetperms.repository.SnippetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class LintStatusService(
    @Autowired var lintStatusRepository: LintStatusRepository,
    @Autowired var snippetRepository: SnippetRepository,
) {
    fun createLintStatus(snippet: Snippet): ResponseEntity<LintStatusDTO> {
        // checking if there is not a lint status already created for this snippet
        if (getLintStatusBySnippetId(snippet.id).statusCode.is2xxSuccessful) {
            return ResponseEntity.badRequest().build()
        }
        val lintStatus =
            LintStatus().apply {
                this.snippet = snippet
                this.status = "pending"
            }
        val creation = lintStatusRepository.save(lintStatus)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(this.lintStatusDTO(creation))
    }

    fun createLintStatusFromSnippetId(snippetId: Long): ResponseEntity<LintStatusDTO> {
        // get snippet from id
        val snippet =
            snippetRepository.findById(snippetId)
                .orElseThrow { RuntimeException("Snippet not found with id $snippetId") }
        return createLintStatus(snippet)
    }

    fun deleteLintStatusById(lintStatusId: Long): ResponseEntity<Boolean> {
        // check if lint status exists
        if (lintStatusRepository.existsById(lintStatusId)) {
            lintStatusRepository.deleteById(lintStatusId)
            return ResponseEntity.ok().build()
        }
        return ResponseEntity.badRequest().build()
    }

    fun deleteLintStatusBySnippetId(snippetId: Long): ResponseEntity<Boolean> {
        // check if there is a lint status that belongs to the snippet
        val lintStatus = lintStatusRepository.findBySnippetId(snippetId)
        if (lintStatus != null) {
            lintStatusRepository.deleteById(lintStatus.id)
            return ResponseEntity.ok().build()
        }
        return ResponseEntity.badRequest().build()
    }

    fun getLintStatusBySnippetId(snippetId: Long): ResponseEntity<LintStatusDTO> {
        val lintStatus = lintStatusRepository.findBySnippetId(snippetId)
        if (lintStatus != null) {
            return ResponseEntity.ok().body(this.lintStatusDTO(lintStatus))
        }
        return ResponseEntity.badRequest().build()
    }

    fun getLintStatusBySnippetsIds(snippetIds: List<Long>): ResponseEntity<List<LintStatusDTO>> {
        val lintStatuses = lintStatusRepository.findLintStatusBySnippetIds(snippetIds)
        return ResponseEntity.ok().body(lintStatuses.map { this.lintStatusDTO(it) })
    }

    fun modifyLintStatus(
        id: Long,
        reportList: List<String>,
        errors: List<String>,
    ): ResponseEntity<LintStatusDTO> {
        val lintStatus =
            lintStatusRepository.findById(id)
                .orElseThrow { RuntimeException("LintStatus not found with id $id") }
        var newStatus = "compliant"
        if (errors.isNotEmpty()) {
            newStatus = "failed"
        } else if (reportList.isNotEmpty()) {
            newStatus = "not compliant"
        }
        lintStatus.status = newStatus
        val updatedLintStatus = lintStatusRepository.save(lintStatus)
        return ResponseEntity.ok()
            .body(this.lintStatusDTO(updatedLintStatus))
    }

    private fun lintStatusDTO(lintStatus: LintStatus): LintStatusDTO {
        return LintStatusDTO(
            lintStatus.id,
            getSnippetId(lintStatus.snippet),
            lintStatus.status,
        )
    }

    private fun getSnippetId(snippet: Snippet?): Long {
        if (snippet != null) {
            return snippet.id
        }
        return -1
    }
}
