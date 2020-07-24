package coreAndroid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;

public class GenericFunctions {

	@SuppressWarnings("rawtypes")
	public AndroidDriver driver;
	public String browserType = "";
	public String winHandle;
	public WebDriver webDriver;
	public AppiumDriverLocalService service;
	public int port;
	public int appiumRunPort;

	public GenericFunctions() {

	}

	@SuppressWarnings("rawtypes")
	public GenericFunctions(AndroidDriver driver) {
		this.driver = driver;
	}

	/***********************************************************************************************
	 * Function Description : To start grid through build.xml
	 *********************************************************************************************/
	public static void main(String[] args) {
		GenericFunctions generic = new GenericFunctions();
		int numberOfDevicesConnected = generic.getconnectedDevicesNumber();
		String enteredDeviceNames = null;
		List<String> devices = new ArrayList<String>();
		int numberOfDevices;
		if (enteredDeviceNames == null) {
			devices = generic.getConnectedDevicesList();
			numberOfDevices = devices.size();
			numberOfDevicesConnected = numberOfDevices;
		} else {
			System.out.println("Entered Device Names: " + enteredDeviceNames);
			String[] devicesArray = enteredDeviceNames.split(",");
			List<String> devicesNames = new ArrayList<String>();
			Collections.addAll(devicesNames, devicesArray);
			numberOfDevices = devicesNames.size();
		}

		if (numberOfDevices == 0) {
			System.out.println("ERROR!!! : 'devices' value not set. It should be set to 1 or more. Exiting....");
			System.exit(1);
		} else if (numberOfDevices > numberOfDevicesConnected) {
			System.out.println("ERROR!!! : Wanted number of devices are " + numberOfDevices
					+ ". But actual connected are only " + numberOfDevicesConnected + " . Exiting....");
			System.exit(2);
		}
		if (numberOfDevices == 0) {
			System.out.println("Number of connected devices = " + numberOfDevices);
			return;
		} else if (numberOfDevices == 1) {
			System.out.println("Executing in Single Device mode");
		} else {
			System.out.println("Executing in Grid mode");
			System.out.println("Number of Connected devices = " + numberOfDevicesConnected);
			System.out.println("Number of devices on which automation needs to run on = " + numberOfDevices);
			GridDrivers grid = new GridDrivers();
			try {
				grid.StartGrid(devices);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/***********************************************************************************************
	 * Function Description : Sets implicit Wait by accepting timeout in seconds
	 * author: Tarun Narula, date: 25-Feb-2013
	 *********************************************************************************************/

	

	public AndroidDriver StartDriverAndroidApp(String appLocation, String appPackage, String appActivity)
			throws Exception {
		System.out.println("This is the device mapping file path: " + System.getenv("COMMON_RESOURCES"));
		String Platform = "";
		String AppiumHost = "";
		GridDrivers grid = new GridDrivers();
		ServerSocket se = new ServerSocket(0);
		System.out.println("listening on port: " + se.getLocalPort());
		port = se.getLocalPort();
		String ip = "127.0.0.1";// grid.GetIpAddress();
		 int numberOfDevices = getconnectedDevicesNumber();
		 
		System.out.println("Checking which OS we are running on!");
		if (numberOfDevices == 0) {
			System.out.println("Number of connected devices is ZERO");
			return null;
		} else if (numberOfDevices == 1) {
			if (SystemUtils.IS_OS_MAC) {
				service = new AppiumServiceBuilder().usingDriverExecutable(new File("/usr/local/bin/node"))
						.withAppiumJS(new File("/usr/local/bin/appium")).withIPAddress("127.0.0.1").usingAnyFreePort()
						.build();
				appiumRunPort = service.getUrl().getPort();
				service.start();
			} else {
				service = new AppiumServiceBuilder()
						.usingDriverExecutable(new File("/home/linuxbrew/.linuxbrew/bin/node"))
						.withAppiumJS(new File("/home/linuxbrew/.linuxbrew/bin/appium")).withIPAddress("127.0.0.1")
						.usingAnyFreePort().build();
				appiumRunPort = service.getUrl().getPort();
				service.start();
			}
		} else {
			System.out.println("In grid");
			Platform = "Android";
			AppiumHost = "http://" + ip + ":4444/wd/hub";
		}

		System.out.println("Launching app...");
		System.setProperty("webdriver.http.factory", "apache");
		runtimeExec("adb uninstall io.appium.uiautomator2.server");
		runtimeExec("adb uninstall io.appium.uiautomator2.server.test");
		DesiredCapabilities capabilities = new DesiredCapabilities();
		System.out.println("==set browser==");
		capabilities.setCapability("platformName", "Android");
//		capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Android");
		System.out.println("==set device==");
		capabilities.setCapability(MobileCapabilityType.APP, appLocation);
		capabilities.setCapability("appPackage", appPackage);
		capabilities.setCapability("appActivity", appActivity);
		capabilities.setCapability("automationName", "UiAutomator2");
		capabilities.setCapability("fullReset", true);
		System.out.println("==set app==");
		if (numberOfDevices != 1) {
			driver = new AndroidDriver(new URL("http://127.0.0.1:4444/wd/hub"), capabilities);
		} else {
			try {
				driver = new AndroidDriver(new URL("http://127.0.0.1:" + appiumRunPort + "/wd/hub"), capabilities);
			} catch (Exception s) {
				System.out.println("Unable to launch App...Retrying...");
				driver = new AndroidDriver(new URL("http://127.0.0.1:" + appiumRunPort + "/wd/hub"), capabilities);
			}
		}
		driver.manage().timeouts().implicitlyWait(120000, TimeUnit.MILLISECONDS);
		System.out.println("==========complete launchApp========");
		return driver;
	}

	private class RuntimeExec {
		public StreamWrapper getStreamWrapper(InputStream is, String type) {
			return new StreamWrapper(is, type);
		}

		private class StreamWrapper extends Thread {
			InputStream is = null;
			@SuppressWarnings("unused")
			String type = null;
			String message = null;

			StreamWrapper(InputStream is, String type) {
				this.is = is;
				this.type = type;
			}

			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(is));
					StringBuffer buffer = new StringBuffer();
					String line = null;
					while ((line = br.readLine()) != null) {
						buffer.append(line);// .append("\n");
					}
					message = buffer.toString();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}

		public void startAppiumUbuntu(String comand) {
			Runtime rt = Runtime.getRuntime();
			RuntimeExec rte = new RuntimeExec();
			StreamWrapper error, output;

			try {
				Process proc = rt.exec(comand);
				Thread.sleep(5000);
				error = rte.getStreamWrapper(proc.getErrorStream(), "ERROR");
				output = rte.getStreamWrapper(proc.getInputStream(), "OUTPUT");
				// int exitVal = 0;

				BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String s;
				while ((s = stdInput.readLine()) != null) {
					System.out.println(s);
					if (s.contains("Appium REST http")) {
						break;
					}
				}
				error.start();
				output.start();
				error.join(3000);
				output.join(3000);
				// exitVal = proc.waitFor();
				System.out.println("Output: " + output.message + "\nError: " + error.message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void stopAppiumUbuntu(String comand) {
			Runtime rt = Runtime.getRuntime();
			RuntimeExec rte = new RuntimeExec();
			StreamWrapper error, output;

			try {
				Process proc = rt.exec(comand);
				Thread.sleep(5000);
				error = rte.getStreamWrapper(proc.getErrorStream(), "ERROR");
				output = rte.getStreamWrapper(proc.getInputStream(), "OUTPUT");
				error.start();
				output.start();
				error.join(3000);
				output.join(3000);
				if (error.message.equals("") && output.message.equals("")) {
					// closed appium server
				} else if (error.message.contains("No matching processes belonging to you were found")) {
					// Display nothing as no instances of Appium Server were found running
				} else {
					System.out.println("Output: " + output.message + "\nError: " + error.message);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/***********************************************************************************************
	 * Function Description : Kill the appium server via commandline
	 * 
	 * @author rashi.atry, date: 19-May-2014
	 * @throws IOException, InterruptedException
	 *********************************************************************************************/

	public void stopAppiumServer(int port) throws IOException {

		Runtime.getRuntime().exec("cmd.exe");
		String AppiumServerPortNumber = Integer.toString(port);// getPropertyValue("port");
		String command = "cmd /c echo off & FOR /F \"usebackq tokens=5\" %a in" + " (`netstat -nao ^| findstr /R /C:\""
				+ AppiumServerPortNumber + "\"`) do (FOR /F \"usebackq\" %b in"
				+ " (`TASKLIST /FI \"PID eq %a\" ^| findstr /I node.exe`) do taskkill /F /PID %a)";

		String s = null;
		try {
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			// read the output from the command
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}

			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}
		} catch (IOException e) {
			System.out.println(" ---------->>> Exception happened: ");
			e.printStackTrace();
		}

		System.out.println("------------>>> Appium server stopped");
	}


	/***********************************************************************************************
	 * Function Description : Stops the driver
	 * 
	 * @author Sandeep.Yadav, date: 18-Feb-2020
	 *********************************************************************************************/

	public String StopDriver(String appPackage) {
		System.out.println("Stopping driver...");
		driver.removeApp(appPackage);
		driver.quit();
		int numberOfDevices = getconnectedDevicesNumber();
		if (SystemUtils.IS_OS_WINDOWS) {
			if (numberOfDevices == 1) {
				try {
					stopAppiumServer(port);
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("stopped driver");
			}
		} else if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX) {
			driver.quit();
//			runtimeExec("pkill -a Terminal");
			try {
				Runtime.getRuntime().exec("netstat -vanp tcp | grep " + appiumRunPort);
//				Process p = Runtime.getRuntime().exec("lsof -t -i:"+appiumRunPort);
//				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
//				String line = null;
//				String a="";
//				while ((line = in.readLine()) != null) {
//					a=line;
//				}
//				System.out.println("KILLING PROCESS "+a.trim()+" ON PORT "+ appiumRunPort);
//				Runtime.getRuntime().exec("kill -9 " + a.trim());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (numberOfDevices == 1) {
				RuntimeExec appiumObj = new RuntimeExec();
				GridDrivers grid = new GridDrivers();
				grid.stopPort(Integer.toString(appiumRunPort));
				// appiumObj.stopAppiumUbuntu("killall -9 node");
				System.out.println("stopped driver.");
			}
		}
		return ("------------>>> Browser closed");

	}

	
	/***********************************************************************************************
	 * Function Description : To get list of all connected Android devices (with
	 * UDIDs of Android Devices)
	 * 
	 * @author rashi.atry, date: 19-Oct-2015
	 *********************************************************************************************/

	public List<String> getConnectedDevicesList() {

		List<String> devicesID = new ArrayList<String>();

		String command = "adb devices";
		try {
			Process process = Runtime.getRuntime().exec(command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String s;
			while ((s = reader.readLine()) != null) {
				if (s.contains("device") && !s.contains("attached")) {
					String[] device = s.split("\t");
					devicesID.add(device[0]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return devicesID;
	}

	/***********************************************************************************************
	 * Function Description : To get list of all connected Android devices (with
	 * UDIDs of Android Devices)
	 * 
	 * @author rashi.atry, date: 19-Oct-2015
	 *********************************************************************************************/

	public int getconnectedDevicesNumber() {

		int connectDevices;
		connectDevices = this.getConnectedDevicesList().size();

		return connectDevices;
	}

	/***********************************************************************************************
	 * Function Description : To clear cache of app
	 * 
	 * @author rashi.atry, date: 02-Aug-2016
	 *********************************************************************************************/

	
	public String runtimeExec(String command) {
		String s = null;

		try {

			// Process provides control of native processes started by ProcessBuilder.start
			// and Runtime.exec.
			// getRuntime() returns the runtime object associated with the current Java
			// application.
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			// read the output from the command
			while ((s = stdInput.readLine()) != null) {
				return s;
			}

			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
				return s;
			}

		} catch (IOException e) {
			System.out.println("exception happened - here's what I know: ");
			e.printStackTrace();
		}
		return s;
	}

}