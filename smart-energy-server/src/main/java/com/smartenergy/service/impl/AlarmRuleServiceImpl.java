package com.smartenergy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartenergy.common.PageResult;
import com.smartenergy.dto.AlarmRuleCreateDTO;
import com.smartenergy.dto.AlarmRuleUpdateDTO;
import com.smartenergy.entity.AlarmRule;
import com.smartenergy.exception.BusinessException;
import com.smartenergy.mapper.AlarmRuleMapper;
import com.smartenergy.service.AlarmRuleService;
import com.smartenergy.vo.AlarmRuleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmRuleServiceImpl implements AlarmRuleService {

    private final AlarmRuleMapper alarmRuleMapper;

    @Override
    public PageResult<AlarmRuleVO> listRules(int page, int pageSize,
                                              String keyword, String severity, Integer status) {
        LambdaQueryWrapper<AlarmRule> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(AlarmRule::getRuleName, keyword);
        }
        if (StringUtils.hasText(severity)) {
            wrapper.eq(AlarmRule::getSeverity, severity);
        }
        if (status != null) {
            wrapper.eq(AlarmRule::getStatus, status);
        }
        wrapper.orderByDesc(AlarmRule::getId);

        Page<AlarmRule> result = alarmRuleMapper.selectPage(Page.of(page, pageSize), wrapper);
        List<AlarmRuleVO> vos = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(vos, result.getTotal());
    }

    @Override
    public AlarmRuleVO getRule(Long id) {
        AlarmRule rule = alarmRuleMapper.selectById(id);
        if (rule == null) {
            throw BusinessException.notFound("告警规则不存在");
        }
        return toVO(rule);
    }

    @Override
    @Transactional
    public AlarmRuleVO createRule(AlarmRuleCreateDTO dto) {
        validateRuleDTO(dto);
        AlarmRule rule = new AlarmRule();
        rule.setDeviceId(dto.getDeviceId());
        rule.setRuleName(dto.getRuleName());
        rule.setMetric(dto.getMetric().toUpperCase());
        rule.setOperator(dto.getOperator().toUpperCase());
        rule.setThreshold(dto.getThreshold());
        rule.setSeverity(dto.getSeverity().toUpperCase());
        rule.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        alarmRuleMapper.insert(rule);
        return toVO(rule);
    }

    @Override
    @Transactional
    public AlarmRuleVO updateRule(Long id, AlarmRuleUpdateDTO dto) {
        AlarmRule rule = alarmRuleMapper.selectById(id);
        if (rule == null) {
            throw BusinessException.notFound("告警规则不存在");
        }
        validateRuleDTO(dto);
        rule.setDeviceId(dto.getDeviceId());
        rule.setRuleName(dto.getRuleName());
        rule.setMetric(dto.getMetric().toUpperCase());
        rule.setOperator(dto.getOperator().toUpperCase());
        rule.setThreshold(dto.getThreshold());
        rule.setSeverity(dto.getSeverity().toUpperCase());
        if (dto.getStatus() != null) {
            rule.setStatus(dto.getStatus());
        }
        alarmRuleMapper.updateById(rule);
        return toVO(rule);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        AlarmRule rule = alarmRuleMapper.selectById(id);
        if (rule == null) {
            throw BusinessException.notFound("告警规则不存在");
        }
        alarmRuleMapper.deleteById(id);
    }

    private void validateRuleDTO(AlarmRuleCreateDTO dto) {
        if (!AlarmRuleCreateDTO.VALID_METRICS.contains(dto.getMetric().toUpperCase())) {
            throw BusinessException.badRequest("非法的监控指标: " + dto.getMetric());
        }
        if (!AlarmRuleCreateDTO.VALID_OPERATORS.contains(dto.getOperator().toUpperCase())) {
            throw BusinessException.badRequest("非法的运算符: " + dto.getOperator());
        }
        if (!AlarmRuleCreateDTO.VALID_SEVERITIES.contains(dto.getSeverity().toUpperCase())) {
            throw BusinessException.badRequest("非法的严重级别: " + dto.getSeverity());
        }
    }

    private void validateRuleDTO(AlarmRuleUpdateDTO dto) {
        if (!AlarmRuleCreateDTO.VALID_METRICS.contains(dto.getMetric().toUpperCase())) {
            throw BusinessException.badRequest("非法的监控指标: " + dto.getMetric());
        }
        if (!AlarmRuleCreateDTO.VALID_OPERATORS.contains(dto.getOperator().toUpperCase())) {
            throw BusinessException.badRequest("非法的运算符: " + dto.getOperator());
        }
        if (!AlarmRuleCreateDTO.VALID_SEVERITIES.contains(dto.getSeverity().toUpperCase())) {
            throw BusinessException.badRequest("非法的严重级别: " + dto.getSeverity());
        }
    }

    private AlarmRuleVO toVO(AlarmRule rule) {
        AlarmRuleVO vo = new AlarmRuleVO();
        vo.setId(rule.getId());
        vo.setDeviceId(rule.getDeviceId());
        vo.setRuleName(rule.getRuleName());
        vo.setMetric(rule.getMetric());
        vo.setOperator(rule.getOperator());
        vo.setThreshold(rule.getThreshold());
        vo.setSeverity(rule.getSeverity());
        vo.setStatus(rule.getStatus());
        vo.setCreateTime(rule.getCreateTime());
        vo.setUpdateTime(rule.getUpdateTime());
        return vo;
    }
}
