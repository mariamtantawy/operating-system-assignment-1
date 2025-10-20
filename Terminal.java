import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.ZipOutputStream;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


class Parser {
    private String commandName;
    private String[] args;

    public boolean parse(String input) {
        if (input == null || input.trim().isEmpty())
            return false;

        Scanner scanner = new Scanner(input);
        commandName = scanner.next();

        List<String> argsList = new ArrayList<>();
        while (scanner.hasNext()) {
            argsList.add(scanner.next());
        }
        args = argsList.toArray(new String[0]);
        scanner.close();
        return true;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}

public class Terminal {
    private Parser parser;
    private File currentDirectory = new File(System.getProperty("user.dir"));

    private void copyFile (File src, File dest) throws IOException {
        Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void copyDir (File srcDir, File destDir) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        for (File file : Objects.requireNonNull(srcDir.listFiles())) {
            File newFile = new File(destDir, file.getName());
            if (file.isDirectory()) {
                copyDir(file, newFile);
            } else {
                copyFile(file, newFile);
            }
        }
    }

    private void zipFile(File file, String entryPath, ZipOutputStream stream) throws IOException {
        try (FileInputStream input = new FileInputStream(file)) {
            ZipEntry entry = new ZipEntry(entryPath);
            stream.putNextEntry(entry);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                stream.write(buffer, 0, bytesRead);
            }
            stream.closeEntry();
        }
    }

private void zipFolder(File folder, String basePath, ZipOutputStream stream) throws IOException {
    File[] files = folder.listFiles();
    if (files == null) {
        return;
    }

    for (File file : files) {
        String entryPath = basePath + "/" + file.getName();
        if (file.isDirectory()) {
            zipFolder(file, entryPath, stream);
        } else {
            zipFile(file, entryPath, stream);
        }
    }
}


    public Terminal() {
        parser = new Parser();
    }

    public String pwd() {
        return currentDirectory.getAbsolutePath();
    }

    public String cd(String[] args) {
        String path = (args.length > 0) ? args[0] : "";
        if (path.isEmpty()) {
            currentDirectory = new File(System.getProperty("user.home"));
            return "Changed to home directory";
        } else if (path.equals("..")) {
            File parent = currentDirectory.getParentFile();
            if (parent != null) {
                currentDirectory = parent;
                return "Moved to parent directory";
            } else {
                return "Already at root directory";
            }
        } else {
            File newDir = new File(path);
            if (!newDir.isAbsolute()) {
                newDir = new File(currentDirectory, path);
            }

            if (newDir.exists() && newDir.isDirectory()) {
                currentDirectory = newDir;
                return "Changed directory to: " + currentDirectory.getAbsolutePath();
            } else {
                return "cd: invalid path";
            }
        }
    }

    public String ls(String[] args) {
        File[] files = currentDirectory.listFiles();
        if (files == null) {
            return "ls: cannot access directory";
        }
        Arrays.sort(files);
        StringBuilder result = new StringBuilder();
        for (File file : files) {
            result.append(file.getName()).append("\n");
        }
        return result.toString().trim();
    }

    public String mkdir(String[] args) {
        if (args.length == 0)
            return "mkdir: missing argument";

        StringBuilder result = new StringBuilder();
        for (String path : args) {
            File dir = new File(path);
            if (!dir.isAbsolute())
                dir = new File(currentDirectory, path);
            if (dir.exists()) {
                result.append("Directory already exists: ").append(dir.getName()).append("\n");
            } else if (dir.mkdirs()) {
                result.append("Directory created: ").append(dir.getAbsolutePath()).append("\n");
            } else {
                result.append("mkdir: failed to create ").append(dir.getAbsolutePath()).append("\n");
            }
        }
        return result.toString().trim();
    }

