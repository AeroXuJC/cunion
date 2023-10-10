package com.example.cunion.service;

public interface CollectService {
    void addCollect(String shopId, String userId);

    void removeCollect(String shopId, String userId);
}
