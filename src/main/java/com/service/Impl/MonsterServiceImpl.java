package com.service.Impl;

import com.entity.Monster;
import com.mvc.annotation.Service;
import com.service.MonsterService;

import java.util.ArrayList;
import java.util.List;

@Service
public class MonsterServiceImpl implements MonsterService {
    public List<Monster> listMonsters() {
        ArrayList<Monster> monsters = new ArrayList<>();
        monsters.add(new Monster("牛魔王", 500, "北京"));
        monsters.add(new Monster( "蜘蛛精", 200, "天津"));
        return monsters;
    }
}
