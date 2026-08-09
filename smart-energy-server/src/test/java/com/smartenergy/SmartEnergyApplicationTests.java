package com.smartenergy;

import com.smartenergy.exception.BusinessException;
import com.smartenergy.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 应用基础测试
 * <p>
 * 验证核心类可正常加载。完整的 Spring 上下文集成测试需要运行中的数据库，
 * 在 CI 环境中通过 docker-compose 提供。
 */
class SmartEnergyApplicationTests {

    @Test
    void applicationMainClassExists() {
        SmartEnergyApplication app = new SmartEnergyApplication();
        assertNotNull(app);
    }

    @Test
    void globalExceptionHandlerCanBeCreated() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        assertNotNull(handler);
    }

    @Test
    void businessExceptionWorks() {
        BusinessException ex = BusinessException.notFound("设备不存在");
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("设备不存在"));
    }
}
