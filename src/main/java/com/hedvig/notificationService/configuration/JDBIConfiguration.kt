package com.hedvig.notificationService.configuration

import org.jdbi.v3.spring4.JdbiFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class JDBIConfiguration {
    @Bean("jdbi")
    fun jdbiFactory(dataSource: DataSource): JdbiFactoryBean {
        return JdbiFactoryBean(dataSource)
    }
}
