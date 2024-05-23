package austral.ingsis.snippetperms.model

import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
class Snippet {
    @Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Long = 0
    @Column(nullable = false) var container: String = ""
    @Column(nullable = false) var writer: String = ""
    @ManyToMany(mappedBy = "snippets")
    var readers: MutableSet<User> = mutableSetOf()
}