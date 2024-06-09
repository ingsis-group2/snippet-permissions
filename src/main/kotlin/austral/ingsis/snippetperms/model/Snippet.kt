package austral.ingsis.snippetperms.model

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

@Entity
class Snippet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0

    @Column(nullable = false)
    var container: String = ""

    @Column(nullable = false, unique = true, name = "snippet_writerId")
    var writer: String = ""

    @Column(nullable = false)
    var name: String = ""

    @Column(nullable = false)
    var language: String = ""

    @Column(nullable = false)
    var extension: String = ""

    @Column
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    var creationDate: LocalDateTime = LocalDateTime.now()

    @Column
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    var updateDate: LocalDateTime? = null

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "snippet_readersId")
    var readers: MutableSet<String> = mutableSetOf()
}
