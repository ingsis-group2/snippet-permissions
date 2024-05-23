package austral.ingsis.snippetperms.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany

@Entity
class User {
    @Id
    var id: String = ""

    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "Reads_Snippet",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "snippet_id")],
    )
    var snippets: MutableSet<Snippet> = mutableSetOf()
}
