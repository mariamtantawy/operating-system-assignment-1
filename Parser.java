public class Parser {
    String commandName;
    String[] args;

    public boolean parse(String input) {
        input = input.trim();
        if (input.isEmpty()) return false;

        String[] parts = input.split("\\s+");
        commandName = parts[0];
        args = new String[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            args[i - 1] = parts[i];
        }

        return true;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}