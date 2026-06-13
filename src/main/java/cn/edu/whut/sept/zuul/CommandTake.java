package cn.edu.whut.sept.zuul;

public class CommandTake implements CommandExecution {
    @Override
    public boolean execute(Game game, Command command) {
        if (!command.hasSecondWord()) {
            System.out.println("Take what?");
            return false;
        }

        String itemName = command.getSecondWord();
        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();

        // 1. 在房间里找物品
        Item item = currentRoom.getItem(itemName);

        if (item == null) {
            System.out.println("There is no item named \"" + itemName + "\" here.");
        } else {
            // 2. 尝试捡起
            if (player.takeItem(item)) {
                currentRoom.removeItem(item); // 房间移除
                System.out.println("You picked up: " + item.getDescription());
            } else {
                System.out.println("The item is too heavy! You can't carry it.");
            }
        }
        return false;
    }
}

