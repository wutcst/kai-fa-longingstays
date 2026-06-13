package cn.edu.whut.sept.zuul;

public class CommandGo implements CommandExecution {
    @Override
    public boolean execute(Game game, Command command) {
        if(!command.hasSecondWord()) {
            // if there is no second word, we don't know where to go...
            System.out.println("Go where?");
            return false;
        }

        String direction = command.getSecondWord();

        // Try to leave current room.
        Room nextRoom = game.getCurrentRoom().getExit(direction);

        if (nextRoom == null) {
            System.out.println("There is no door!");
        }
        else {
            // 【关键修改】：在切换房间前，保存当前房间到历史记录
            game.pushHistory();

            game.setCurrentRoom(nextRoom);
            // 【修改】移动后，只打印简短描述（不含物品）
            System.out.println(game.getCurrentRoom().getShortDescription());
        }
        return false;
    }
}

