package com.smartenergy.controller;

import com.smartenergy.common.PageResult;
import com.smartenergy.common.Result;
import com.smartenergy.dto.AlarmRuleCreateDTO;
import com.smartenergy.dto.AlarmRuleUpdateDTO;
import com.smartenergy.service.AlarmRuleService;
import com.smartenergy.vo.AlarmRuleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "告警规则", description = "告警规则 CRUD 接口")
@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class AlarmRuleController {

    private final AlarmRuleService alarmRuleService;

    @Operation(summary = "查询告警规则列表")
    @GetMapping("/rules")
    public Result<PageResult<AlarmRuleVO>> listRules(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Integer status) {
        return Result.success(alarmRuleService.listRules(page, pageSize, keyword, severity, status));
    }

    @Operation(summary = "查询告警规则详情")
    @GetMapping("/rules/{id}")
    public Result<AlarmRuleVO> getRule(@Parameter(description = "规则ID") @PathVariable Long id) {
        return Result.success(alarmRuleService.getRule(id));
    }

    @Operation(summary = "新增告警规则")
    @PostMapping("/rules")
    public Result<AlarmRuleVO> createRule(@Valid @RequestBody AlarmRuleCreateDTO dto) {
        return Result.success(alarmRuleService.createRule(dto));
    }

    @Operation(summary = "更新告警规则")
    @PutMapping("/rules/{id}")
    public Result<AlarmRuleVO> updateRule(@Parameter(description = "规则ID") @PathVariable Long id,
                                           @Valid @RequestBody AlarmRuleUpdateDTO dto) {
        return Result.success(alarmRuleService.updateRule(id, dto));
    }

    @Operation(summary = "删除告警规则")
    @DeleteMapping("/rules/{id}")
    public Result<Void> deleteRule(@Parameter(description = "规则ID") @PathVariable Long id) {
        alarmRuleService.deleteRule(id);
        return Result.success(null);
    }
}
