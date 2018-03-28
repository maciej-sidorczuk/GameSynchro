This program runs in windows and linux GUI. It observes save game folders (previously defined in source_paths.txt) and waits for any changes. When some file is created or modified (when you save the game) it is copied to destination folder for example to google drive folder (previously defined in destin_path.txt).
Thanks to this program every new save game will be backup.

Instructions:
Before you run program you need to provide folder paths in source_path.txt and destin_path.txt

In source_path.txt you put in each line:
path_to_save_folder;name_of_folder_which_will_be_created_in_destination_folder e.g.
C:\Users\Maciej\Documents\GTA Vice City User Files\;GTA_VICE_CITY
C:\Users\Maciej\Documents\Rayman Legends\;RAYMAN_LEGENDS
each save game folder with destination name you have to put in new line

In destin_path.txt you put only one line - a path to folder where all your save games will be copied e.g.
F:\users\Maciej\Dysk Google\Synchronizowane\Gry\save

You can compile program to jar file and then create bat file with below content:
start javaw -jar gamesynchro.jar
then run command: shell:startup
and insert bat file to startup folder
