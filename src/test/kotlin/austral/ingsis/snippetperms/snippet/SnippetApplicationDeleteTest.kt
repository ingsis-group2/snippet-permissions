package austral.ingsis.snippetperms.snippet

import austral.ingsis.snippetperms.SnippetTestApplication
import austral.ingsis.snippetperms.model.Snippet
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(classes = [SnippetTestApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SnippetApplicationDeleteTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    data class SnippetDTO
        @JsonCreator
        constructor(
            @JsonProperty("id") val id: Long,
            @JsonProperty("container") val container: String,
            @JsonProperty("writer") val writer: String,
            @JsonProperty("name") val name: String,
            @JsonProperty("language") val language: String,
            @JsonProperty("extension") val extension: String,
            @JsonProperty("readers") val readers: List<String>,
            @JsonProperty("creationDate") val creationDate: String,
            @JsonProperty("updateDate") val updateDate: String?,
        )

    @BeforeEach
    fun beforeEach() {
        val snippetCreateJson =
            """
            {
                "writer": "testWriter",
                "name": "testName",
                "language": "testLanguage",
                "extension": "txt"
            }
            """.trimIndent()
        mockMvc.perform(
            post("http://localhost:8080/snippet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(snippetCreateJson),
        ).andReturn()
    }

    private fun sendGetSnippetById(id: Long): MvcResult {
        return mockMvc.perform(
            get("http://localhost:8080/snippet/$id")
                .contentType(MediaType.APPLICATION_JSON),
        ).andReturn()
    }

    private fun getSnippetByIdFromDB(id: Long): Snippet? {
        val query = entityManager.createQuery("SELECT s FROM Snippet s WHERE s.id = :id", Snippet::class.java)
        query.setParameter("id", id)
        return query.resultList.firstOrNull()
    }

    @AfterEach
    fun cleanup() {
        entityManager.createQuery("DELETE FROM LintStatus").executeUpdate()
        entityManager.createQuery("DELETE FROM Snippet").executeUpdate()
        entityManager.flush()
    }

    @Test
    fun `should delete a snippet by id that exists on database`() {
        val query = entityManager.createQuery("SELECT s FROM Snippet s ORDER BY s.id ASC", Snippet::class.java)
        query.maxResults = 1
        val snippetFromDB = query.resultList.firstOrNull()
        assertNotNull(snippetFromDB) // snippet exists on database
        val id = snippetFromDB?.id as Long

        val result =
            mockMvc.perform(
                delete("http://localhost:8080/snippet/$id"),
            ).andReturn()
        assertEquals(HttpStatus.OK.value(), result.response.status)

        val nonExistingSnippetResponse = this.sendGetSnippetById(id) // not found by application
        assertEquals(HttpStatus.NOT_FOUND.value(), nonExistingSnippetResponse.response.status)

        val nonExistingSnippetFromDb = this.getSnippetByIdFromDB(id) // not found on DB
        assertNull(nonExistingSnippetFromDb)
    }

    @Test
    fun `should fail delete a snippet by id that not exists on database`() {
        val firstQuery = entityManager.createQuery("SELECT COUNT(s) FROM Snippet s", Long::class.javaObjectType)
        val firstCount: Long = firstQuery.singleResult
        assertEquals(firstCount, 1) // initial snippet qty

        val maxIdQuery = entityManager.createQuery("SELECT MAX(s.id) FROM Snippet s")
        val maxId = maxIdQuery.singleResult as Long
        val nonExistentId = maxId + 1000L // for sure id does not exists

        val result =
            mockMvc.perform(
                delete("http://localhost:8080/snippet/$nonExistentId"),
            ).andReturn()
        assertEquals(HttpStatus.NOT_FOUND.value(), result.response.status)

        val secondQuery = entityManager.createQuery("SELECT COUNT(s) FROM Snippet s", Long::class.javaObjectType)
        val secondCount: Long = secondQuery.singleResult
        assertEquals(secondCount, firstCount) // qty still the same in db
    }
}
