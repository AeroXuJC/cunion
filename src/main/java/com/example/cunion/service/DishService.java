package com.example.cunion.service;

import java.util.ArrayList;
import java.util.HashMap;

public interface DishService {
    ArrayList<HashMap> searchDishesByShopId(String shopId);
}
