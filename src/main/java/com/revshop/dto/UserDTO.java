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
    private User.Role role;
    private boolean enabled;
    private boolean blocked;
    private LocalDateTime createdAt;
}