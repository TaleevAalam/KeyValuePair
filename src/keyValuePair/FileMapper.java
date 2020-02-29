package keyValuePair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileMapper {

	private static List<String> lS = new ArrayList<String>();

	private FileMapper() {

	}

	public static MHashMap getMap(String fileName) throws CustomException, IOException {
		if (lS.contains(fileName)) {
			throw new CustomException("Can not create map for this file named " + fileName + " , as it is already in use");
		} else {
			lS.add(fileName);
			return new MHashMap(fileName);
		}

	}

	public static MHashMap getMap() throws CustomException, IOException {

		String defaultLocation = System.getProperty("user.dir") + "\\keyValuePair.txt";
		return getMap(defaultLocation);

	}

	public static class MHashMap {

		private static boolean updated = false;
		private HashMap<String, ValueObject> hM = new HashMap<String, ValueObject>();
		private String fileLocation;
		private File file;
		private final static String EMPTY_STRING = "";
		private long fileLength;

		private MHashMap() throws CustomException, IOException {
			fileLocation = System.getProperty("user.dir") + "\\keyValuePair.txt";
			loadTheFile();
		}

		private synchronized long fileLengthInc(long value) {
			fileLength = fileLength + value;
			return fileLength;
		}

		@SuppressWarnings("unchecked")
		private MHashMap(String fileLocation) throws CustomException, IOException {
			this.fileLocation = fileLocation;
			loadTheFile();
		}

		@SuppressWarnings("unchecked")
		private synchronized void loadTheFile() throws CustomException, IOException {
			file = new File(fileLocation);
			ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(new FileInputStream(file));
				try {
					hM = (HashMap<String, ValueObject>) in.readObject();
					fileLength = file.length();
				} catch (Exception e) {
					in.close();
					file.deleteOnExit();
					throw new CustomException("File you are trying to load is curropted");
				}
			} catch (IOException e) {
				file.createNewFile();
			}
		}

		public synchronized void put(String key, String value, String timeLimit) throws CustomException, IOException {

			if (getKeyLength(key) > 32)
				throw new CustomException("Size limit of 32 characters exceeds, can not insert for ke :"+key);

			if (get(key) != null)
				throw new CustomException("Value already exists for "+key);

			if (getSizeInKiloByte(value.getBytes().length) > 16)
				throw new CustomException("Value limit of 16 KB exceeds, can not insert for key :"+ key );

			ValueObject object = new ValueObject(key, value, timeLimit);
			if (getSizeInMegaByte(fileLength + object.getSize()) > 1)
				throw new CustomException("File size limit of 1 GB will exceed, can not insert for key :"+key);
			else
				fileLength = fileLengthInc(object.getSize());
			hM.put(key, object);
		}

		private double getSizeInMegaByte(double size) {
			return size / (1024 * 1000 * 1000);
		}

		private double getSizeInKiloByte(long size) {
			return size / 1000;
		}

		private int getKeyLength(String key) {
			return key.length();
		}

		public synchronized String get(String key) throws IOException, CustomException {
			if (updated)
				loadTheFile();

			ValueObject valueObject = hM.get(key);
			if (valueObject != null) {
				if (isExpired(valueObject)) {
					delete(valueObject);
					return null;
				}
				return valueObject.value;

			}
			return null;
		}

		private void delete(ValueObject object) {
			fileLength = fileLength - object.getSize();
			hM.remove(object.key);
		}

		public synchronized void delete(String key) {
			ValueObject object = hM.get(key);
			if (object != null) {
				fileLength = fileLength - object.getSize();
				hM.remove(key);
			}
		}

		private boolean isExpired(ValueObject valueObject) {
			return Duration.between(Instant.ofEpochSecond(valueObject.creationTime), Instant.now())
					.getSeconds() > valueObject.timeLimit;
		}

		public synchronized void flush() {
			
			Thread flashThread = new Thread(()->{
				ObjectOutputStream Ob;
				try {
					Ob = new ObjectOutputStream(new FileOutputStream(new File(fileLocation)));
					Ob.writeObject(hM);
					Ob.close();

				} catch (IOException e) {
				}
				updated = true;
			});
			flashThread.setPriority(Thread.MIN_PRIORITY);
			flashThread.start();
		}

		static class ValueObject implements Serializable {

			private static final long serialVersionUID = 1L;
			String value;
			long timeLimit;
			long creationTime;
			String key;

			public ValueObject(String key, String value, String time) throws CustomException {
				this.value = value;
				this.key = key;
				creationTime = Instant.now().getEpochSecond();
				if (time.matches("\\d+[s]")) {
					timeLimit = Long.parseLong(time.replace("s", EMPTY_STRING));
				} else if (time.matches("\\d+[m]")) {
					timeLimit = (60 * Long.parseLong(time.replace("m", EMPTY_STRING)));
				} else if (time.matches("\\d+[h]")) {
					timeLimit = (360 * Long.parseLong(time.replace("h", EMPTY_STRING)));
				} else {
					throw new CustomException("Plesae enter valid time in e.g 5s, 5m, 5h for key "+key);
				}

			}

			private long getSize() {
				return key.getBytes().length + value.getBytes().length;
			}
		}

	}


	public static class CustomException extends Exception {
		private static final long serialVersionUID = 1L;

		public CustomException(String message) {
			super(message);
		}
		
		public CustomException() {
			super();
		}
		

	}
	
}
