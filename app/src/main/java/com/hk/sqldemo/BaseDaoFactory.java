package com.hk.sqldemo;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by hk on 2019/5/27.
 */
public class BaseDaoFactory {

    private static BaseDaoFactory instance = new BaseDaoFactory();

    private SQLiteDatabase sqLiteDataBase;

    private String path;

    public static BaseDaoFactory getInstance() {
        return instance;
    }

    private BaseDaoFactory() {
        path = "data/data/com.hk.sqldemo/database/hk.db";
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        sqLiteDataBase = SQLiteDatabase.openOrCreateDatabase(path, null);
    }

    public <T> BaseDao getBaseDao(Class<T> beanClazz) {
        BaseDao baseDao = null;
        try {
            baseDao = BaseDao.class.newInstance();
            baseDao.init(sqLiteDataBase, beanClazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baseDao;
    }
}
