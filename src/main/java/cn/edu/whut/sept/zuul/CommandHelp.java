package cn.edu.whut.sept.zuul;

public class CommandHelp implements CommandExecution {
    @Override
    public boolean execute(Game game, Command command) {
        System.out.println("You are lost. You are alone. You wander");
        System.out.println("around at the university.");
        System.out.println();
        System.out.println("Your command words are:");

        // 修复点：使用 game 对象中的 parser，而不是 new 一个新的
        game.getParser().showCommands();

        return false;
    }
}

