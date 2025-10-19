import java.util.*;

class Parser {
    private String commandName;
    private String[] args;
    
    public boolean parse(String input) {
        if (input == null || input.trim().isEmpty()) return false;

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


    public Terminal() {
        parser = new Parser();
    }

    public String pwd() {
        return "pwd";
    }

    public String cd(String[] args) {
        return "cd";
    }

    public String ls(String[] args) {
        return "ls";
    }

    public String mkdir(String[] args) {
        return "mkdir";
    }

    public String rmdir(String[] args) {
        return "rmdir";
    }

    public String touch(String[] args) {
        return "touch";
    }
    public String cp(String[] args) {
        return "cp";
    }

    public String rm(String[] args) {
        return "rm";
    }

    public String cat(String[] args) {
        return "cat";
    }

    public String wc(String[] args) {
        return "wc";
    }

    public String zip(String[] args) {
        return "zip";
    }

    public String unzip(String[] args) {
        return "unzip";
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

