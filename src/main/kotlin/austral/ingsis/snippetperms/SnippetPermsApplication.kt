package austral.ingsis.snippetperms

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SnippetPermsApplication

fun main(args: Array<String>) {
    runApplication<SnippetPermsApplication>(*args)
}
