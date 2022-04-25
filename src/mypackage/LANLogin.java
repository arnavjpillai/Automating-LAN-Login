package mypackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class LANLogin {

	private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

	public static void main(String[] args) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(11);

		scheduler.scheduleWithFixedDelay(new Task(), 0, 1, TimeUnit.HOURS);
	}

}

class Task implements Runnable {

	@Override
	public void run() {
		ChromeOptions options = new ChromeOptions();
		options.setHeadless(true);
		options.setPageLoadStrategy(PageLoadStrategy.EAGER);

		System.setProperty("wdm.cachePath", "Driver");
		WebDriverManager.chromedriver().setup();

		WebDriver driver = new ChromeDriver(options);

		try {
			File outputFile = new File("log.txt");
			File inputFile = new File("LoginDetails.txt");
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true));

			login(driver, reader);
			writer.write(java.time.LocalDateTime.now().toString() + "\n");
			reader.close();
			writer.close();
			
			checkFileLineCount();
		} catch (IOException e) {
			e.printStackTrace();
		}

		driver.quit();
	}

	private void checkFileLineCount() throws IOException {
		Path file = Paths.get("log.txt");
		long count = Files.lines(file).count();

		if(count > 5) {
			File inputFile = new File("log.txt");
			File tempFile = new File("templog.txt");
			if(tempFile.exists() == false)
				tempFile.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			
			reader.readLine();
			String s;
			while((s = reader.readLine()) != null)
				writer.write(s + System.getProperty("line.separator"));
			
			reader.close();
			writer.close();
			inputFile.delete();
			tempFile.renameTo(inputFile);
		}
	}

	private void login(WebDriver driver, BufferedReader reader) throws IOException {
		String username = reader.readLine();
		String password = reader.readLine();

		driver.get("http://172.16.0.30:8090/httpclient.html");

		driver.findElement(By.id("username")).sendKeys(username);
		driver.findElement(By.id("password")).sendKeys(password);

		driver.findElement(By.id("loginbutton")).click();

		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			System.out.println(e);
		}
	}

}
