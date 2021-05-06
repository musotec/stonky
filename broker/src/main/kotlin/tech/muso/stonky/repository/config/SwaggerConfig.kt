package tech.muso.stonky.repository.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.WebFluxConfigurer
import springfox.documentation.builders.PathSelectors.regex
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType

import springfox.documentation.spring.web.plugins.Docket

@Configuration
class SwaggerConfig : WebFluxConfigurer {

    @Bean
    fun createRestApi(): Docket = Docket(DocumentationType.SWAGGER_2)
            .enable(true)
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(regex("/v./.*"))
//            .paths(regex("/v1/.*"))
//            .paths(regex("/v2/.*"))
            .build()
}