package cn.edu.whut.sept.zuul;

/**
 * 实现"back"命令的执行类.
 * 该命令用于将玩家带回上一个房间.
 */
public class CommandBack implements CommandExecution {
    @Override
    public boolean execute(Game game, Command command) {
        if (command.hasSecondWord()) {
            System.out.println("Back where?");
            return false;
        }

        // 调用 Game 中的逻辑
        game.goBack();

        return false;
    }
}

