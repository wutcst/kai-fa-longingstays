package cn.edu.whut.sept.zuul;

public class CommandBuy implements CommandExecution {
    @Override
    public boolean execute(Game game, Command command) {
        if (!command.hasSecondWord()) {
            System.out.println("购买什么？");
            return false;
        }

        String itemName = command.getSecondWord();
        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();

        // 必须在酒吧才能购买
        if (!currentRoom.getRawDescription().contains("pub") && !currentRoom.getRawDescription().contains("酒吧")) {
            System.out.println("你只能在酒吧购买饮料！");
            return false;
        }

        int price = 0;
        Item newItem = null;
        if (itemName.contains("WarmveinAle") || itemName.contains("麦脉暖酿") || itemName.contains("麦脉")) {
            newItem = new Item("麦脉暖酿", 0.5, 50, 50);
            price = 20;
        } else if (itemName.contains("Moonhoney") || itemName.contains("月花蜜醴") || itemName.contains("月花")) {
            newItem = new Item("月花蜜醴", 0.4, 50, 50);
            price = 30;
        } else {
            System.out.println("没有这种饮料可供购买！");
            return false;
        }

        // 【修复】检查金币是否足够
        if (player.getCoins() < price) {
            System.out.println("你的金币不够！需要 " + price + " 🪙，你只有 " + player.getCoins() + " 🪙。");
            return false;
        }

        // 检查负重
        if (player.getCurrentWeight() + newItem.getWeight() > player.getMaxWeight()) {
            System.out.println("你不能再带了！你的背包太重了。");
            return false;
        }

        // 扣金币 + 加物品
        player.spendCoins(price);
        player.getInventory().add(newItem);
        System.out.println("你购买了: " + newItem.getDescription() + " (" + newItem.getWeight() + " kg)");
        return false;
    }
}