    public String rmdir(String[] args) {
        if (args.length == 0) {
            return "rmdir: missing argument";
        }

        String path = args[0];
        StringBuilder result = new StringBuilder();

        if (path.equals("*")) {
            File[] dirs = currentDirectory.listFiles(File::isDirectory);
            if (dirs != null) {
                for (File d : dirs) {
                    if (d.list().length == 0) {
                        d.delete();
                        result.append("Deleted empty directory: ").append(d.getName()).append("\n");
                    }
                }
                if (result.length() == 0) {
                    result.append("No empty directories found to delete.");
                }
            } else {
                result.append("Error: could not access current directory.");
            }
        } else {
            File dir = new File(path);
            if (!dir.isAbsolute())
                dir = new File(currentDirectory, path);

            if (dir.exists() && dir.isDirectory()) {
                if (dir.list().length == 0) {
                    dir.delete();
                    result.append("Deleted directory: ").append(dir.getAbsolutePath());
                } else {
                    result.append("Error: directory not empty.");
                }
            } else {
                result.append("Error: directory not found.");
            }
        }

        return result.toString().trim();
    }

    public String touch(String[] args) {
        if (args.length == 0) {
            return "You must specify directory";
        }
        File file = new File(args[0]);
        if (!file.isAbsolute()) {
            file = new File(currentDirectory, args[0]); 
        }
        try {
            if (file.createNewFile()) {
                return "File " + args[0] + " created successfully";

            } else {
                return "File already exists";
            }

        } catch (IOException e) {
            return "Error creating file: " + e.getMessage();
        }
    }

    public String cp(String[] args) {
        if (args.length < 2) {
            return "Invalid command. Usage: cp [-r] source target";
        }
        boolean recursive;
        int index;
        if (args[0].equals("-r")) {
            recursive = true;
            index = 1;
        } else {
            recursive = false;
            index = 0;
        }

        File source = new File (args[index]);
        File destination = new File (args[index+1]);

        if (!source.isAbsolute()) {
            source = new File (currentDirectory, args[index]);
        }
        
        if (!destination.isAbsolute()) {
            destination = new File (currentDirectory, args[index+1]);
        }
        if (!source.exists()) {
            return "Error: source not found";
        }

        try {
            if (source.isDirectory()) {
                if (!recursive) {
                    return "Use -r for directories";
                }
                copyDir(source, destination);
            }
            else {
                copyFile(source, destination);
            }
        }
        catch (IOException e) {
            return "Copy failed: " + e.getMessage();
        }
        return "Copied " + source.getName() + " to " + destination.getPath(); 
    }

    public String rm(String[] args) {
        if (args.length == 0) {
            return "You must specifiy directory";
        }
        File file = new File(args[0]);
        try {
            if (file.exists()) {
                file.delete();
                return "File " + "'" + args[0] + "'" + "deleted successfully";
            } else {
                return "There isn't this file";
            }
        } catch (Exception e) {
            return "Error Delete file: " + e.getMessage();
        }
    }

    public String cat(String[] args) {
        if (args.length == 0) {
            return "you must specify Directory";
        }
        String line = null;

        for (int i = 0; i < args.length; i++) {

            try (BufferedReader reader = new BufferedReader(new FileReader(args[i]))) {
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

            } catch (IOException e) {
                return "Error: " + e.getMessage();
            }
        }

        return line;

    }

