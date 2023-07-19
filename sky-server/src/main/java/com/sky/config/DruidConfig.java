package com.sky.config;

/*
 * 解决druid 日志报错：discard long time none received connection:xxx
 * */

import javax.annotation.PostConstruct;

public class DruidConfig {
    @PostConstruct
    public void setProperties() {
        System.setProperty("druid.mysql.usePingMethod", "false");
    }
}





