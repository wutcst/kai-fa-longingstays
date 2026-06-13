package cn.edu.whut.sept.zuul;

/**
 * 实现"look"命令的执行类.
 */
public class CommandLook implements CommandExecution {
    @Override
    public boolean execute(Game game, Command command) {
        // 1. 输出房间信息（包含位置、出口、房间内物品）
        System.out.println(game.getCurrentRoom().getLongDescription());

        // 2. 输出玩家背包信息 (新增)
        // 注意：这里没有加 "--- Player Inventory ---" 这种分割线，符合你的要求“直接输出”
        System.out.println(game.getPlayer().getInventoryString());

        return false;
    }
}

