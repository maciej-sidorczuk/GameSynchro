package pl.com.sidorczuk.developers;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import static java.nio.file.StandardCopyOption.*;

import java.nio.file.WatchEvent.Kind;

public class SingleWatchThread implements Runnable {
	
	Path dir;
	String gameDestinPath;
	WatchService watcher;
	public SingleWatchThread(Path dir, String gameDestinPath) {
		this.dir = dir;
		this.gameDestinPath = gameDestinPath;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try {
			watcher = FileSystems.getDefault().newWatchService();
			try {
				//dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
				this.registerRecursive(dir);
				WatchKey key = null;
				while(true) {
					key = watcher.take();
					Kind<?> kind = null;
					for(WatchEvent<?> watchEvent : key.pollEvents()) {
						// Get the type of the event
						kind = watchEvent.kind();
						if (OVERFLOW == kind) {
							continue; //loop
						} else if (ENTRY_CREATE == kind) {
							// A new Path was created 
							System.out.println("created");
							Path newdir = (Path) key.watchable();
							Path newPath = ((WatchEvent<Path>) watchEvent).context();
							Path fullPath = newdir.resolve(newPath);
							String fullPathString = fullPath.toString();
							String abolutePathString = dir.toString();
							String relativePath = fullPathString.replace(abolutePathString, "");
							String FileDestinPath = gameDestinPath + relativePath;
							Path FileDestinPath_p = Paths.get(FileDestinPath);
							Path FileSourcePath_p = Paths.get(fullPathString);
							String fileDestinName = FileDestinPath_p.getFileName().toString();
							DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
							Date date = new Date();
							String currentTime = dateFormat.format(date);
							File file = FileSourcePath_p.toFile();
							if(file.isDirectory()) {
								File[] files = file.listFiles();
								for (int i = 0; i < files.length; i++) {
									System.out.println(files[i].getPath());
								}
								FileUtils.copyDirectory(FileSourcePath_p.toFile(), FileDestinPath_p.toFile());
								this.registerRecursive(dir);
							} else {
								try {
									Files.copy(FileSourcePath_p, FileDestinPath_p);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}							
							}
							// Output
						} else if (ENTRY_MODIFY == kind) {
							System.out.println("modified");
							Path newdir = (Path) key.watchable();
							Path newPath = ((WatchEvent<Path>) watchEvent).context();
							Path fullPath = newdir.resolve(newPath);
							if(!(fullPath.toFile().isDirectory())) {
								String fullPathString = fullPath.toString();
								String abolutePathString = dir.toString();
								String relativePath = fullPathString.replace(abolutePathString, "");
								String FileDestinPath = gameDestinPath + relativePath;
								Path FileDestinPath_p = Paths.get(FileDestinPath);
								Path FileSourcePath_p = Paths.get(fullPathString);
								System.out.println(fullPathString);
								String fileDestinName = FileDestinPath_p.getFileName().toString();
								DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
								Date date = new Date();
								String currentTime = dateFormat.format(date);
								if(Files.exists(FileSourcePath_p)) {
									if (Files.notExists(FileDestinPath_p)) {
										try {
											Files.copy(FileSourcePath_p, FileDestinPath_p);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									} else {
										Files.move(FileDestinPath_p, FileDestinPath_p.resolveSibling("old_" + currentTime + "_" + fileDestinName),REPLACE_EXISTING);
										try {
											Files.copy(FileSourcePath_p, FileDestinPath_p);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
								
								//
							}
							
						} else if(ENTRY_DELETE == kind) {
							//System.out.println("delete");
							//TODO
							//need to find a way to stop watching content of deleted folder
							//how to solve situation when you move or rename folder
							//this.registerRecursive(dir);
							
						}
					}
					if(!key.reset()) {
						break; //loop
					}
				}
				
			} catch (IOException x) {
				System.err.println(x);
			} catch(InterruptedException ie) {
				ie.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private void registerRecursive(final Path root) throws IOException {
	    // register all subfolders
	    Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
	            dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
	            return FileVisitResult.CONTINUE;
	        }
	    });
	}

}
