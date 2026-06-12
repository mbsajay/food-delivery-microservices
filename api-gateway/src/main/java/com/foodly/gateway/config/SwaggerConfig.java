package com.foodly.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Serves the aggregated Swagger UI for the gateway.
 *
 * Springdoc WebFlux redirects /swagger-ui.html to the webjars path but cannot
 * override the static swagger-initializer.js. This RouterFunction intercepts:
 *   1. /swagger-ui.html              — redirect to /swagger-ui/index.html
 *   2. swagger-initializer.js        — custom version that uses configUrl
 *   3. /swagger-ui/** and /webjars/** — static files from classpath webjars
 *
 * The configUrl /v3/api-docs/swagger-config is served by the gateway's own
 * springdoc and returns the urls array with all six service specs.
 */
@Configuration
public class SwaggerConfig {

    private static final String INIT_JS = """
            window.onload = function () {
                window.ui = SwaggerUIBundle({
                    configUrl: '/v3/api-docs/swagger-config',
                    dom_id: '#swagger-ui',
                    deepLinking: true,
                    presets: [
                        SwaggerUIBundle.presets.apis,
                        SwaggerUIBundle.SwaggerUIStandalonePreset
                    ],
                    plugins: [ SwaggerUIBundle.plugins.DownloadUrl ],
                    layout: 'StandaloneLayout'
                });
            };
            """;

    @Bean
    @Order(-1)
    public RouterFunction<ServerResponse> swaggerUiRouter() {
        MediaType js = MediaType.parseMediaType("application/javascript;charset=UTF-8");

        return route()
                // /swagger-ui.html → send to index
                .GET("/swagger-ui.html", req ->
                        ServerResponse.temporaryRedirect(URI.create("/swagger-ui/index.html")).build())

                // Override swagger-initializer.js at BOTH common paths
                .GET("/swagger-ui/swagger-initializer.js", req ->
                        ServerResponse.ok().contentType(js).bodyValue(INIT_JS))
                .GET("/webjars/swagger-ui/swagger-initializer.js", req ->
                        ServerResponse.ok().contentType(js).bodyValue(INIT_JS))

                .build()
                // Static swagger-ui resources from springdoc webjars on the classpath
                .and(RouterFunctions.resources("/swagger-ui/**",
                        new ClassPathResource("META-INF/resources/webjars/swagger-ui/")))
                .and(RouterFunctions.resources("/webjars/**",
                        new ClassPathResource("META-INF/resources/webjars/")));
    }
}
