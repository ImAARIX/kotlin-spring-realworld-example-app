package io.realworld.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Hashtable
import javax.naming.Context
import javax.naming.directory.InitialDirContext

@Service
class LdapAuthService(
    @Value("\${ldap.url}") private val ldapUrl: String,
    @Value("\${ldap.base}") private val baseDn: String,
) {

    fun authenticate(username: String, password: String): Boolean {
        val userDn = "cn=$username,$baseDn"
        val env = Hashtable<String, String>().apply {
            put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
            put(Context.PROVIDER_URL, ldapUrl)
            put(Context.SECURITY_AUTHENTICATION, "simple")
            put(Context.SECURITY_PRINCIPAL, userDn)
            put(Context.SECURITY_CREDENTIALS, password)
        }

        return try {
            InitialDirContext(env).let { true }
        } catch (ex: Exception) {
            false
        }
    }
}
