package ru.cns

import com.zaxxer.hikari.HikariDataSource
import net.ttddyy.dsproxy.listener.DataSourceQueryCountListener
import net.ttddyy.dsproxy.support.ProxyDataSource
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import javax.sql.DataSource

@EnableAutoConfiguration
@Configuration
@EnableJpaAuditing
class TestJpaConfiguration {

    @Bean
    fun proxyDataSource(): DataSource {
        val proxyDataSource = ProxyDataSource(
                DataSourceBuilder.create()
                        .type(HikariDataSource::class.java)
                        .url("jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE")
                        .build()
        )
        proxyDataSource.addListener(DataSourceQueryCountListener())

        return proxyDataSource
    }
}
