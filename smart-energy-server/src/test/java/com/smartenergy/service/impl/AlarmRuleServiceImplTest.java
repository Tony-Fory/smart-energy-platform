package com.smartenergy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartenergy.common.PageResult;
import com.smartenergy.dto.AlarmRuleCreateDTO;
import com.smartenergy.dto.AlarmRuleUpdateDTO;
import com.smartenergy.entity.AlarmRule;
import com.smartenergy.exception.BusinessException;
import com.smartenergy.mapper.AlarmRuleMapper;
import com.smartenergy.vo.AlarmRuleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlarmRuleService 单元测试")
class AlarmRuleServiceImplTest {

    @Mock private AlarmRuleMapper alarmRuleMapper;
    @InjectMocks private AlarmRuleServiceImpl alarmRuleService;

    private AlarmRule rule;
    private AlarmRuleCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        rule = new AlarmRule();
        rule.setId(1L);
        rule.setRuleName("功率过高告警");
        rule.setMetric("POWER");
        rule.setOperator("GT");
        rule.setThreshold(1000.0);
        rule.setSeverity("WARNING");
        rule.setStatus(1);

        createDTO = new AlarmRuleCreateDTO();
        createDTO.setRuleName("功率过高告警");
        createDTO.setMetric("POWER");
        createDTO.setOperator("GT");
        createDTO.setThreshold(1000.0);
        createDTO.setSeverity("WARNING");
        createDTO.setStatus(1);
    }

    @Test
    @DisplayName("分页查询规则列表")
    void shouldListRules() {
        Page<AlarmRule> page = new Page<>(1, 10);
        page.setRecords(List.of(rule));
        page.setTotal(1);
        when(alarmRuleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<AlarmRuleVO> result = alarmRuleService.listRules(1, 10, null, null, null);
        assertEquals(1, result.getTotal());
        assertEquals("POWER", result.getRecords().get(0).getMetric());
    }

    @Test
    @DisplayName("新增规则成功")
    void shouldCreateRule() {
        AlarmRuleVO vo = alarmRuleService.createRule(createDTO);
        assertNotNull(vo);
        assertEquals("POWER", vo.getMetric());
        assertEquals("GT", vo.getOperator());
        verify(alarmRuleMapper).insert(any(AlarmRule.class));
    }

    @Test
    @DisplayName("新增规则-非法 metric 抛出异常")
    void shouldRejectInvalidMetric() {
        createDTO.setMetric("INVALID");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> alarmRuleService.createRule(createDTO));
        assertTrue(ex.getMessage().contains("非法的监控指标"));
    }

    @Test
    @DisplayName("新增规则-非法 operator 抛出异常")
    void shouldRejectInvalidOperator() {
        createDTO.setOperator("EQ");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> alarmRuleService.createRule(createDTO));
        assertTrue(ex.getMessage().contains("非法的运算符"));
    }

    @Test
    @DisplayName("新增规则-非法 severity 抛出异常")
    void shouldRejectInvalidSeverity() {
        createDTO.setSeverity("HIGH");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> alarmRuleService.createRule(createDTO));
        assertTrue(ex.getMessage().contains("非法的严重级别"));
    }

    @Test
    @DisplayName("修改规则成功")
    void shouldUpdateRule() {
        when(alarmRuleMapper.selectById(1L)).thenReturn(rule);
        AlarmRuleUpdateDTO updateDTO = new AlarmRuleUpdateDTO();
        updateDTO.setRuleName("更新规则");
        updateDTO.setMetric("CURRENT");
        updateDTO.setOperator("LT");
        updateDTO.setThreshold(5.0);
        updateDTO.setSeverity("CRITICAL");

        AlarmRuleVO vo = alarmRuleService.updateRule(1L, updateDTO);
        assertEquals("CURRENT", vo.getMetric());
        assertEquals("CRITICAL", vo.getSeverity());
    }

    @Test
    @DisplayName("修改不存在的规则抛出异常")
    void shouldThrowWhenUpdateNotFound() {
        when(alarmRuleMapper.selectById(999L)).thenReturn(null);
        AlarmRuleUpdateDTO dto = new AlarmRuleUpdateDTO();
        dto.setRuleName("x"); dto.setMetric("POWER"); dto.setOperator("GT");
        dto.setThreshold(1.0); dto.setSeverity("WARNING");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> alarmRuleService.updateRule(999L, dto));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("删除规则成功")
    void shouldDeleteRule() {
        when(alarmRuleMapper.selectById(1L)).thenReturn(rule);
        alarmRuleService.deleteRule(1L);
        verify(alarmRuleMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除不存在的规则抛出异常")
    void shouldThrowWhenDeleteNotFound() {
        when(alarmRuleMapper.selectById(999L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> alarmRuleService.deleteRule(999L));
        assertEquals(404, ex.getCode());
    }
}
