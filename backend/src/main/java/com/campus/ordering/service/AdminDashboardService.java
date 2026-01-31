package com.campus.ordering.service;

import java.util.Map;

public interface AdminDashboardService {
    Map<String, Object> getRealtime();
    Map<String, Object> getTodayStats();
    Map<String, Object> getOrderMap();
    Map<String, Object> getTrend(String period);
}
