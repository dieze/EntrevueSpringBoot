package com.example.entrevueSpringBoot;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Set HTTP status NOT_FOUND when a @ResponseBody handler returns null.
 *
 * @see WebMvcAutoConfiguration.EnableWebMvcConfiguration#requestMappingHandlerAdapter bean created by auto-configuration
 * @see RequestMappingHandlerAdapter#afterPropertiesSet that queries ApplicationContext to create ControllerAdviceBean instances
 * @see ControllerAdviceBean#findAnnotatedBeans that creates ControllerAdviceBean instances
 */
@ControllerAdvice
public class NullAsNotFoundResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // for handlers returning ResponseEntity
        // let's say the handler's code is responsible for setting the response HTTP status
        return returnType.getParameterType() != ResponseEntity.class;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) response.setStatusCode(HttpStatus.NOT_FOUND);
        return body;
    }
}
