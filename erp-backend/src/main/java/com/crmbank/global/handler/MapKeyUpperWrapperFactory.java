package com.crmbank.global.handler;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

import java.util.Map;

/**
 * 💡 MyBatis 결과가 Map일 경우 키를 대문자로 처리하는 Wrapper를 생성하는 Factory
 */
public class MapKeyUpperWrapperFactory implements ObjectWrapperFactory {

    @Override
    public boolean hasWrapperFor(Object object) {
        // 💡 결과 객체가 Map인 경우에만 Wrapper 적용
        return object instanceof Map;
    }

    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        return new MapKeyUpperWrapper(metaObject, (Map<String, Object>) object);
    }
}
