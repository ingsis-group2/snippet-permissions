package austral.ingsis.snippetperms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["austral.ingsis.snippetperms.repository"])
@EntityScan(basePackages = ["austral.ingsis.snippetperms.model"])
class SnippetTestApplication {
    fun main(args: Array<String>) {
        runApplication<SnippetTestApplication>(*args)
    }
}
