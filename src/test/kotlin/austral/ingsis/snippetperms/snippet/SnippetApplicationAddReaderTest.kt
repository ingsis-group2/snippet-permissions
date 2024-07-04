package austral.ingsis.snippetperms.snippet

import austral.ingsis.snippetperms.SnippetTestApplication
import austral.ingsis.snippetperms.model.NewReaderForm
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(classes = [SnippetTestApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SnippetApplicationAddReaderTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @PersistenceContext
    private lateinit var entityManager: EntityManager

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
        sendRequestAndReturn(snippetCreateJson, "snippet")
    }

    @AfterEach
    fun cleanup() {
        entityManager.createQuery("DELETE FROM LintStatus").executeUpdate()
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

    private fun sendRequestAndReturn(
        snippetCreateJson: String,
        url: String,
    ): MvcResult {
        return mockMvc.perform(
            post("http://localhost:8080/$url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(snippetCreateJson),
        ).andReturn()
    }

    @Test
    fun `should add reader into existing snippet on database`() {
        val firstQuery = entityManager.createQuery("SELECT s FROM Snippet s ORDER BY s.id ASC", Snippet::class.java)
        firstQuery.maxResults = 1
        val snippetFromDB = firstQuery.resultList.firstOrNull()
        val id = snippetFromDB?.id
        val readerForm =
            NewReaderForm(
                id!!,
                "testWriter",
                "testReader",
            )
        val objectMapper = ObjectMapper()
        val jsonContent = objectMapper.writeValueAsString(readerForm)
        val result = this.sendRequestAndReturn(jsonContent, "/snippet/addReader")
        assertEquals(HttpStatus.OK.value(), result.response.status)

        val secondQuery = entityManager.createQuery("SELECT s FROM Snippet s WHERE s.id = :id", Snippet::class.java)
        secondQuery.maxResults = 1
        secondQuery.setParameter("id", id)
        val updatedSnippet = secondQuery.resultList.firstOrNull()
        val readers = updatedSnippet?.readers?.toList() as List<String>

        assertNotNull(readers)
        assertEquals(readers.size, 1)
        assertTrue(readers.contains("testReader"))
    }

    @Test
    fun `should fail add reader into existing snippet on database where reader is writer`() {
        val firstQuery = entityManager.createQuery("SELECT s FROM Snippet s ORDER BY s.id ASC", Snippet::class.java)
        firstQuery.maxResults = 1
        val snippetFromDB = firstQuery.resultList.firstOrNull()
        val id = snippetFromDB?.id
        val readerForm =
            NewReaderForm(
                id!!,
                "testWriter",
                "testWriter",
            )
        val objectMapper = ObjectMapper()
        val jsonContent = objectMapper.writeValueAsString(readerForm)
        val result = this.sendRequestAndReturn(jsonContent, "/snippet/addReader")
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.response.status)

        val secondQuery = entityManager.createQuery("SELECT s FROM Snippet s WHERE s.id = :id", Snippet::class.java)
        secondQuery.maxResults = 1
        secondQuery.setParameter("id", id)
        val updatedSnippet = secondQuery.resultList.firstOrNull()
        val readers = updatedSnippet?.readers?.toList() as List<String>

        assertNotNull(readers)
        assertTrue(readers.isEmpty())
    }

    @Test
    fun `should fail add reader into non existing snippet on database`() {
        val maxIdQuery = entityManager.createQuery("SELECT MAX(s.id) FROM Snippet s")
        val maxId = maxIdQuery.singleResult as Long
        val nonExistentId = maxId + 1000L // for sure id does not exists
        val readerForm =
            NewReaderForm(
                nonExistentId,
                "testWriter",
                "testWriter",
            )
        val objectMapper = ObjectMapper()
        val jsonContent = objectMapper.writeValueAsString(readerForm)
        val result = this.sendRequestAndReturn(jsonContent, "/snippet/addReader")
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.response.status)
    }

    @Test
    fun `should fail add reader into snippet with invalid writer`() {
        val firstQuery = entityManager.createQuery("SELECT s FROM Snippet s ORDER BY s.id ASC", Snippet::class.java)
        firstQuery.maxResults = 1
        val snippetFromDB = firstQuery.resultList.firstOrNull()
        val id = snippetFromDB?.id
        val readerForm =
            NewReaderForm(
                id!!,
                "testReader",
                "testReader",
            )
        val objectMapper = ObjectMapper()
        val jsonContent = objectMapper.writeValueAsString(readerForm)
        val result = this.sendRequestAndReturn(jsonContent, "/snippet/addReader")
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.response.status)

        val secondQuery = entityManager.createQuery("SELECT s FROM Snippet s WHERE s.id = :id", Snippet::class.java)
        secondQuery.maxResults = 1
        secondQuery.setParameter("id", id)
        val updatedSnippet = secondQuery.resultList.firstOrNull()
        val readers = updatedSnippet?.readers?.toList() as List<String>

        assertNotNull(readers)
        assertTrue(readers.isEmpty())
    }
}
