package com.revshop.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SellerReviewDTO {
    private Long   reviewId;
    private Long   productId;
    private String productName;
    private String productImageUrl;
    private String buyerName;
    private String buyerEmail;
    private int    rating;
    private String comment;
    private LocalDateTime createdAt;
}