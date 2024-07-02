package austral.ingsis.snippetperms.snippet

import austral.ingsis.snippetperms.SnippetTestApplication
import austral.ingsis.snippetperms.model.GetterForm
import austral.ingsis.snippetperms.model.NewReaderForm
import austral.ingsis.snippetperms.model.Snippet
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(classes = [SnippetTestApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SnippetApplicationGetterlTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @BeforeEach
    fun beforeEach() {
        repeat(3) {
            val snippetCreateJson = """
            {
                "writer": "testWriter",
                "name": "testName${it + 1}",
                "language": "testLanguage",
                "extension": "txt"
            }
            """.trimIndent()
            sendRequestAndReturn(snippetCreateJson, "snippet")
        }
    }

    @AfterEach
    fun cleanup() {
        entityManager.createQuery("DELETE FROM Snippet").executeUpdate() // Ejecutar dentro de la transacción
        entityManager.flush() // Forzar el flush para asegurar que se apliquen los cambios
    }

    data class SnippetDTO @JsonCreator constructor(
        @JsonProperty("id") val id: Long,
        @JsonProperty("container") val container: String,
        @JsonProperty("writer") val writer: String,
        @JsonProperty("name") val name: String,
        @JsonProperty("language") val language: String,
        @JsonProperty("extension") val extension: String,
        @JsonProperty("readers") val readers: List<String>,
        @JsonProperty("creationDate") val creationDate: String,
        @JsonProperty("updateDate") val updateDate: String?
    )

    private fun sendRequestAndReturn(snippetCreateJson: String, url: String): MvcResult {
        return mockMvc.perform(
            post("http://localhost:8080/$url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(snippetCreateJson)
        ).andReturn()
    }

    private fun sendGetRequestAndReturn(url: String): MvcResult {
        return mockMvc.perform(
                get("http://localhost:8080/$url")
                    .contentType(MediaType.APPLICATION_JSON)
                ).andReturn()
    }

    private fun addReaderIntoSnippet(snippetId: Long, writerId: String, readerId: String) {
        val readerForm = NewReaderForm(
            snippetId,
            writerId,
            readerId
        )
        val objectMapper = ObjectMapper()
        val jsonContent = objectMapper.writeValueAsString(readerForm)
        this.sendRequestAndReturn(jsonContent, "/snippet/addReader")
    }

    private fun getSnippetFormDB(snippetId: Long): Snippet {
        val second_query = entityManager.createQuery("SELECT s FROM Snippet s WHERE s.id = :id", Snippet::class.java)
        second_query.maxResults = 1
        second_query.setParameter("id", snippetId)
        return second_query.resultList.firstOrNull() as Snippet
    }

    @Test
    fun `should get snippet by id that exists on database`() {
        val query = entityManager.createQuery("SELECT s FROM Snippet s ORDER BY s.id ASC", Snippet::class.java)
        query.maxResults = 1
        val snippetFromDB = query.resultList.firstOrNull()
        assertNotNull(snippetFromDB)
        val id = snippetFromDB?.id

        val result = this.sendGetRequestAndReturn("snippet/$id")

        val content = result.response.contentAsString
        val objectMapper = ObjectMapper()
        val snippet = objectMapper.readValue(content, SnippetDTO::class.java)

        assertNotNull(snippet)
        assertEquals(snippetFromDB?.writer, snippet.writer)
        assertEquals(snippetFromDB?.id, snippet.id)
        assertEquals(snippetFromDB?.name, snippet.name)
        assertEquals(snippetFromDB?.language, snippet.language)
    }

    @Test
    fun `should fail get a snippet because does not exist on database`() {
        val maxIdQuery = entityManager.createQuery("SELECT MAX(s.id) FROM Snippet s")
        val maxId = maxIdQuery.singleResult as Long
        val nonExistentId = maxId + 1000L  //for sure id does not exists

        val result = this.sendGetRequestAndReturn("snippet/$nonExistentId")
        val content = result.response.contentAsString
        assertTrue(content.isBlank())
        assertEquals(HttpStatus.NOT_FOUND.value(), result.response.status)
    }

    @Test
    fun `should send a list of snippets by writer test`() {
        val getterForm = GetterForm(
            userId = "testWriter",
            page = 0,
            size = 10
        )
        val objectMapper = ObjectMapper()
        val jsonContent = objectMapper.writeValueAsString(getterForm)

        val result = this.sendRequestAndReturn(jsonContent, "snippet/byWriter")
        val content = result.response.contentAsString
        val snippets = objectMapper.readValue(content, List::class.java)
        assertNotNull(snippets)
        assertEquals(snippets.size, 3)
    }

    @Test
    fun `should send a list of snippets by reader test`() {
        val query = entityManager.createQuery("SELECT s.id FROM Snippet s", Long::class.java)
        val snippetIds: List<Long> = query.resultList
        assertEquals(3, snippetIds.size)
        val first_Id = snippetIds[0]
        val second_Id = snippetIds[1]

        this.addReaderIntoSnippet(first_Id, "testWriter", "testReader")
        this.addReaderIntoSnippet(second_Id, "testWriter", "testReader")

        val getterForm = GetterForm(
            userId = "testReader",
            page = 0,
            size = 10
        )

        val objectMapper = ObjectMapper()
        val jsonContent = objectMapper.writeValueAsString(getterForm)

        val result = this.sendRequestAndReturn(jsonContent, "snippet/byReader")
        val content = result.response.contentAsString
        val snippets: List<SnippetDTO> = objectMapper.readValue(content, object : TypeReference<List<SnippetDTO>>() {})
        assertNotNull(snippets)

        val first_snippet = this.getSnippetFormDB(first_Id)
        val second_snippet = this.getSnippetFormDB(second_Id)
        val first_snippet_readers = first_snippet.readers
        val second_snippet_readers = second_snippet.readers
        assertTrue(first_snippet_readers.contains("testReader"))
        assertTrue(second_snippet_readers.contains("testReader"))
    }

    @Test
    fun `should send a list of snippets by reader and writer test`() {
        val snippetCreateJson = """
            {
                "writer": "anotherTestWriter",
                "name": "AnotherName",
                "language": "testLanguage",
                "extension": "txt"
            }
            """.trimIndent()
        val response = sendRequestAndReturn(snippetCreateJson, "snippet")
        val new_snippet_content = response.response.contentAsString
        val objectMapper = ObjectMapper()
        val new_snippet = objectMapper.readValue(new_snippet_content, SnippetDTO::class.java)
        this.addReaderIntoSnippet(new_snippet.id, "anotherTestWriter", "testWriter")

        val getterForm = GetterForm(
            userId = "testWriter",
            page = 0,
            size = 10
        )
        val jsonContent = objectMapper.writeValueAsString(getterForm)
        val result = this.sendRequestAndReturn(jsonContent, "snippet/byReaderAndWriter")
        val content = result.response.contentAsString
        val snippets: List<SnippetDTO> = objectMapper.readValue(content, object : TypeReference<List<SnippetDTO>>() {})
        assertNotNull(snippets)
        assertEquals(snippets.size, 4)  //three that wrote before the test and one that can read from another writer
        for (snippet in snippets) {
            if (snippet.id == new_snippet.id) {
                assertTrue(snippet.readers.contains("testWriter"))
            } else {
              assertEquals(snippet.writer, "testWriter")
            }
        }
    }

    @Test
    fun `should fail sending a list of snippets by writer who has no snippets test`() {
        val getterForm = GetterForm(
            userId = "WriterWhoHasNoSnippetsWritten",
            page = 0,
            size = 10
        )
        val objectMapper = ObjectMapper()
        val jsonContent = objectMapper.writeValueAsString(getterForm)
        val result = this.sendRequestAndReturn(jsonContent, "snippet/byReaderAndWriter")
        assertEquals(HttpStatus.OK.value(), result.response.status)

        val content = result.response.contentAsString
        val snippets: List<SnippetDTO> = objectMapper.readValue(content, object : TypeReference<List<SnippetDTO>>() {})
        assertNotNull(snippets)
        assertTrue(snippets.isEmpty())
    }
}
