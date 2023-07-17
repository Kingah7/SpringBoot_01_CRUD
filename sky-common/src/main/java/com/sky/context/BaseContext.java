package com.sky.context;

import java.lang.reflect.Type;

public class BaseContext {

    public static ThreadLocal<Object> threadLocal = new ThreadLocal<>();

    public static void setThreadLocal(Object data) {
        threadLocal.set(data);
    }

    public static <E> E getThreadLocal(Class<E> type) {
        E data = (E) threadLocal.get();
        return data;
    }

    public static void removeThreadLocal() {
        threadLocal.remove();
    }
}
