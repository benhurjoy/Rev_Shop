package com.revshop.dto;

import com.revshop.entity.User;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String businessName; // ← ADDED
    private User.Role role;
    private boolean enabled;
    private boolean blocked;
    private LocalDateTime createdAt;

    public String getInitials() {
        String f = (firstName != null && !firstName.isEmpty()) ? String.valueOf(firstName.charAt(0)) : "";
        String l = (lastName  != null && !lastName.isEmpty())  ? String.valueOf(lastName.charAt(0))  : "";
        return (f + l).toUpperCase();
    }
}