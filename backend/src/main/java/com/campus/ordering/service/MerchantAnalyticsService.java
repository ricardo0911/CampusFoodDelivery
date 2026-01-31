package com.campus.ordering.service;

import java.util.Map;

public interface MerchantAnalyticsService {
    Map<String, Object> getOverview(Long shopId);
    Map<String, Object> getSalesTrend(Long shopId, String period);
    Map<String, Object> getHotDishes(Long shopId, int limit);
    Map<String, Object> getCustomerAnalysis(Long shopId);
    Map<String, Object> getPeakHours(Long shopId);
}
