package com.smartenergy.service;

import com.smartenergy.common.PageResult;
import com.smartenergy.dto.AlarmRuleCreateDTO;
import com.smartenergy.dto.AlarmRuleUpdateDTO;
import com.smartenergy.vo.AlarmRuleVO;

public interface AlarmRuleService {
    PageResult<AlarmRuleVO> listRules(int page, int pageSize, String keyword, String severity, Integer status);
    AlarmRuleVO getRule(Long id);
    AlarmRuleVO createRule(AlarmRuleCreateDTO dto);
    AlarmRuleVO updateRule(Long id, AlarmRuleUpdateDTO dto);
    void deleteRule(Long id);
}
