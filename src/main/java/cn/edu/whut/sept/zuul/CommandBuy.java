package cn.edu.whut.sept.zuul;

public class CommandBuy implements CommandExecution {
    @Override
    public boolean execute(Game game, Command command) {
        if (!command.hasSecondWord()) {
            System.out.println("Buy what?");
            return false;
        }

        String itemName = command.getSecondWord();
        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();

        // 必须在酒吧才能购买
        if (!currentRoom.getRawDescription().contains("pub") && !currentRoom.getRawDescription().contains("酒吧")) {
            System.out.println("You can only buy drinks in the pub!");
            return false;
        }

        Item newItem = null;
        if (itemName.contains("WarmveinAle") || itemName.contains("麦脉暖酿")) {
            newItem = new Item("WarmveinAle", 0.5, 50, 50);
        } else if (itemName.contains("Moonhoney") || itemName.contains("月花蜜醴")) {
            newItem = new Item("Moonhoney-Sippen-Sippen", 0.4, 50, 50);
        } else {
            System.out.println("The barmaid doesn't sell that!");
            return false;
        }

        // 检查负重
        if (player.getCurrentWeight() + newItem.getWeight() > player.getMaxWeight()) {
            System.out.println("You can't carry any more! Your inventory is too heavy.");
            return false;
        }

        player.getInventory().add(newItem);
        System.out.println("You bought: " + newItem.getDescription() + " (" + newItem.getWeight() + " kg)");
        return false;
    }
}