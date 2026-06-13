package cn.edu.whut.sept.zuul;

public class CommandItems implements CommandExecution {
    @Override
    public boolean execute(Game game, Command command) {
        Player player = game.getPlayer();
        Room room = player.getCurrentRoom();

        // 打印房间物品及总重
        System.out.println("--- Room Items ---");
        // 这里复用 Room.getLongDescription 里的逻辑，或者只打印物品
        // 为了符合题目“打印当前房间内所有物件及总重量”：
        System.out.println(room.getLongDescription());
        System.out.println("Total weight in room: " + room.getTotalItemWeight() + " kg");

        System.out.println();

        // 打印玩家物品及总重
        System.out.println("--- Player Inventory ---");
        System.out.println(player.getInventoryString());

        return false;
    }
}

