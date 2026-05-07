package com.wms.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private PageInfo pageInfo;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> success(T data, PageInfo pageInfo) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("操作成功");
        response.setData(data);
        response.setPageInfo(pageInfo);
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    @Data
    public static class PageInfo {
        private long total;
        private int page;
        private int size;
        private int totalPages;

        public PageInfo(long total, int page, int size, int totalPages) {
            this.total = total;
            this.page = page;
            this.size = size;
            this.totalPages = totalPages;
        }
    }
}
