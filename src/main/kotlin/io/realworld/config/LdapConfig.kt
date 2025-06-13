package io.realworld.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.LdapContextSource

@Configuration
class LdapConfig {

    @Bean
    fun contextSource(props: LdapProperties): LdapContextSource {
        return LdapContextSource().apply {
            setUrl(props.url)
            setBase(props.base)
            userDn = props.userDn
            password = props.password
            afterPropertiesSet()
        }
    }

    @Bean
    fun ldapTemplate(contextSource: LdapContextSource): LdapTemplate {
        return LdapTemplate(contextSource)
    }
}
