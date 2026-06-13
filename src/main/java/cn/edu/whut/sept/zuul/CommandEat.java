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

        if (item.getDescription().contains("饼干")) {
            player.increaseMaxWeight(10.0); // 增加 10kg 负重
            player.dropItem(item); // 吃掉（从背包移除）
            // 不需要加回房间，因为它被吃掉了
            System.out.println("You ate the magic cookie.");
            System.out.println("You feel stronger! Your max carry weight increased.");
        } else {
            System.out.println("You can't eat that!");
        }
        return false;
    }
}

