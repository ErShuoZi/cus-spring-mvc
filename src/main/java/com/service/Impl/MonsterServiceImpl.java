package com.service.Impl;

import com.entity.Monster;
import com.mvc.annotation.Service;
import com.service.MonsterService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class MonsterServiceImpl implements MonsterService {
    @Override
    public List<Monster> listMonsters() {
        ArrayList<Monster> monsters = new ArrayList<>();
        monsters.add(new Monster("牛魔王", 500, "北京"));
        monsters.add(new Monster("蜘蛛精", 200, "天津"));
        return monsters;
    }

    @Override
    public List<Monster> findMonsterByName(String name) {
        ArrayList<Monster> monsters = new ArrayList<>();
        monsters.add(new Monster("牛魔王", 500, "北京"));
        monsters.add(new Monster("蜘蛛精", 200, "天津"));

        //创建查询到的monster集合
        ArrayList<Monster> findedMonster = new ArrayList<>();
        for (Monster monster : monsters) {
            //返回满足条件的
            if (monster.getName().contains(name)) {
                findedMonster.add(monster);
            }
        }
        return findedMonster;
    }
}
