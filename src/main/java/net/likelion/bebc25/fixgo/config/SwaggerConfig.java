package net.likelion.bebc25.fixgo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import net.likelion.bebc25.fixgo.global.exception.ErrorResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH = "bearerAuth";
    private static final String ERROR_RESPONSE = "ErrorResponse";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                              .title("FixGo API")
                              .description("FixGo A/S 관리 시스템 REST API 문서")
                              .version("v1"))
                .components(new Components()
                                    .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                            .name(BEARER_AUTH)
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("bearer")
                                            .bearerFormat("JWT")
                                            .description("JWT Access Token (Authorization: Bearer {token})"))
                                    .addSchemas(ERROR_RESPONSE, new Schema<ErrorResponse>()
                                            .type("object")
                                            .addProperty("code", new Schema<>().type("string").example("NOT_FOUND"))
                                            .addProperty("message", new Schema<>().type("string")
                                                    .example("요청한 리소스를 찾을 수 없습니다."))))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    @Bean
    public OperationCustomizer globalErrorResponses() {
        return (operation, handlerMethod) -> {
            operation.getResponses().addApiResponse("400", errorResponse("잘못된 요청 파라미터 또는 검증 실패"));
            operation.getResponses().addApiResponse("401", errorResponse("인증되지 않은 요청"));
            operation.getResponses().addApiResponse("403", errorResponse("접근 권한이 없음"));
            operation.getResponses().addApiResponse("404", errorResponse("요청한 리소스를 찾을 수 없음"));
            operation.getResponses().addApiResponse("500", errorResponse("서버 내부 오류"));
            return operation;
        };
    }

    private static ApiResponse errorResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + ERROR_RESPONSE))
                ));
    }
}
