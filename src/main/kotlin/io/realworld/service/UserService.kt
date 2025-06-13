package io.realworld.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.realworld.exception.InvalidLoginException
import io.realworld.exception.InvalidRequest
import io.realworld.model.User
import io.realworld.model.inout.Login
import io.realworld.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.validation.BindException
import java.util.*

@Service
class UserService(
    val userRepository: UserRepository,
    @Value("\${jwt.secret}") val jwtSecret: String,
    @Value("\${jwt.issuer}") val jwtIssuer: String,
    private val ldapAuthService: LdapAuthService
) {

    val currentUser = ThreadLocal<User>()

    fun findByToken(token: String) = userRepository.findByToken(token)

    fun clearCurrentUser() = currentUser.remove()

    fun setCurrentUser(user: User): User {
        currentUser.set(user)
        return user
    }

    fun currentUser(): User = currentUser.get()

    fun newToken(user: User): String {
        return Jwts.builder()
                .setIssuedAt(Date())
                .setSubject(user.email)
                .setIssuer(jwtIssuer)
                .setExpiration(Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000)) // 10 days
                .signWith(SignatureAlgorithm.HS256, jwtSecret).compact()
    }

    fun validToken(token: String, user: User): Boolean {
        val claims = Jwts.parser().setSigningKey(jwtSecret)
                .parseClaimsJws(token).body
        return claims.subject == user.email && claims.issuer == jwtIssuer
                && Date().before(claims.expiration)
    }

    fun updateToken(user: User): User {
        user.token = newToken(user)
        return userRepository.save(user)
    }

    fun login(login: Login): User? {
        var user = userRepository.findByUsername(login.username!!)

        val ldapOk = ldapAuthService.authenticate(login.username!!, login.password!!)
        if (!ldapOk) {
            throw InvalidLoginException("password", "Invalid password combo ${login.username} + ${login.password}")
        }

        if (user == null) {
            user = User(username = login.username!!,
                email = login.username!! + "@lololol.fr", password = BCrypt.hashpw(login.password!!, BCrypt.gensalt()))
            user.token = newToken(user)
        }

        return user
    }
}
