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
            System.out.println("你吃了这块魔法饼干.");
            System.out.println("你感觉更强壮了! ");
        } else if (desc.contains("麦脉暖酿") || desc.contains("麦脉")) {
            player.dropItem(item);
            System.out.println("你喝完了这杯 麦脉暖酿 感到很温暖. HP +20.");
        } else if (desc.contains("月花蜜醴") || desc.contains("月花")) {
            player.dropItem(item);
            System.out.println("你一口喝完了一整杯 月花蜜醴 感到很清晰. MP +20.");
        } else {
            System.out.println("别吃这个!");
        }
        return false;
    }
}

