package com.youtube.com.old;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

public class youtubeTestH {
	WebDriver driverYoutube;
	WebDriver driverSubtitle;
	// -------------- inputs are here:--------------------//

	Scanner sc = new Scanner(System.in);
	String searchTerm ;
	
	int expectedSubtitleNumber;

	// Links what program will use:
	String urlYoutube = "http://www.youtube.com";
	String urlSubtitle = "http://www.diycaptions.com/get-automatic-captions-as-text/";

	List<WebElement> searchResultList;

	@BeforeClass
	public void setUp() {
		 System.out.println("Enter Search Term");
		 searchTerm =sc.nextLine();
		 System.out.println("Enter Expected Number Of Subtitles");
		 expectedSubtitleNumber = 0;
		 while(expectedSubtitleNumber <=0) {
		 System.out.println("Enter positive number");
		 expectedSubtitleNumber=sc.nextInt();
		 }

		WebDriverManager.chromedriver().setup();
		driverYoutube = new ChromeDriver();
		driverYoutube.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		driverYoutube.manage().window().maximize();

		driverSubtitle = new ChromeDriver();
		driverSubtitle.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		driverSubtitle.manage().window().maximize();

	}

	@Test(priority = 1)
	public void getYoutubeSubtitles() throws InterruptedException, IOException {
		driverYoutube.get(urlYoutube);

		WebElement searchBox = driverYoutube.findElement(By.id("search"));
		searchBox.sendKeys(searchTerm);
		searchBox.sendKeys(Keys.ENTER);
		Thread.sleep(1000);

		searchResultList = driverYoutube
				.findElements(By.xpath(".//a[@class ='yt-simple-endpoint style-scope ytd-video-renderer']"));

		int count = 0;
		for (int i = 0; i < searchResultList.size() && count < expectedSubtitleNumber; i++) {
			if (checkSubstitleExistence(searchResultList.get(i).getAttribute("href"),
					searchResultList.get(i).getAttribute("title"))) {
				count++;
			}
			if (count < expectedSubtitleNumber && i == (searchResultList.size() - 1)) {
				driverYoutube.findElement(By.xpath(".//a[@class ='yt-simple-endpoint style-scope ytd-video-renderer']"))
						.sendKeys(Keys.END);
				Thread.sleep(1000);
				searchResultList = driverYoutube
						.findElements(By.xpath(".//a[@class ='yt-simple-endpoint style-scope ytd-video-renderer']"));
			}
		}
	}

	@AfterClass
	public void tearDown() throws InterruptedException {
		Thread.sleep(2000);
		driverYoutube.quit();
		driverSubtitle.quit();
	}

	private boolean checkSubstitleExistence(String link, String title) throws InterruptedException, IOException {

		driverSubtitle.get(urlSubtitle);
		Thread.sleep(1000);
		WebElement searchBox = driverSubtitle.findElement(By.id("urlidsrt"));
		searchBox.sendKeys(link);
		searchBox.sendKeys(Keys.ENTER);
		Thread.sleep(1000);
		handleNextWindow();
		boolean SubEngButton = driverSubtitle.getPageSource().contains("English (auto-generated)");
		System.out.println(title + "    has subtitle    : " + SubEngButton);
		if (SubEngButton) {
			driverSubtitle.findElement(By.xpath("//a[contains(text(),'English (auto-generated)')]")).click();
			Thread.sleep(1000);
			handleNextWindow();
			fileWriter(generatePath(title), driverSubtitle.findElement(By.xpath("//div[@class='well']")).getText());

			return true;
		} else {
			return false;
		}

	}

	private void fileWriter(String path, String text) throws IOException {

		String prePathOutput = ".//output//";

		File newfile = new File(prePathOutput + path + ".doc");
		String fullFileName = newfile.toString();
		// System.out.println(fullFileName);
		BufferedWriter writer = new BufferedWriter(new FileWriter(newfile));

		writer.write("Title : " + path);
		writer.newLine();
		writer.write(text);
		writer.flush();
		writer.close();
	}

	private String generatePath(String title) {

		String fileName = title.replaceAll(" ", "_");
		fileName = fileName.replaceAll("[^A-Za-z0-9_]", "");
		return fileName;
	}

	private void handleNextWindow() {
		//String parentWindowHandler = driverSubtitle.getWindowHandle();
		String subWindowHandler = null;

		Set<String> handles = driverSubtitle.getWindowHandles();
		Iterator<String> iterator = handles.iterator();
		while (iterator.hasNext()) {
			subWindowHandler = iterator.next();
		}

		driverSubtitle.switchTo().window(subWindowHandler);
	}

}