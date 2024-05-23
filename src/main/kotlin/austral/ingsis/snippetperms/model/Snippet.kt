package austral.ingsis.snippetperms.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany

@Entity
class Snippet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0

    @Column(nullable = false)
    var container: String = ""

    @Column(nullable = false)
    var writer: String = ""

    @ManyToMany(mappedBy = "snippets")
    var readers: MutableSet<User> = mutableSetOf()
}
