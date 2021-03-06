package com.wubaoguo.springboot.manage.service;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.wubaoguo.springboot.manage.controller.commond.CondCacheCommond.CondCacheCommond;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wustrive.java.common.util.StringUtil;
import org.wustrive.java.core.request.BaseState;
import org.wustrive.java.core.request.StateMap;
import org.wustrive.java.dao.jdbc.dao.BaseDao;
import org.wustrive.java.dao.jdbc.dao.QuerySupport;

import com.google.common.collect.ImmutableMap;
import com.wubaoguo.springboot.constant.ShiroConstants;
import com.wubaoguo.springboot.entity.SysDictionary;
import com.wubaoguo.springboot.redis.SysDictionaryCache;
import com.wubaoguo.springboot.util.BeanUtil;

@Service
public class SysDictionaryService {

    @Autowired
    QuerySupport querySupport;
    @Autowired
    BaseDao baseDao;
    @Autowired
    SysDictionaryCache sysDictionaryCache;

    /**
     * 查询单个字典内容
     *
     * @param tag_code
     * @return
     */
    public String findContent(String tag_code) {
        List<String> list = baseDao.queryForList("select content from sys_dictionary where tag_code=:code ", ImmutableMap.of("code", tag_code), String.class);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public List<Map<String, Object>> findSysDictionaryByComond(CondCacheCommond commond) {
        commond.setPager(true);
        return querySupport.find("select * from sys_dictionary where 0=0 ", Maps.newHashMap(), commond);
    }

    public BaseState saveSysDictionary(SysDictionary sysDictionary) {
        SysDictionary dbSysDictionary = new SysDictionary().setTag_code(sysDictionary.getTag_code()).queryForBean();
        if (dbSysDictionary != null && StringUtil.isNotBlank(dbSysDictionary.getId())) {
            return new BaseState(StateMap.S_CLIENT_WARNING, "词典编码重复");
        } else {
            sysDictionary.setAdd_time(ShiroConstants.currentTimeSecond());
            sysDictionaryCache.sync(sysDictionary);
            sysDictionary.insert();
            return new BaseState();
        }
    }

    public BaseState updateSysDictionary(SysDictionary sysDictionary) {
        sysDictionary.update();
        return new BaseState();
    }

    public String updateSysDictionary(String id, String column, String value) {
        SysDictionary sysDictionary = new SysDictionary();
        sysDictionary.setId(id);
        switch (column) {
            case "title":
                sysDictionary.setTitle(value);
                break;
            case "description":
                sysDictionary.setDescription(value);
                break;
            case "content":
                sysDictionary.setContent(value);
                break;
        }
        // 修改 同步到缓存
        SysDictionary dbSysDictionary = new SysDictionary(id).queryForBean();
        BeanUtil.copyPropertiesIgnoreNull(sysDictionary, dbSysDictionary);
        sysDictionaryCache.sync(dbSysDictionary);

        updateSysDictionary(sysDictionary);
        return value;
    }
}
