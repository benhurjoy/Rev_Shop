package com.revshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    // User stats
    private Long totalUsers;
    private Long totalBuyers;
    private Long totalSellers;
    private Long blockedUsers;

    // Product stats
    private Long totalProducts;
    private Long activeProducts;
    private Long outOfStockProducts;

    // Order stats
    private Long totalOrders;
    private Long pendingOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;

    // Revenue
    private Double totalRevenue;

    // Review stats
    private Long totalReviews;
}