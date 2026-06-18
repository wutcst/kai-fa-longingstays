package cn.edu.whut.sept.zuul;

public class CommandEat implements CommandExecution {
    @Override
    public boolean execute(Game game, Command command) {
        if (!command.hasSecondWord()) {
            System.out.println("Eat what?");
            return false;
        }

        String itemName = command.getSecondWord();
        Player player = game.getPlayer();

        // 只能吃背包里的东西（或者你可以设计成可以吃房间里的，这里假设先捡起来才能吃）
        Item item = player.getItem(itemName);

        if (item == null) {
            System.out.println("You don't have that!");
            return false;
        }

        String desc = item.getDescription();
        if (desc.contains("饼干")) {
            player.increaseMaxWeight(10.0);
            player.dropItem(item);
            System.out.println("You ate the magic cookie.");
            System.out.println("You feel stronger! Your max carry weight increased.");
        } else if (desc.contains("WarmveinAle") || desc.contains("麦脉暖酿")) {
            player.dropItem(item);
            System.out.println("You drank the WarmveinAle. HP +20.");
        } else if (desc.contains("Moonhoney") || desc.contains("月花蜜醴")) {
            player.dropItem(item);
            System.out.println("You drank the Moonhoney. MP +20.");
        } else {
            System.out.println("You can't eat that!");
        }
        return false;
    }
}

