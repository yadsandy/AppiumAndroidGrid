package coreAndroid;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import io.appium.java_client.android.AndroidDriver;

public class TC_Execution {
	AndroidDriver driver;
	GenericFunctions generic = new GenericFunctions(driver);

	@BeforeMethod(firstTimeOnly = true)
	public void StartDriver() {
		try {
			driver = generic.StartDriverAndroidApp(AppPath, APP_PACKAGE, APP_ACTIVITY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterMethod(lastTimeOnly = true)
	public void stopDriver() {
		generic.StopDriver(APP_PACKAGE);
	}

	@Test
	public void TC_Execution_001() {
		System.out.println("TC_RMJ_001_CheckForP0User");
	}

	@Test
	public void TC_Execution_002() {
		System.out.println("TC_RMJ_002_CheckRMJForNormalUser");
	}
}
