package com.revshop.service;

import com.revshop.dto.CouponDTO;
import com.revshop.entity.Coupon;
import com.revshop.exception.BadRequestException;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.repository.CouponRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CouponService {

    private static final Logger logger = LogManager.getLogger(CouponService.class);

    @Autowired private CouponRepository couponRepository;

    // ── Validate and calculate discount (called from cart/checkout) ──
    public BigDecimal applyAndCalculateDiscount(String code, BigDecimal orderTotal) {
        logger.info("ApplyCoupon called: code={} total={}", code, orderTotal);

        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BadRequestException("Invalid coupon code: " + code));

        if (!coupon.isActive()) {
            throw new BadRequestException("This coupon is no longer active.");
        }
        if (coupon.isExpired()) {
            throw new BadRequestException("This coupon has expired.");
        }
        if (coupon.isUsageLimitReached()) {
            throw new BadRequestException("This coupon has reached its usage limit.");
        }
        if (orderTotal.compareTo(coupon.getMinimumOrderAmount()) < 0) {
            throw new BadRequestException("Minimum order amount of ₹" +
                    coupon.getMinimumOrderAmount() + " required for this coupon.");
        }

        BigDecimal discount;
        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            discount = orderTotal
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountValue().min(orderTotal); // can't discount more than total
        }

        logger.info("Coupon {} applied — discount: ₹{}", code, discount);
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    // ── Increment usage count after order is placed ──
    @Transactional
    public void incrementUsage(String code) {
        couponRepository.findByCodeIgnoreCase(code).ifPresent(coupon -> {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
            logger.info("Coupon {} usage incremented to {}", code, coupon.getUsedCount());
        });
    }

    // ── Admin CRUD ──
    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CouponDTO getCouponById(Long id) {
        return mapToDTO(couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + id)));
    }

    @Transactional
    public void createCoupon(CouponDTO dto) {
        if (couponRepository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new BadRequestException("Coupon code already exists: " + dto.getCode());
        }
        Coupon coupon = Coupon.builder()
                .code(dto.getCode().toUpperCase().trim())
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .minimumOrderAmount(dto.getMinimumOrderAmount())
                .usageLimit(dto.getUsageLimit())
                .expiryDate(dto.getExpiryDate())
                .active(true)
                .build();
        couponRepository.save(coupon);
        logger.info("Coupon created: {}", coupon.getCode());
    }

    @Transactional
    public void toggleCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + id));
        coupon.setActive(!coupon.isActive());
        couponRepository.save(coupon);
        logger.info("Coupon {} toggled to active={}", coupon.getCode(), coupon.isActive());
    }

    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new ResourceNotFoundException("Coupon not found: " + id);
        }
        couponRepository.deleteById(id);
        logger.info("Coupon deleted: {}", id);
    }

    // Returns coupons visible to buyers: active, not expired, under usage limit,
    // and whose minimum order amount is met by the buyer's current cart total.
    public List<CouponDTO> getAvailableCoupons(BigDecimal cartTotal) {
        return couponRepository.findAll().stream()
                .filter(c -> c.isActive()
                        && !c.isExpired()
                        && !c.isUsageLimitReached()
                        && (c.getMinimumOrderAmount() == null
                        || c.getMinimumOrderAmount().compareTo(BigDecimal.ZERO) == 0
                        || cartTotal.compareTo(c.getMinimumOrderAmount()) >= 0))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CouponDTO mapToDTO(Coupon c) {
        CouponDTO dto = new CouponDTO();
        dto.setId(c.getId());
        dto.setCode(c.getCode());
        dto.setDiscountType(c.getDiscountType());
        dto.setDiscountValue(c.getDiscountValue());
        dto.setMinimumOrderAmount(c.getMinimumOrderAmount());
        dto.setUsageLimit(c.getUsageLimit());
        dto.setUsedCount(c.getUsedCount());
        dto.setExpiryDate(c.getExpiryDate());
        dto.setActive(c.isActive());
        if (!c.isActive()) dto.setStatus("Inactive");
        else if (c.isExpired()) dto.setStatus("Expired");
        else if (c.isUsageLimitReached()) dto.setStatus("Limit Reached");
        else dto.setStatus("Active");
        return dto;
    }
}