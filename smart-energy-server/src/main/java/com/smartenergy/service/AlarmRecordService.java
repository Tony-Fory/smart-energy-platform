package com.smartenergy.service;

import com.smartenergy.common.PageResult;
import com.smartenergy.vo.AlarmRecordVO;

public interface AlarmRecordService {
    PageResult<AlarmRecordVO> listRecords(int page, int pageSize, String deviceCode, Integer status, String severity);
    AlarmRecordVO getRecord(Long id);
    void ackAlarm(Long id);
}