    public String wc(String[] args) {
        int lines = 0;
        int wordsCount = 0;
        int characters = 0;
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(args[0]))) {
            while ((line = reader.readLine()) != null) {
                lines++;
                String words[] = line.split(" ");
                wordsCount += words.length;
                for (String word : words) {
                    characters += word.length();
                }

            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

        return Integer.toString(lines) + " " + Integer.toString(wordsCount) + " " + Integer.toString(characters) + " "
                + args[0];
    }

    public String zip(String[] args) {
        if (args.length < 2) {
            return "Invalid command. Usage: zip [-r] <output.zip> <file1> [file2 ...]";
        }
        boolean recursive = false;
        String fileName;
        int index;

        if (args[0].equals("-r")) {
            recursive = true;
            if (args.length < 3) {
                return "Error: missing file or directory after -r option.";
            }
            fileName = args[1];
            index = 2;
        } else {
            fileName = args[0];
            index = 1;
        }

        File zippedFile = new File(fileName);
        if (!zippedFile.isAbsolute()) {
            zippedFile = new File(currentDirectory, fileName);
        }

        try (ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(zippedFile))) {

        for (int i = index; i < args.length; i++) {
            File source = new File(args[i]);
            if (!source.isAbsolute()) {
                source = new File(currentDirectory, args[i]);
            }

            if (!source.exists()) {
                return "Error: file or directory not found - " + args[i];
            }

            if (source.isDirectory()) {
                if (!recursive) {
                    return "Error: " + args[i] + " is a directory. Use -r for recursive zipping.";
                }
                zipFolder(source, source.getName(), stream);
            } else {
                zipFile(source, source.getName(), stream);
            }
        }

         return "Archive successfully created at: " + zippedFile.getAbsolutePath();

        } catch (IOException e) {
            return "Failed to create zip file: " + e.getMessage();
        }
    }


    public String unzip(String[] args) {
        if (args.length == 0) {
            return "Invalid command. Usage: unzip <archive.zip> [-d <destinationFolder>]";
        }

        File archive = new File(args[0]);
        if (!archive.isAbsolute()) {
            archive = new File(currentDirectory, args[0]);
        }

        File destDir = currentDirectory;

        if (args.length >= 2 && args[1].equals("-d")) {
            if (args.length < 3) {
                return "Error: destination folder missing after -d.";
            }
            destDir = new File(args[2]);
            if (!destDir.isAbsolute()) {
                destDir = new File(currentDirectory, args[2]);
            }
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
        }

        try (ZipInputStream zipInput = new ZipInputStream(new FileInputStream(archive))) {
            ZipEntry entry;
            byte[] buffer = new byte[4096];

            while ((entry = zipInput.getNextEntry()) != null) {
                File extractedFile = new File(destDir, entry.getName());

                if (entry.isDirectory()) {
                    extractedFile.mkdirs();
                } else {
                    extractedFile.getParentFile().mkdirs();
                    try (FileOutputStream output = new FileOutputStream(extractedFile)) {
                        int count;
                        while ((count = zipInput.read(buffer)) != -1) {
                            output.write(buffer, 0, count);
                        }
                    }
                }
                zipInput.closeEntry();
            }

            return "Archive extracted to: " + destDir.getAbsolutePath();

        } catch (IOException e) {
            return "Error while extracting archive: " + e.getMessage();
        }
    }

    public String chooseCommandAction(String commandName, String[] args) {
        switch (commandName) {
            case "pwd":
                return pwd();
            case "cd":
                return cd(args);
            case "ls":
                return ls(args);
            case "mkdir":
                return mkdir(args);
            case "rmdir":
                return rmdir(args);
            case "touch":
                return touch(args);
            case "cp":
                return cp(args);
            case "rm":
                return rm(args);
            case "cat":
                return cat(args);
            case "wc":
                return wc(args);
            case "zip":
                return zip(args);
            case "unzip":
                return unzip(args);
            case "exit":
                return null;
            default:
                return "Unknown command: " + commandName;
        }
    }

    public void run() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String input = sc.nextLine();

            if (!parser.parse(input)) {
                continue;
            }

            String command = parser.getCommandName();
            String[] args = parser.getArgs();

            if (command.equals("exit")) {
                break;
            }

            String output = chooseCommandAction(command, args);
            if (output != null && !output.isEmpty()) {
                System.out.println(output);
            }
        }
        sc.close();
    }

    public static void main(String[] args) {
        Terminal t = new Terminal();
        System.out.println("Type 'exit' to quit.");
        t.run();
        System.out.println("Program exited.");
    }
}
