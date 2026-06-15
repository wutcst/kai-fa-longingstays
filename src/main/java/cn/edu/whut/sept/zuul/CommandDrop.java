package cn.edu.whut.sept.zuul;

public class CommandDrop implements CommandExecution {
    @Override
    public boolean execute(Game game, Command command) {
        if (!command.hasSecondWord()) {
            System.out.println("Drop what?");
            return false;
        }

        String secondWord = command.getSecondWord();
        String[] parts = secondWord.split(" ");
        String itemName;
        double dropX = -1, dropY = -1;

        if (parts.length >= 3) {
            // 最后两个是坐标
            itemName = String.join(" ", java.util.Arrays.copyOf(parts, parts.length - 2));
            try {
                dropX = Double.parseDouble(parts[parts.length - 2]);
                dropY = Double.parseDouble(parts[parts.length - 1]);
            } catch (NumberFormatException e) {
                dropX = -1; dropY = -1;
            }
        } else {
            itemName = secondWord;
        }

        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();

        Item item = player.getItem(itemName);

        if (item == null) {
            System.out.println("You don't have item named \"" + itemName + "\".");
        } else {
            if (dropX >= 0 && dropY >= 0) {
                item.setX(dropX);
                item.setY(dropY);
            }
            player.dropItem(item);
            currentRoom.addItem(item);
            System.out.println("You dropped: " + item.getDescription());
        }
        return false;
    }
}

