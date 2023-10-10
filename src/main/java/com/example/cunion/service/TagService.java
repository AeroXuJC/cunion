package com.example.cunion.service;

import org.apache.shiro.crypto.hash.Hash;

import java.util.ArrayList;
import java.util.HashMap;

public interface TagService {
    ArrayList<HashMap> searchTagByClassId();

}
