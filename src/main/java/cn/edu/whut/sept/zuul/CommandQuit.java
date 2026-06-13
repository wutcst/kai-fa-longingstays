package cn.edu.whut.sept.zuul;

public class CommandQuit implements CommandExecution {
    @Override
    public boolean execute(Game game, Command command) {
        if(command.hasSecondWord()) {
            System.out.println("Quit what?");
            return false;
        }
        else {
            return true;  // 标记着玩家想要退出
        }
    }
}

