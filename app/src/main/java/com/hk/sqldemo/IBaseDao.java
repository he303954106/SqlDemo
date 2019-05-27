package com.hk.sqldemo;

/**
 * Created by hk on 2019/5/27.
 */
public interface IBaseDao<T> {

    long insert(T bean);
}
