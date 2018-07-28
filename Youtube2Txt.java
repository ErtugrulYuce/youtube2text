package com.youtube.com.old;

import static org.testng.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

public class Youtube2Txt {
	WebDriver driver;
	String urlYoutube = "http://www.youtube.com";
	String urlSubtitle = "http://www.lilsubs.com/";
	String searchTerm = "engliseh toelf preperation";
	String txtName = "";
	List<String> searchlinks = new ArrayList<String>();
	List<String> searchTitlies = new ArrayList<String>();
	List<WebElement> searchList;
	List<String> downloadedTitles = new ArrayList<String>();
	List<String> fileNames = new ArrayList<String>();

	@BeforeClass
	public void setUp() {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		driver.manage().window().maximize();
	}

	@Test(priority = 1)
	public void get10Youtubelinks() throws InterruptedException {
		driver.get(urlYoutube);
		WebElement searchBox = driver.findElement(By.id("search"));
		searchBox.sendKeys(searchTerm);
		searchBox.sendKeys(Keys.ENTER);
		Thread.sleep(1000);

		// System.out.println(driver.findElement(By.xpath("(.//a[@class
		// ='yt-simple-endpoint style-scope
		// ytd-video-renderer'])[1]")).getAttribute("href"));

		searchList = driver.findElements(By.xpath(".//a[@class ='yt-simple-endpoint style-scope ytd-video-renderer']"));

		for (int i = 1; i <= 5; i++) {
			searchlinks.add(searchList.get(i).getAttribute("href"));
			searchTitlies.add(searchList.get(i).getAttribute("title"));

		}
		// System.out.println(searchlinks);
		// System.out.println(searchTitlies);
		int expectedLinksNumber = 5;

		Assert.assertEquals(searchlinks.size(), expectedLinksNumber);
	}

	@Test(priority = 2)
	public void downloadAvailableSubtitles() throws InterruptedException {
		for (int i = 0; i < searchlinks.size(); i++) {
			driver.get(urlSubtitle);
			WebElement searchBox = driver.findElement(By.id("link"));
			searchBox.sendKeys(searchlinks.get(i));
			searchBox.sendKeys(Keys.ENTER);
			boolean SubEngButton = driver.getPageSource().contains("English (auto-generated) Subtitle");
			System.out.println(searchTitlies.get(i) + "    has subtitle    : " + SubEngButton);
			if (SubEngButton) {
				downloadedTitles.add(searchTitlies.get(i));
				driver.findElement(By.xpath("//button[.='English (auto-generated) Subtitle']")).click();

			}
		}

	}

	@Test(priority = 3)
	public void generateFilePaths() {
		for (int i = 0; i < downloadedTitles.size(); i++) {
			String fileName = downloadedTitles.get(i).replaceAll("&", "");
			//fileName = downloadedTitles.get(i).replaceAll("[^A-Za-z0-9]", "");
			fileName = "[LilSubs.com]_" + downloadedTitles.get(i).replaceAll(" ", "_") + "_English_(Auto-generated)";
			fileNames.add(fileName);
		}
		for (String e : fileNames) {
			System.out.println(e);
		}
	}

	@Test(priority = 4)
	public void convert2Txt() throws IOException {
		for (int i = 0; i < fileNames.size(); i++) {
			txtGenerator(fileNames.get(i));
		}
	}

	@AfterClass
	public void tearDown() throws InterruptedException {
		Thread.sleep(2000);
		driver.quit();
	}

	public void txtGenerator(String path) throws IOException {
		String prePathDownload = "C://Users//Asus//Downloads//";
		String prePathOutput = ".//output//";
		// String path =
		// "[LilSubs.com]_Selenium_Interview_Question_for_Fresher_and_Experienced_Part_-2_Advanced_Selenium_English_(Auto-generated)";
		File newfile = new File(prePathOutput + path + ".txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(newfile));
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(prePathDownload + path + ".srt"), "UTF-8"));
		String line = reader.readLine();
		line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			if (line.equals("")) {
				reader.readLine();
				reader.readLine();
				continue;
			}
			// System.out.println(line);
			writer.write(line);
			writer.newLine();
		}
		writer.flush();
		writer.close();
		reader.close();
	}

}
