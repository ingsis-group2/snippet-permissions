package austral.ingsis.snippetperms.controller

import austral.ingsis.snippetperms.model.User
import austral.ingsis.snippetperms.model.dto.UserCreateDTO
import austral.ingsis.snippetperms.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/user")
class UserController {
    @Autowired
    private lateinit var userService: UserService

    @PostMapping("")
    fun user(
        @RequestBody body: UserCreateDTO,
    ): ResponseEntity<User> {
        val resp = userService.createUser(body)
        return resp
    }

    @GetMapping("/{id}")
    fun getUser(
        @PathVariable("id") id: String,
    ): ResponseEntity<User> {
        val resp = userService.getUserById(id)
        return resp
    }

    @GetMapping("/exists/{id}")
    fun exists(
        @PathVariable("id") id: String,
    ): ResponseEntity<Boolean> {
        val resp = userService.existsById(id)
        return resp
    }
}
