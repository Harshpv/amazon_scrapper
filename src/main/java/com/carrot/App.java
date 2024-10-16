package com.carrot;

/**
 * Hello world!
 *
 */

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.time.Duration;
import java.util.Set;

public class App 
{
    static List<String> list;
    static Set<String> set;
    static List<String> failed;

    public static void main( String[] args )
    {
        System.setProperty("webdriver.chrome.driver", "D:\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        ChromeDriver driver = new ChromeDriver(options);
        driver.get("https://www.amazon.in/gp/bestsellers");
        WebElement groupElement = driver.findElement(By.xpath("//*[@role='group']"));
//        System.out.println(groupElement.getText());
        List<WebElement> items = groupElement.findElements(By.cssSelector("[role='treeitem']"));
        List<String> itemsList = new ArrayList<>();
        for (WebElement item : items) {
            itemsList.add(item.getText());
        }
//        System.out.println(itemsList);
        list = new ArrayList<>();
        set = new HashSet<>();
        failed = new ArrayList<>();
        func(itemsList, driver);
        System.out.println("final list: "+list);
        System.out.println("failed: "+failed);

    }
    public static void func(List<String> itemsList, ChromeDriver driver) {
        int size = itemsList.size();
        System.out.println("parent: "+ itemsList);
        for (int i = 0; i < size; i++) {
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
//            WebElement newGroup1 = driver.findElement(By.xpath("//*[@role='group']"));
                WebElement newGroup1 = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@role='group']")));
//            List<WebElement> newItems1 = newGroup1.findElements(By.cssSelector("[role='treeitem']"));
                List<WebElement> newItems1 = wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                        newGroup1, By.cssSelector("[role='treeitem']")));

//            WebElement linkElement = newItems1.get(i).findElement(By.tagName("a"));
                WebElement linkElement = wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                        newItems1.get(i), By.tagName("a"))).get(0);
                if (set.contains(linkElement.getAttribute("href"))) {
                    System.out.println("Skipping " + newItems1.get(i).getText());
                    continue;
                }

                System.out.println("clicking " + newItems1.get(i).getText());
                set.add(linkElement.getAttribute("href"));
                linkElement.click();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
//            WebElement newGroup = driver.findElement(By.xpath("//*[@role='group']"));
                WebElement newGroup = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@role='group']")));

//            List<WebElement> newItems = newGroup.findElements(By.cssSelector("[role='treeitem']"));
                List<WebElement> newItems = wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                        newGroup, By.cssSelector("[role='treeitem']")));
                List<String> newItemsList = new ArrayList<>();

                for (WebElement item : newItems) {
                    newItemsList.add(item.getText());
                }
                System.out.println("Child: " + newItemsList);
//                set.add(itemsList.get(i));
                if (isSame(itemsList, newItemsList)) {
                    list.add(newItems.get(i).getText());
                } else {
                    func(newItemsList, driver);
                }
//            set.add(itemsList.get(i));
                System.out.println("back to parent");
                driver.navigate().back();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }catch (Exception e) {
                failed.add(itemsList.get(i)+" "+e.getMessage());
            }
        }
    }
    public static boolean isSame(List<String> item1, List<String> item2) {
        for (int i = 0; i < item2.size(); i++) {
            if(item1.size()!=item2.size() || !item1.get(i).equals(item2.get(i))) {
                return false;
            }
        }
        return true;
    }
}
