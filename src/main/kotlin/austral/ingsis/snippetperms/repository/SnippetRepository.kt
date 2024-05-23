package austral.ingsis.snippetperms.repository

import austral.ingsis.snippetperms.model.Snippet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SnippetRepository : JpaRepository<Snippet, Long> {
    @Query("SELECT s FROM Snippet s WHERE s.writer = :writer")
    fun findSnippetsByWriter(@Param("writer") writer: String): List<Snippet>
}