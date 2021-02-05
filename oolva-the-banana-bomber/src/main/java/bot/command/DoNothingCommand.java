package bot.command;

public class DoNothingCommand implements Command {
    @Override
    public String render() {
        return "nothing";
    }
}
