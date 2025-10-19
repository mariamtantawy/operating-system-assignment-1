import java.io.File;
import java.util.Arrays;

public class Terminal {
    private File currentDirectory = new File(System.getProperty("user.dir"));
    private Parser parser = new Parser();

    // Implement all commands here
    public String pwd() {
        return currentDirectory.getAbsolutePath();
    }
    public void chooseCommandAction(String input) {
        if (!parser.parse(input)) return;

        String cmd = parser.getCommandName();
        String[] args = parser.getArgs();

        switch (cmd) {
            case "pwd":pwd();break;
            
        }
    }

    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        java.util.Scanner sc = new java.util.Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String input = sc.nextLine();
            terminal.chooseCommandAction(input);
        }
    }
}