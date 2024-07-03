package austral.ingsis.snippetperms.repository

import austral.ingsis.snippetperms.model.LintStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface LintStatusRepository : JpaRepository<LintStatus, Long> {
    @Query("SELECT ls FROM LintStatus ls WHERE ls.snippet.id = :snippetId")
    fun findBySnippetId(
        @Param("snippetId") snippetId: Long,
    ): LintStatus?

    @Query("SELECT ls FROM LintStatus ls WHERE ls.snippet.id IN :snippetIds")
    fun findLintStatusBySnippetIds(
        @Param("snippetIds") snippetIds: List<Long>,
    ): List<LintStatus>
}
