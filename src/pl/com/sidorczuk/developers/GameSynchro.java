package pl.com.sidorczuk.developers;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.apache.commons.io.FileUtils;

public class GameSynchro {

	static String destin_folder = "";
	static String stringSystemSeparator = File.separator;
	static char charSystemSeparator = stringSystemSeparator.charAt(0);

	public static void main(String[] args) throws IOException {
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		Image image = Toolkit.getDefaultToolkit().getImage("trayico.png");

		final PopupMenu popup = new PopupMenu();
		final TrayIcon trayIcon = new TrayIcon(image, "GameSynchroSave", popup);
		final SystemTray tray = SystemTray.getSystemTray();

		MenuItem exitItem = new MenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(1);
			}
		});
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
		}
		String source_file_name = "source_paths.txt";
		String destin_file_name = "destin_path.txt";
		Scanner scan_in_create_file = new Scanner(System.in);
		File spath = new File(source_file_name);
		if(!spath.exists()) {
			createFile(source_file_name, "source", scan_in_create_file );			
		}
		File dpath = new File(destin_file_name);
		if(!dpath.exists()) {
			createFile(destin_file_name, "destination", scan_in_create_file );			
		}	
		scan_in_create_file.close();
		HashMap<String, String> input_data = new HashMap<>();
		Scanner scan;
		try {
			scan = new Scanner(new File(source_file_name));
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				StringTokenizer st = new StringTokenizer(line, ";");
				ArrayList<String> tokenizery = new ArrayList<>();
				while (st.hasMoreTokens()) {
					tokenizery.add(st.nextToken());
				}
				input_data.put(tokenizery.get(0), tokenizery.get(1));
			}
			scan.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}
		Scanner scan2;
		try {
			scan2 = new Scanner(new File(destin_file_name));
			while (scan2.hasNextLine()) {
				destin_folder = scan2.nextLine();
			}
			if (destin_folder == "") {
				System.out.println("Wrong path to destination folder. Exiting...");
				System.exit(1);
			}
			Path destin_folder_path = Paths.get(destin_folder);
			if (!Files.exists(destin_folder_path)) {
				boolean success = (new File(destin_folder)).mkdirs();
				if (!success) {
					System.out.println("Destination folder does not exist and I can't create path to this folder. Exiting...");
					System.exit(1);
				}
			}
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			System.exit(1);
		}		
		char lastChar = destin_folder.charAt(destin_folder.length() - 1);
		if (lastChar != charSystemSeparator) {
			destin_folder += stringSystemSeparator;
		}

		boolean is_observed = false;
		for (String iter : input_data.keySet()) {
			if(!new File(iter).exists()) {
				System.out.println(iter + " does not exist. Skipping...");
				continue;
			}
			Path dir = Paths.get(iter);
			String game_folder_name = input_data.get(iter);
			String game_folder_destin = destin_folder + game_folder_name;
			Path game_folder_destin_path = Paths.get(game_folder_destin);
			if (!Files.exists(game_folder_destin_path)) {
				boolean success = (new File(game_folder_destin)).mkdirs();
				if (!success) {
					System.exit(1);
				}
			}
			synchroFolders(iter, game_folder_destin);
			SingleWatchThread watcher_in_thread = new SingleWatchThread(dir, game_folder_destin);
			Thread watek = new Thread(watcher_in_thread);
			watek.start();
			is_observed = true;

			// TODO
			// http://andreinc.net/2013/12/06/java-7-nio-2-tutorial-writing-a-simple-filefolder-monitor-using-the-watch-service-api/
		}
		if(!is_observed) {
			System.out.println("There is no folder to observe. Exiting...");
			System.exit(1);
		}
	}

	public static void synchroFolders(String source, String destin) {
		File folder = new File(source);
		File[] listOfFiles = folder.listFiles();
		char lastCharSource = source.charAt(source.length() - 1);
		
		if (lastCharSource != charSystemSeparator) {
			source += stringSystemSeparator;
		}
		char lastCharDestin = destin.charAt(destin.length() - 1);
		if (lastCharDestin != charSystemSeparator) {
			destin += stringSystemSeparator;
		}
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isDirectory()) {
				Path path = Paths.get(destin + listOfFiles[i].getName());
				if (Files.notExists(path)) {
					File srcDir = new File(source + listOfFiles[i].getName());
					File destDir = new File(destin + listOfFiles[i].getName());
					try {
						FileUtils.copyDirectory(srcDir, destDir);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					String subsource = source + listOfFiles[i].getName();
					String subdestin = destin + listOfFiles[i].getName();
					synchroFolders(subsource, subdestin);
				}
			} else {
				File ff = new File(destin + listOfFiles[i].getName());
				if(!((ff).exists())) {
					Path srcFile = Paths.get(source + listOfFiles[i].getName());
					Path destDir = Paths.get(destin + listOfFiles[i].getName());
					try {
						Files.copy(srcFile, destDir);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static void createFile(String fileName, String type, Scanner scan) {
		PrintWriter writer;
		try {
			String message = "";
			switch(type) {
				case "source" : message = "Please provide source folder path with destination folder name separated by semicolon e.g. C:\\Users\\Maciej\\Documents\\GTA Vice City User Files\\;GTA_VICE_CITY"; break;
				case "destination" : message = "Please provide destination folder path"; break;
			}			
			System.out.println(message);
			String path_to_provide = "";
			if(scan.hasNextLine()) {
				path_to_provide  = scan.nextLine();
			}						
			writer = new PrintWriter(fileName, "UTF-8");
			writer.write(path_to_provide);
			System.out.println("Path was saved."); 
			writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("Can't write or find a file: " + fileName + " Check and run program again");
			System.exit(1);
		} catch (UnsupportedEncodingException e) {
			System.out.println("Unsupported File Encoding. Exiting...");
			System.exit(1);
		}
	}

}
