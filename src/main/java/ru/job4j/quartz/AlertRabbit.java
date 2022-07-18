package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    public static void main(String[] args) throws Exception {
        Properties config = initConfig();
        Connection connection = initConnection(config);
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(config.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
        }
    }

    private static Connection initConnection(Properties config)
            throws ClassNotFoundException, SQLException {
        Class.forName(config.getProperty("driver-class-name"));
        try (Connection cn = DriverManager.getConnection(
                config.getProperty("url"),
                config.getProperty("username"),
                config.getProperty("password")
        )) {
            return cn;
        } catch (SQLException e) {
            throw new SQLException(e);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    private static Properties initConfig() {
        Properties config;
        try (InputStream in = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            config = new Properties();
            config.load(in);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        return config;
    }
}