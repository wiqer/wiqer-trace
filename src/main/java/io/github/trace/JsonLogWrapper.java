package io.github.trace;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * ：JsonLogWrapper
 *
 * @author ：李岚峰、lilanfeng、
 * @device name ：user
 * @date ：Created in 03 / 2024/1/3  11:45
 * @description：
 * @modified By：
 */
public class JsonLogWrapper {

    private Object val;

    public JsonLogWrapper(Object val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(val, SerializerFeature.WriteMapNullValue);
    }
}
