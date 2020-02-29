package keyValuePair;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import keyValuePair.FileMapper.CustomException;
import keyValuePair.FileMapper.MHashMap;

public class SimpleInputTest {

	public static MHashMap M = null;

	@BeforeClass
	public static void beforeClass() throws CustomException, IOException {
		// initializing the file
		M = FileMapper.getMap();
	}

	@Test
	public void simpleInputTest() throws CustomException, IOException {
		String name = "name";
		String value = "taleev";

		// putting detail for the file
		M.put(name, value, "15s");

		// flushing the detail into file
		M.flush();

		// test agains file and local
		assertEquals(M.get(name), value);
	}
	
	@Test(expected=CustomException.class)
	public void illegalTimeLimitTest() throws CustomException, IOException {
		M.put("name", "value", "5ss");
	}

	/*
	 * Test method for size limit of key which is 32 character
	 */
	@Test(expected = CustomException.class)
	public void keySizeTest() throws CustomException, IOException{
		M.put("sdssssssssssssssssssssssssssssssssssssssssssssssssssss", "throughException", "5s");
		M.flush();
	}

	/*
	 * Test method for size limit of value which is 16kb
	 */

	@Test(expected = CustomException.class)
	public void ValueSizeTest() throws CustomException, IOException {
		String fileName = "fileForInvalidValueTest.txt";
		M.put("nameS", new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "\\" + fileName))),
				"5s");
		M.flush();
	}


	// Value already exists test

	@Test(expected = CustomException.class)
	public void valueAlredyExistTest() throws CustomException, IOException  {
		M.put("name", "value", "5s");
	}
	
	@Test
	public void expiryTest() throws InterruptedException, IOException, CustomException{

		// entering value with expiry of 5 second
		M.put("forExpiry", "forExpiry", "2s");
		Thread.sleep(6000);
		String gettingValueAfter5Second = M.get("forExpiry");
		assertEquals(null, gettingValueAfter5Second);

	}

	/*
	 * Test method for file size limit which is 1GB
	 */

	@Test(expected = CustomException.class)
	@Ignore
	public void FileSizeTest()
			throws CustomException, IOException {
		String fileName = "fileForFileSizeTest.txt";

		for (int i = 1; i < 1000000000; i++) {
			try {
				M.put("nameM" + i,
						new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "\\" + fileName))),
						"5s");
			} catch (CustomException e) {
				M.flush();
				throw new CustomException();
			}
		}
	}
	
}
