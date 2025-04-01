package com.twitterclone.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth"
        return new OpenAPI()
                .info(new Info()
                        .title("Twitter Clone API")
                        .description("""
REST API for Twitter-like application.

## Authentication
This API uses JWT Bearer token authentication. To authorize:

1. Use the `/api/auth/login` endpoint to obtain a token
2. Click the 'Authorize' button at the top of this page
3. Enter your token in the format: `Bearer your-token-here`
4. Click 'Authorize' and close the popup

Now all your API requests will include the token.
                """)
                        .version("1.0.0"))
                .addServersItem(new Server()
                        .url("/")
                        .description("Local server"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT Bearer token in the format: `Bearer {token}`")))
    }
}
