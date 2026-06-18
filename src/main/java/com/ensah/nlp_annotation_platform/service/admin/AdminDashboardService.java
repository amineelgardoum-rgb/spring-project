package com.ensah.nlp_annotation_platform.service.admin;

import com.ensah.nlp_annotation_platform.dto.response.admin.DashboardStatsResponse;

public interface AdminDashboardService {
    DashboardStatsResponse getStats();
}
