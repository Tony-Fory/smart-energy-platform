package com.smartenergy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HistoryDataVO {

    private String deviceCode;
    private String metric;
    private String interval;
    private List<DataPoint> list;

    @Data
    public static class DataPoint {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime timestamp;
        private Double value;
    }
}
