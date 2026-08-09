package com.smartenergy.controller;

import com.smartenergy.common.PageResult;
import com.smartenergy.common.Result;
import com.smartenergy.service.AlarmRecordService;
import com.smartenergy.vo.AlarmRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "告警记录", description = "告警记录查询与确认接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AlarmRecordController {

    private final AlarmRecordService alarmRecordService;

    @Operation(summary = "查询告警记录列表")
    @GetMapping("/alarms")
    public Result<PageResult<AlarmRecordVO>> listRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String deviceCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String severity) {
        return Result.success(alarmRecordService.listRecords(page, pageSize, deviceCode, status, severity));
    }

    @Operation(summary = "查询告警记录详情")
    @GetMapping("/alarms/{id}")
    public Result<AlarmRecordVO> getRecord(@Parameter(description = "记录ID") @PathVariable Long id) {
        return Result.success(alarmRecordService.getRecord(id));
    }

    @Operation(summary = "确认告警")
    @PutMapping("/alarms/{id}/ack")
    public Result<Void> ackAlarm(@Parameter(description = "记录ID") @PathVariable Long id) {
        alarmRecordService.ackAlarm(id);
        return Result.success(null);
    }
}
