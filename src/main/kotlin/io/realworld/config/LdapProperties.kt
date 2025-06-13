package io.realworld.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "ldap")
class LdapProperties {
    lateinit var url: String
    lateinit var base: String
    lateinit var userDn: String
    lateinit var password: String
}