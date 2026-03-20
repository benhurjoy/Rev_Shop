package com.revshop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    // ── NEW: variant flags ─────────────────────────────────────
    private Boolean hasColors = false;
    private Boolean hasSizes  = false;

    public boolean isHasColors() { return Boolean.TRUE.equals(hasColors); }
    public boolean isHasSizes()  { return Boolean.TRUE.equals(hasSizes);  }
    // ──────────────────────────────────────────────────────────
}