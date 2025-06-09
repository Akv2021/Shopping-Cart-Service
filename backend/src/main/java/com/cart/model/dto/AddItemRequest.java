package com.cart.model.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class AddItemRequest {
    @NotBlank(message = "Item name is required")
    private String itemName;
    private Long clientVersion;
}