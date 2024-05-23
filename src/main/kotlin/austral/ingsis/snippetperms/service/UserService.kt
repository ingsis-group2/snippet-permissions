package austral.ingsis.snippetperms.service

import austral.ingsis.snippetperms.model.User
import austral.ingsis.snippetperms.model.dto.SnippetDTO
import austral.ingsis.snippetperms.model.dto.UserCreateDTO
import austral.ingsis.snippetperms.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service("user")
class UserService {

    @Autowired
    lateinit var userRepository: UserRepository

    fun createUser(body: UserCreateDTO): ResponseEntity<User> {
        return when {
            userRepository.existsById(body.id) -> ResponseEntity(HttpStatus.CONFLICT)
            else -> {
                val user = User()
                user.id = body.id
                val response = userRepository.save(user)
                ResponseEntity(response, HttpStatus.CREATED)
            }
        }
    }

    fun getUserById(userId: String): ResponseEntity<User> {
        if (userRepository.existsById(userId)) {
            val resposne = userRepository.findById(userId).get()
            return ResponseEntity(resposne, HttpStatus.OK)
        }
        return ResponseEntity(HttpStatus.NOT_FOUND)
    }

    fun existsById(userId: String): ResponseEntity<Boolean> {
        return when {
            userRepository.existsById(userId) -> ResponseEntity(true, HttpStatus.OK)
            else -> ResponseEntity(false, HttpStatus.NOT_FOUND)
        }
    }
}
