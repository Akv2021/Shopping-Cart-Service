package com.cart.model.dto;

import java.util.List;

import lombok.Data;
@Data
public class SyncRequest {
    private List<PendingOperation> operations;

    @Data
    public static class PendingOperation {
        private String type;
        private String item;
        private long clientVersion;
        private String timestamp;
    }
}
