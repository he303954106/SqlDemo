package com.hk.sqldemo;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hk on 2019/5/27.
 */
public class BaseDao<T> implements IBaseDao<T> {

    private SQLiteDatabase sqLiteDatabase;

    private String tableName;

    private Class<T> beanClazz;

    private Map<String, Field> cacheMap;

    private boolean isInit = false;

    protected boolean init(SQLiteDatabase sqLiteDatabase, Class<T> beanClazz) {
        this.sqLiteDatabase = sqLiteDatabase;
        this.beanClazz = beanClazz;
        if (!isInit) {

            this.tableName = beanClazz.getAnnotation(DbTable.class).value();
            if (sqLiteDatabase == null || !sqLiteDatabase.isOpen() || tableName == null) {
                return false;
            }
            sqLiteDatabase.execSQL(getCreateTableSql());
            cacheMap = new HashMap<>();
            initCacheMap();
            isInit = true;
        }
        return false;
    }

    private void initCacheMap() {
        Field[] declaredFields = beanClazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            String fieldName = declaredField.getAnnotation(DbField.class).value();
            cacheMap.put(fieldName, declaredField);
        }
    }

    private String getCreateTableSql() {
        StringBuffer sb = new StringBuffer();
        sb.append("create table if not exists ");
        sb.append(tableName + "(");
        Field[] declaredFields = beanClazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            Class<?> type = declaredField.getType();
            String fieldName = declaredField.getAnnotation(DbField.class).value();
            if (type == String.class) {
                sb.append(fieldName + " TEXT ,");
            } else if (type == Integer.class) {
                sb.append(fieldName + " INTEGER ,");
            } else if (type == Long.class) {
                sb.append(fieldName + " LONG ,");
            } else {
                continue;
            }
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(")");
        Log.e("SQL", sb.toString());
        return sb.toString();
    }

    @Override
    public long insert(T bean) {
        Map<String, String> map = getValue(bean);
        ContentValues contentValues = getContentValues(map);
        long result = sqLiteDatabase.insert(tableName, null, contentValues);
        Log.e("SQL", "成功插入" + result + "条数据");
        return result;
    }

    private ContentValues getContentValues(Map<String, String> map) {
        ContentValues contentValues = new ContentValues();
        for (String key : map.keySet()) {
            String value = map.get(key);
            if (value != null) {
                contentValues.put(key, value);
            }
        }
        return contentValues;
    }

    private Map<String, String> getValue(T bean) {
        Map<String, String> map = new HashMap<>();
        for (Field field : cacheMap.values()) {
            field.setAccessible(true);
            try {
                Object o = field.get(bean);
                if (o == null) {
                    continue;
                }
                String value = o.toString();
                String key = field.getAnnotation(DbField.class).value();
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    map.put(key, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}
