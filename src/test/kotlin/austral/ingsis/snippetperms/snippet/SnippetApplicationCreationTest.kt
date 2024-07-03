import austral.ingsis.snippetperms.SnippetTestApplication
import austral.ingsis.snippetperms.model.Snippet
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(classes = [SnippetTestApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SnippetApplicationCreationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @AfterEach
    fun cleanup() {
        entityManager.createQuery("DELETE FROM Snippet").executeUpdate() // Ejecutar dentro de la transacción
        entityManager.flush() // Forzar el flush para asegurar que se apliquen los cambios
    }

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

    private fun sendRequestAndReturn(snippetCreateJson: String): MvcResult {
        return mockMvc.perform(
            post("http://localhost:8080/snippet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(snippetCreateJson),
        ).andReturn()
    }

    @Test
    fun `should create snippet`() {
        val snippetCreateJson =
            """
            {
                "writer": "testWriter",
                "name": "testName",
                "language": "testLanguage",
                "extension": "txt"
            }
            """.trimIndent()

        val result = this.sendRequestAndReturn(snippetCreateJson)
        assertEquals(HttpStatus.CREATED.value(), result.response.status)
    }

    @Test
    fun `should fail create snippet for invalid body by missing extension field`() {
        val snippetCreateJson =
            """   
            {
                "writer": "testWriter",
                "name": "testName",
                "language": "testLanguage",
            }
            """.trimIndent()

        val result = this.sendRequestAndReturn(snippetCreateJson)
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.response.status)
    }

    @Test
    fun `should create and save snippet on data base`() {
        val snippetCreateJson =
            """   
            {
                "writer": "testWriter",
                "name": "testName",
                "language": "testLanguage",
                "extension": "txt"
            }
            """.trimIndent()

        val result = this.sendRequestAndReturn(snippetCreateJson)
        val content = result.response.contentAsString
        val objectMapper = ObjectMapper()
        val snippet = objectMapper.readValue(content, SnippetDTO::class.java)

        val id = snippet.id
        val query = entityManager.createQuery("SELECT s FROM Snippet s WHERE s.id = :id", Snippet::class.java)
        query.setParameter("id", id)
        val snippetFromDB = query.resultList.firstOrNull()

        assertNotNull(snippetFromDB)
        assertEquals(snippetFromDB?.writer, "testWriter")
        assertEquals(snippetFromDB?.name, "testName")
    }

    @Test
    fun `send bad request by missing extension field in body and not saved on database`() {
        val snippetCreateJson =
            """   
            {
                "writer": "testWriter",
                "name": "testName",
                "language": "testLanguage",
            }
            """.trimIndent()

        val result = this.sendRequestAndReturn(snippetCreateJson)
        val content = result.response.contentAsString
        assertTrue(content.isBlank())

        val queryCount = entityManager.createQuery("SELECT COUNT(s) FROM Snippet s", Long::class.java)
        val count = queryCount.singleResult
        assertEquals(0L, count)
    }
}
