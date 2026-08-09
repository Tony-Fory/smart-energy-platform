package com.smartenergy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartenergy.common.PageResult;
import com.smartenergy.entity.AlarmRecord;
import com.smartenergy.exception.BusinessException;
import com.smartenergy.mapper.AlarmRecordMapper;
import com.smartenergy.service.AlarmRecordService;
import com.smartenergy.vo.AlarmRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmRecordServiceImpl implements AlarmRecordService {

    private final AlarmRecordMapper alarmRecordMapper;

    @Override
    public PageResult<AlarmRecordVO> listRecords(int page, int pageSize,
                                                  String deviceCode, Integer status, String severity) {
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(deviceCode)) {
            wrapper.eq(AlarmRecord::getDeviceCode, deviceCode);
        }
        if (status != null) {
            wrapper.eq(AlarmRecord::getStatus, status);
        }
        if (StringUtils.hasText(severity)) {
            wrapper.eq(AlarmRecord::getSeverity, severity);
        }
        wrapper.orderByDesc(AlarmRecord::getAlarmTime);

        Page<AlarmRecord> result = alarmRecordMapper.selectPage(Page.of(page, pageSize), wrapper);
        List<AlarmRecordVO> vos = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(vos, result.getTotal());
    }

    @Override
    public AlarmRecordVO getRecord(Long id) {
        AlarmRecord record = alarmRecordMapper.selectById(id);
        if (record == null) {
            throw BusinessException.notFound("告警记录不存在");
        }
        return toVO(record);
    }

    @Override
    @Transactional
    public void ackAlarm(Long id) {
        AlarmRecord record = alarmRecordMapper.selectById(id);
        if (record == null) {
            throw BusinessException.notFound("告警记录不存在");
        }
        if (record.getStatus() != 0) {
            throw BusinessException.badRequest("当前告警状态不允许确认（仅未处理的告警可确认）");
        }
        record.setStatus(1);
        alarmRecordMapper.updateById(record);
    }

    private AlarmRecordVO toVO(AlarmRecord record) {
        AlarmRecordVO vo = new AlarmRecordVO();
        vo.setId(record.getId());
        vo.setDeviceId(record.getDeviceId());
        vo.setDeviceCode(record.getDeviceCode());
        vo.setRuleId(record.getRuleId());
        vo.setRuleName(record.getRuleName());
        vo.setMetric(record.getMetric());
        vo.setActualValue(record.getActualValue());
        vo.setThreshold(record.getThreshold());
        vo.setSeverity(record.getSeverity());
        vo.setStatus(record.getStatus());
        vo.setAlarmTime(record.getAlarmTime());
        vo.setRecoverTime(record.getRecoverTime());
        vo.setRemark(record.getRemark());
        vo.setCreateTime(record.getCreateTime());
        vo.setUpdateTime(record.getUpdateTime());
        return vo;
    }
}
