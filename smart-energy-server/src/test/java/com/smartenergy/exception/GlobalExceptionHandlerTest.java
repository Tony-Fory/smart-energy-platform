package com.smartenergy.exception;

import com.smartenergy.common.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * GlobalExceptionHandler 单元测试
 */
@DisplayName("GlobalExceptionHandler 单元测试")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BusinessException notFound 返回 404")
    void shouldReturn404ForNotFound() {
        BusinessException ex = BusinessException.notFound("设备不存在");

        ResponseEntity<Result<Void>> response = handler.handleBusiness(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getCode());
        assertEquals("设备不存在", response.getBody().getMessage());
    }

    @Test
    @DisplayName("BusinessException badRequest 返回 400")
    void shouldReturn400ForBadRequest() {
        BusinessException ex = BusinessException.badRequest("参数错误");

        ResponseEntity<Result<Void>> response = handler.handleBusiness(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCode());
    }

    @Test
    @DisplayName("RuntimeException 返回 500 且不暴露堆栈")
    void shouldReturn500ForRuntimeException() {
        RuntimeException ex = new RuntimeException("内部错误详情");

        ResponseEntity<Result<Void>> response = handler.handleRuntime(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getCode());
        // 不应返回原始异常消息
        assertFalse(response.getBody().getMessage().contains("内部错误详情"));
    }

    @Test
    @DisplayName("Exception 兜底返回 500")
    void shouldReturn500ForGenericException() {
        Exception ex = new Exception("未知错误");

        ResponseEntity<Result<Void>> response = handler.handleAll(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getCode());
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 返回明确的字段错误")
    void shouldReturnFieldErrorsForValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("dto", "deviceCode", "设备编号不能为空"),
                new FieldError("dto", "voltage", "电压不能为空")
        ));

        ResponseEntity<Result<Void>> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("deviceCode"));
        assertTrue(response.getBody().getMessage().contains("设备编号不能为空"));
    }
}
