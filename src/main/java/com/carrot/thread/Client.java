package com.carrot.thread;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.*;
import java.util.concurrent.*;

public class Client {
    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "D:\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");
        int n=7;
        ExecutorService es= Executors.newFixedThreadPool(10);
        List<String> browseItems=new ArrayList<>();
        List<String> rootItems=new ArrayList<>();
        Set<String> visited=new HashSet<>();
        List<String> failedItems=new ArrayList<>();
        String link= "https://www.amazon.in/gp/bestsellers";
        DynamicCountDownLatch latch=new DynamicCountDownLatch(1);
        BlockingQueue<ChromeDriver> driverPool=new LinkedBlockingQueue<>(n);
        for(int i=0;i<n;i++){
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--headless=old");
            options.addArguments("--disable-gpu");
            // options.addArguments("--window-position=-2400,-2400");
            driverPool.add(new ChromeDriver(options));
        }
        System.out.println("Starting now: "+ new Date());
        es.execute(new Operator(browseItems, rootItems, visited, failedItems, es, link, "", latch, driverPool));

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            for(ChromeDriver driver:driverPool){
                driver.quit();
            }
        }

        System.out.println(rootItems);
        System.out.println(failedItems);
        System.out.println("Completed: "+new Date());
        es.shutdown();
    }
}
