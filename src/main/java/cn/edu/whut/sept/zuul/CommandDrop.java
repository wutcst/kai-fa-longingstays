package cn.edu.whut.sept.zuul;

public class CommandDrop implements CommandExecution {
    @Override
    public boolean execute(Game game, Command command) {
        if (!command.hasSecondWord()) {
            System.out.println("Drop what?");
            return false;
        }

        String itemName = command.getSecondWord();
        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();

        // 1. 在背包里找物品
        Item item = player.getItem(itemName);

        if (item == null) {
            System.out.println("You don't have item named \"" + itemName + "\".");
        } else {
            // 2. 丢弃
            player.dropItem(item);
            currentRoom.addItem(item); // 房间增加
            System.out.println("You dropped: " + item.getDescription());
        }
        return false;
    }
}

