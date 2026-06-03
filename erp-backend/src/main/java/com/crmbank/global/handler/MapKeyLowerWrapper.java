package com.crmbank.global.handler;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.MapWrapper;

import java.util.Map;

/**
 * 💡 MyBatis 결과가 Map일 경우 키를 무조건 소문자로 처리하는 Wrapper
 */
public class MapKeyLowerWrapper extends MapWrapper {
    private final Map<String, Object> map;

    public MapKeyLowerWrapper(MetaObject metaObject, Map<String, Object> map) {
        super(metaObject, map);
        this.map = map;
    }

    @Override
    public Object get(PropertyTokenizer prop) {
        if (prop.getIndex() != null) {
            return super.get(prop);
        } else {
            // 💡 데이터를 읽을 때도 소문자 키로 조회
            return map.get(prop.getName().toLowerCase());
        }
    }

    @Override
    public void set(PropertyTokenizer prop, Object value) {
        if (prop.getIndex() != null) {
            super.set(prop, value);
        } else {
            // 💡 MyBatis가 Map에 데이터를 담을 때 키를 강제로 소문자로 변환하여 저장
            map.put(prop.getName().toLowerCase(), value);
        }
    }

    @Override
    public boolean hasSetter(String name) {
        return true;
    }

    @Override
    public boolean hasGetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.getIndex() != null) {
            return super.hasGetter(name);
        } else {
            // 💡 존재 여부 확인 시에도 소문자로 체크
            return map.containsKey(prop.getName().toLowerCase());
        }
    }
}
