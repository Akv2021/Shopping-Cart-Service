package com.cart.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SyncResponse {
    private String status;
    private long version;
    private int syncedOperations;
}
