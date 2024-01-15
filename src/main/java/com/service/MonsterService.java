package com.service;

import com.entity.Monster;

import java.util.List;

public interface MonsterService {
    public List<Monster> listMonsters();
    public List<Monster> findMonsterByName(String name);
    public boolean login(String name);
}
