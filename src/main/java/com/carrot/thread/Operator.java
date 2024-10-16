package com.carrot.thread;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;


public class Operator implements Runnable{
    List<String> browseItems;
    List<String> rootItems;
    Set<String> visited;
    List<String> failed;
    ExecutorService es;
    String link;
    String category;
    DynamicCountDownLatch latch;
    BlockingQueue<ChromeDriver> drivePool;
    ChromeDriver driver;

    public Operator(List<String> browseItems, List<String> rootItems, Set<String> visited, List<String> failed, ExecutorService es, String link, String category, DynamicCountDownLatch latch, BlockingQueue<ChromeDriver> drivePool){
        this.browseItems = browseItems;
        this.rootItems = rootItems;
        this.visited = visited;
        this.failed = failed;
        this.es = es;
        this.link = link;
        this.category = category;
        this.latch = latch;
        this.drivePool = drivePool;
    }

    @Override
    public void run() {
        try {
//            ChromeOptions options = new ChromeOptions();
//            options.addArguments("--remote-allow-origins=*");
//            options.addArguments("--headless");
//            options.addArguments("--disable-gpu");
//            ChromeDriver driver = new ChromeDriver(options);
            driver = drivePool.take();
            System.out.println("opening: "+category+"- "+link);
            driver.get(link);
            Thread.sleep(500);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement newGroup1 = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@role='group']")));
            List<WebElement> newItems1 = wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                    newGroup1, By.cssSelector("[role='treeitem']")));
            List<String> newItemsList = new ArrayList<>();
            for (WebElement item : newItems1) {
                newItemsList.add(item.getText());
            }
            if (isSame(browseItems, newItemsList)) {
                synchronized(rootItems){
                    rootItems.add(category);
                }
                System.out.println("found: "+category+"- "+link);
                return;
            }
            List<String> allNewLinks = new ArrayList<>();
            List<String> allNewCategories = new ArrayList<>();

            int n = newItemsList.size();

            for (int i = 0; i < n; i++) {

                WebElement linkElement = wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                        newItems1.get(i), By.tagName("a"))).get(0);
                String newLink = linkElement.getAttribute("href");
                String newCategory = newItemsList.get(i);

                allNewLinks.add(newLink);
                allNewCategories.add(newCategory);
            }

            for (int i=0; i<n; i++){
                String newLink=allNewLinks.get(i);
                String newCategory=allNewCategories.get(i);

                synchronized(visited){
                    if (visited.contains(newLink)) {
                        System.out.println("Skipping " + newCategory);
                        continue;
                    }
                    visited.add(newLink);
                }
                latch.countUp();
                es.execute(new Operator(newItemsList, rootItems, visited, failed, es, newLink, newCategory, latch, drivePool));
            }
        }catch (Exception e){
            System.out.println("failed: "+category+"- "+link);
            failed.add(category+": "+link);
        }finally {
            driver.quit();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--headless=old");
            options.addArguments("--disable-gpu");
            ChromeDriver newDriver = new ChromeDriver(options);
            drivePool.offer(newDriver);
            latch.countDown();
        }
    }
    public boolean isSame(List<String> item1, List<String> item2) {
        if(item1.size()!=item2.size())
            return false;

        for (int i = 0; i < item1.size(); i++) {
            if(!item1.get(i).equals(item2.get(i))) {
                return false;
            }
        }
        return true;
    }
}
