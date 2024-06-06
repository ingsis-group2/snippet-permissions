package austral.ingsis.snippetperms.model

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Snippet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0

    @Column(nullable = false)
    var container: String = ""

    @Column(nullable = false, unique = true, name = "snippet_writerId")
    var writer: String = ""

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "snippet_readersId")
    var readers: MutableSet<String> = mutableSetOf()
}
