package com.sky.task;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.sql.SQLException;

import static com.sky.task.CronConstant.MYSQL_UPDATE_TIME;

@Component
@Slf4j
public class SqlUpdate {

    @Resource
    private DruidDataSource dataSource;

    @Scheduled(cron = MYSQL_UPDATE_TIME)
    public void refreshSql() {
        try {
            DruidPooledConnection coon = dataSource.getConnection();
            log.info("刷新数据库:", coon);

            coon.createStatement().execute("select version()");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
