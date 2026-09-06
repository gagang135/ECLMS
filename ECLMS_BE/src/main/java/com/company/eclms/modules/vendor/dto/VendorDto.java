package com.company.eclms.modules.vendor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDto {

    private UUID id;

    @NotBlank(message = "Vendor name is required")
    @Size(max = 255, message = "Vendor name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "Vendor email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @Size(max = 50, message = "Phone number cannot exceed 50 characters")
    private String phone;

    private String address;

    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", 
             message = "Invalid GST number format")
    private String gstNumber;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", 
             message = "Invalid PAN format")
    private String panNumber;

    @Pattern(regexp = "^(LOW|MEDIUM|HIGH)$", 
             message = "Risk level must be LOW, MEDIUM, or HIGH")
    private String riskLevel;
}
