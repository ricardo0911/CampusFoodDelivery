package com.campus.ordering.service;

import java.util.Map;

public interface FinanceService {
    Map<String, Object> getOverview();
    Map<String, Object> getCommission(String startDate, String endDate);
    Map<String, Object> getSettlement(Long shopId, String month);
    byte[] exportReport(String type, String startDate, String endDate);
}
