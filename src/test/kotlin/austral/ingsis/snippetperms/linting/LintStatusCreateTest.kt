package austral.ingsis.snippetperms.linting

import austral.ingsis.snippetperms.SnippetTestApplication
import austral.ingsis.snippetperms.model.CreateLintStatusDTO
import austral.ingsis.snippetperms.model.LintStatus
import austral.ingsis.snippetperms.model.UpdateLintingStatusDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(classes = [SnippetTestApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LintStatusCreateTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    private var snippetId: Long = 0

    private lateinit var body: CreateLintStatusDTO

    private val mapper = ObjectMapper()

    data class LintStatusDTO
        @JsonCreator
        constructor(
            @JsonProperty("id") val id: Long,
            @JsonProperty("snippetId") val snippetId: Long,
            @JsonProperty("status") val status: String,
        )

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
        val postResponse =
            mockMvc.perform(
                post("http://localhost:8080/snippet")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(snippetCreateJson),
            ).andReturn()

        val responseBody = postResponse.response.contentAsString
        val snippet = mapper.readValue(responseBody, SnippetDTO::class.java)
        snippetId = snippet.id
        body = CreateLintStatusDTO(snippetId)
    }

    @AfterEach
    fun cleanup() {
        entityManager.createQuery("DELETE FROM LintStatus").executeUpdate()
        entityManager.createQuery("DELETE FROM Snippet").executeUpdate() // Ejecutar dentro de la transacción
        entityManager.flush() // Forzar el flush para asegurar que se apliquen los cambios
    }

    @Test
    fun `should success creating linting status by creating snippet`() {
        val result =
            mockMvc.perform(
                get("http://localhost:8080/lintStatus/$snippetId"),
            ).andReturn()

        assertEquals(HttpStatus.OK.value(), result.response.status)
        val responseBody = result.response.contentAsString
        val value = mapper.readValue(responseBody, LintStatusDTO::class.java)
        assertEquals(value.snippetId, snippetId)
        assertEquals(value.status, "pending")
    }

    @Test
    fun `should success updating linting status from snippet`() {
        val lintStatusResponse =
            mockMvc.perform(
                get("http://localhost:8080/lintStatus/$snippetId"),
            ).andReturn()
        val lintStatusBody = lintStatusResponse.response.contentAsString
        val oldLintStatus = mapper.readValue(lintStatusBody, LintStatusDTO::class.java)
        val updateStatus =
            UpdateLintingStatusDTO(
                id = oldLintStatus.id,
                reportList = emptyList(),
                errors = emptyList(),
            )
        val body = mapper.writeValueAsString(updateStatus)
        val result =
            mockMvc.perform(
                post("http://localhost:8080/lintStatus")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body),
            ).andReturn()
        assertEquals(HttpStatus.OK.value(), result.response.status)

        val resultResponse = result.response.contentAsString
        val possibleNewLintStatus = mapper.readValue(resultResponse, LintStatusDTO::class.java)
        val possibleNewLintStatusId = possibleNewLintStatus.id
        assertEquals(possibleNewLintStatusId, oldLintStatus.id)

        val query = entityManager.createQuery("SELECT l FROM LintStatus l WHERE l.id = :id", LintStatus::class.java)
        query.maxResults = 1
        query.setParameter("id", possibleNewLintStatus.id)
        val newLintStatus = query.resultList.firstOrNull() as LintStatus
        assertNotEquals(newLintStatus.status, oldLintStatus.status)
        assertEquals(newLintStatus.status, "compliant")
    }
}
