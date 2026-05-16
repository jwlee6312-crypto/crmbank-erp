package com.crmbank.global.handler;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.MapWrapper;

import java.util.Map;

/**
 * 💡 MyBatis 결과 Map의 Key를 강제로 대문자로 변환하는 Wrapper
 */
public class MapKeyUpperWrapper extends MapWrapper {

    private final Map<String, Object> mapRef;

    public MapKeyUpperWrapper(MetaObject metaObject, Map<String, Object> map) {
        super(metaObject, map);
        this.mapRef = map; // 💡 Map 객체의 참조를 직접 보관하여 접근 문제 해결
    }

    @Override
    public void set(PropertyTokenizer prop, Object value) {
        if (prop.getIndex() != null) {
            Object collection = resolveCollection(prop, mapRef);
            setCollectionValue(prop, collection, value);
        } else {
            // 💡 [핵심] 키 이름을 대문자로 변환하여 저장 (프론트엔드 노출 보장)
            mapRef.put(prop.getName().toUpperCase(), value);
        }
    }
}
