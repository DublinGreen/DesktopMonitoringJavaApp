package com.cliquedom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {

	private static String pathToListen;
	private static File fileObj;
	private static String fileString;

	public static void main(String[] args) {

		JSONParser jsonParser = new JSONParser();
		try (FileReader reader = new FileReader("configuration.json")) {
			Object obj = jsonParser.parse(reader);
			JSONObject configurationObject = (JSONObject) obj;
			pathToListen = parseConfigurationJsonFile(configurationObject);

			System.out.println(pathToListen);

		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		}

		if (!pathToListen.isEmpty()) {
			Path watchPathForCreate = Paths.get(pathToListen);
			watchForCreateEvent(watchPathForCreate, pathToListen);
		} else {
			System.out.println("Path to listen is empty");
		}

	}

	private static String parseConfigurationJsonFile(JSONObject configuration) {
		JSONObject configurationObject = (JSONObject) configuration.get("configuration");
		return (String) configurationObject.get("pathToListen");
	}

	private static void watchForCreateEvent(Path watchPathForCreate, String path) {
		try {
			for (;;) {
				WatchService watcher = watchPathForCreate.getFileSystem().newWatchService();
				watchPathForCreate.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);

				System.out.println("Monitoring directory for create changes on ... " + path);

				WatchKey watchKey = watcher.take();
				List<WatchEvent<?>> events = watchKey.pollEvents();
				for (WatchEvent<?> event : events) {
					System.out.println(event.kind());
					if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
						System.out.println("Created: " + event.context().toString());

						fileString = path + "/" + event.context().toString();
						fileObj = new File(fileString);

						if (fileObj.exists() && !fileObj.isDirectory()) {
							String extenstion = FilenameUtils.getExtension(fileString);
							switch (extenstion) {
							case "txt":
								System.out.println(fileString);
								System.out.println(path + "/" + "documents/");
								
								try {
									//if file already exist
									// add number to the file
								Files.move(Paths.get(fileString), Paths.get(path + "/" + "documents/" + event.context().toString()), 
										StandardCopyOption.REPLACE_EXISTING);
								}catch(IOException e) {
									System.out.println(e.getMessage());
								}

								break;
							default:
								break;
							}

						}

					}
				}

				boolean valid = watchKey.reset();
				if (!valid) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
