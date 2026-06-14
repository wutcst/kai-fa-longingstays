/**
 * 该类是"World-of-Zuul"应用程序的主类。
 * 《World of Zuul》是一款简单的文本冒险游戏。用户可以在一些房间组成的迷宫中探险。
 * 你们可以通过扩展该游戏的功能使它更有趣!.
 * 如果想开始执行这个游戏，用户需要创建Game类的一个实例并调用"play"方法。
 * Game类的实例将创建并初始化所有其他类:它创建所有房间，并将它们连接成迷宫；它创建解析器
 * 接收用户输入，并将用户输入转换成命令后开始运行游戏。
 *
 * @author  Michael Kölling and David J. Barnes
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

import java.util.Stack; // 导入 Stack
import java.util.ArrayList; // 导入ArrayList
import java.util.List;      // 导入List
import java.util.Random;    // 导入Random

import cn.edu.whut.sept.zuul.db.GameStateDTO;
import cn.edu.whut.sept.zuul.db.GameStateDTO.ItemDTO;
import cn.edu.whut.sept.zuul.db.GameStateDTO.RoomDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Game
{
    private final Parser parser;
    // private Room currentRoom; // 【删除】不再由Game直接持有
    private Player player;       // 【新增】引入玩家对象
    private final Stack<Room> history; // 新增历史记录栈
    private List<Room> allRooms; // 新增：存储所有房间引用
    private Random random;       // 新增：随机数生成器

    /** 房间 ID 映射，用于序列化/反序列化 */
    private final java.util.Map<String, Room> roomIdMap;
    /** Gson 实例 */
    private final Gson gson;

    /** 游戏是否正在运行（用于退出到主菜单） */
    private boolean running;

    /**
     * 创建游戏并初始化内部数据和解析器.
     */
    public Game()
    {
        // 先初始化列表和随机生成器
        allRooms = new ArrayList<>();
        random = new Random();
        roomIdMap = new java.util.LinkedHashMap<>();
        gson = new GsonBuilder().setPrettyPrinting().create();
        running = true;

        createRooms();// 在这里面会初始化 player
        parser = new Parser();// Parser 内部会创建空的 CommandWords
        initializeCommandMap(); // 向 Parser 的注册表中填充命令
        history = new Stack<>(); // 初始化栈
    }

    /**
     * 初始化所有支持的命令。
     * 直接向 CommandWords 注册，实现"单一数据源"。
     */
    private void initializeCommandMap() {
        CommandWords commands = parser.getCommandWords();
        commands.register("go", new CommandGo());
        commands.register("help", new CommandHelp());
        commands.register("quit", new CommandQuit());
        commands.register("look", new CommandLook()); // 新增注册 look 命令
        commands.register("back", new CommandBack()); // 新增注册 back 命令
        // 【新增】注册新命令
        commands.register("take", new CommandTake());
        commands.register("drop", new CommandDrop());
        commands.register("items", new CommandItems());
        commands.register("eat", new CommandEat());
    }

    /**
     * 创建所有房间对象并连接其出口用以构建迷宫.
     */
    private void createRooms()
    {
        Room outside, theater, pub, lab, office, transporter;

        // create the rooms
        outside = new Room("outside the main entrance of the university");
        theater = new Room("in a lecture theater");
        pub = new Room("in the campus pub");
        lab = new Room("in a computing lab");
        office = new Room("in the computing admin office");
        transporter = new TransporterRoom("in a mysterious room with glowing symbols"); // 创建传送房间

        // 注册房间 ID
        roomIdMap.put("outside", outside);
        roomIdMap.put("theater", theater);
        roomIdMap.put("pub", pub);
        roomIdMap.put("lab", lab);
        roomIdMap.put("office", office);
        roomIdMap.put("transporter", transporter);

        // 将所有普通房间加入列表（用于随机传送的目标）
        allRooms.add(outside);
        allRooms.add(theater);
        allRooms.add(pub);
        allRooms.add(lab);
        allRooms.add(office);
        // 注意：通常我们不希望随机传送到传送房间自己，防止死循环或连续传送，所以不把 transporter 加入 allRooms

        // initialise room exits
        outside.setExit("east", theater);
        outside.setExit("south", lab);
        outside.setExit("west", pub);

        theater.setExit("west", outside);

        pub.setExit("east", outside);
        pub.setExit("west", transporter); // 【连接】在 Pub 的西边放置传送房间

        lab.setExit("north", outside);
        lab.setExit("east", office);

        office.setExit("west", lab);

        // 传送房间也可以有出口（虽然进入就会被传送走，但作为 Room 对象它需要出口以防万一逻辑绕过）
        transporter.setExit("east", pub);

        // 初始化物品

        // outside (外部)
        outside.addItem(new Item("生锈的铁锹：一把木柄生锈的铁锹，看起来被遗弃了很久", 3.2));
        outside.addItem(new Item("破旧的地图：一张泛黄的羊皮纸地图，边缘有烧焦的痕迹，大部分字迹已模糊", 0.1));
        outside.addItem(new Item("空鸟巢：一个由树枝和干草筑成的鸟巢，里面有几片破碎的蛋壳", 0.4));
        outside.addItem(new Item("鹅卵石堆：一堆光滑的鹅卵石，其中一块上面刻着奇怪的符号", 5.0));

        // office (办公室)
        office.addItem(new Item("笔记本电脑：一台黑色的ThinkPad笔记本电脑，屏幕有细微划痕，电池续航约3小时", 2.5));
        office.addItem(new Item("咖啡杯：印有公司logo的白色陶瓷杯，杯底有褐色咖啡渍", 0.3));
        office.addItem(new Item("文件堆：一沓未处理的财务报表和项目计划书，最上面的文件标着\u201c紧急\u201d", 1.2));
        office.addItem(new Item("转椅：黑色皮质办公椅，右侧扶手有破损", 8.5));

        // lab (实验室)
        lab.addItem(new Item("显微镜：一台老式光学显微镜，目镜有些模糊，载物台上有玻璃切片", 4.7));
        lab.addItem(new Item("烧杯组：三个不同尺寸的玻璃烧杯，最小的那个有裂痕", 1.8));
        lab.addItem(new Item("化学试剂瓶：棕色玻璃瓶，标签写着\u201cH\u2082SO\u2084 \u6d53\u786b\u9178 \u5371\u9669\u201d，瓶盖紧闭", 0.9));
        lab.addItem(new Item("实验记录本：皮质封面的笔记本，记录着奇怪的化学公式和潦草的笔记", 0.6));

        // pub (酒吧)
        pub.addItem(new Item("木制吧台：长长的桃花心木吧台，表面有多年使用留下的划痕和酒杯印", 45.0));
        pub.addItem(new Item("威士忌酒瓶：半空的杰克丹尼威士忌，瓶身标签已磨损", 1.1));
        pub.addItem(new Item("飞镖盘：挂在墙上的标准飞镖盘，中心区域有几个深深的孔洞", 3.2));
        pub.addItem(new Item("酒吧凳：高脚旋转凳，红色皮革坐垫有开裂", 6.3));

        // theater (剧院)
        theater.addItem(new Item("红色帷幕：厚重的天鹅绒帷幕，边缘有金线刺绣，有些地方已经褪色", 22.0));
        theater.addItem(new Item("舞台聚光灯：老式的金属聚光灯，镜片有裂纹，但仍能工作", 12.5));
        theater.addItem(new Item("道具剑：舞台用的仿古长剑，剑身是钝的铝合金，剑柄有假宝石", 1.8));
        theater.addItem(new Item("乐谱架：木制乐谱架，上面还放着一份泛黄的乐谱", 2.4));

        // 【新增】添加魔法饼干到某个房间（比如 Lab）
        lab.addItem(new Item("魔法饼干", 0.5));

        // 【新增】初始化玩家
        // 初始房间设为 outside，最大负重设为 10.0kg
        player = new Player("范奕轩", outside, 10.0);
    }

    // ===== 存档/读档/新游戏 =====

    /**
     * 将当前游戏状态序列化为 JSON 字符串.
     * @return JSON 字符串.
     */
    public String serializeGameState() {
        GameStateDTO dto = new GameStateDTO();
        dto.setPlayerName(player.getName());
        dto.setCurrentRoomId(findRoomId(player.getCurrentRoom()));
        dto.setMaxWeight(player.getMaxWeight());

        // 序列化背包物品
        for (Item item : player.getInventory()) {
            dto.getInventory().add(new ItemDTO(item.getDescription(), item.getWeight()));
        }

        // 序列化所有房间
        for (java.util.Map.Entry<String, Room> entry : roomIdMap.entrySet()) {
            String roomId = entry.getKey();
            Room room = entry.getValue();
            RoomDTO roomDTO = new RoomDTO();
            roomDTO.setId(roomId);
            roomDTO.setDescription(room.getRawDescription());
            roomDTO.setTransporter(room instanceof TransporterRoom);

            // 序列化出口
            for (String dir : new String[]{"north", "south", "east", "west"}) {
                Room exit = room.getExit(dir);
                if (exit != null) {
                    String exitId = findRoomId(exit);
                    if (exitId != null) {
                        roomDTO.getExits().put(dir, exitId);
                    }
                }
            }

            // 序列化房间物品
            for (Item item : room.getItems()) {
                roomDTO.getItems().add(new ItemDTO(item.getDescription(), item.getWeight()));
            }

            dto.getRooms().put(roomId, roomDTO);
        }

        // 序列化历史记录栈
        dto.setHistoryRoomIds(new ArrayList<>());
        for (Room room : history) {
            String id = findRoomId(room);
            if (id != null) {
                dto.getHistoryRoomIds().add(id);
            }
        }

        return gson.toJson(dto);
    }

    /**
     * 从 JSON 字符串恢复游戏状态.
     * @param json JSON 字符串.
     */
    public void deserializeGameState(String json) {
        GameStateDTO dto = gson.fromJson(json, GameStateDTO.class);
        if (dto == null) return;

        // 1. 重建所有房间
        roomIdMap.clear();
        allRooms.clear();
        for (java.util.Map.Entry<String, RoomDTO> entry : dto.getRooms().entrySet()) {
            String roomId = entry.getKey();
            RoomDTO roomDTO = entry.getValue();
            Room room;
            if (roomDTO.isTransporter()) {
                room = new TransporterRoom(roomDTO.getDescription());
            } else {
                room = new Room(roomDTO.getDescription());
            }
            roomIdMap.put(roomId, room);
            if (!roomId.equals("transporter")) {
                allRooms.add(room);
            }
        }

        // 2. 重建出口连接
        for (java.util.Map.Entry<String, RoomDTO> entry : dto.getRooms().entrySet()) {
            String roomId = entry.getKey();
            RoomDTO roomDTO = entry.getValue();
            Room room = roomIdMap.get(roomId);
            if (room == null) continue;
            for (java.util.Map.Entry<String, String> exitEntry : roomDTO.getExits().entrySet()) {
                String dir = exitEntry.getKey();
                String neighborId = exitEntry.getValue();
                Room neighbor = roomIdMap.get(neighborId);
                if (neighbor != null) {
                    room.setExit(dir, neighbor);
                }
            }
        }

        // 3. 重建房间物品
        for (java.util.Map.Entry<String, RoomDTO> entry : dto.getRooms().entrySet()) {
            String roomId = entry.getKey();
            RoomDTO roomDTO = entry.getValue();
            Room room = roomIdMap.get(roomId);
            if (room == null) continue;
            room.getItems().clear();
            for (ItemDTO itemDTO : roomDTO.getItems()) {
                room.getItems().add(new Item(itemDTO.getDescription(), itemDTO.getWeight()));
            }
        }

        // 4. 重建玩家
        String currentRoomId = dto.getCurrentRoomId();
        Room currentRoom = roomIdMap.get(currentRoomId);
        if (currentRoom == null) currentRoom = roomIdMap.get("outside");
        String playerName = dto.getPlayerName() != null ? dto.getPlayerName() : "冒险者";
        player = new Player(playerName, currentRoom, dto.getMaxWeight() > 0 ? dto.getMaxWeight() : 10.0);

        // 重建背包
        if (dto.getInventory() != null) {
            for (ItemDTO itemDTO : dto.getInventory()) {
                player.getInventory().add(new Item(itemDTO.getDescription(), itemDTO.getWeight()));
            }
        }

        // 5. 重建历史记录栈
        history.clear();
        if (dto.getHistoryRoomIds() != null) {
            for (String histRoomId : dto.getHistoryRoomIds()) {
                Room histRoom = roomIdMap.get(histRoomId);
                if (histRoom != null) {
                    history.push(histRoom);
                }
            }
        }

        // ========== 修复：读档后标记游戏为运行中 ==========
        this.running = true;
    }

    /**
     * 查找房间对应的 ID.
     */
    public String findRoomId(Room room) {
        for (java.util.Map.Entry<String, Room> entry : roomIdMap.entrySet()) {
            if (entry.getValue() == room) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 获取房间 ID 映射表.
     */
    public java.util.Map<String, Room> getRoomIdMap() {
        return roomIdMap;
    }

    /**
     * 开始新游戏（重置玩家到起始房间）.
     * @param playerName 玩家角色名称.
     */
    public void startNewGame(String playerName) {
        // 重新创建世界（重置所有物品位置）
        allRooms.clear();
        roomIdMap.clear();
        createRooms();
        // 更新玩家名称
        player = new Player(playerName, roomIdMap.get("outside"), 10.0);
        history.clear();
        // 标记为运行中
        this.running = true;
        System.out.println("=== 新游戏开始 ===");
        System.out.println("欢迎你，" + playerName + "！");
        System.out.println(getCurrentRoom().getShortDescription());
    }

    /**
     * 是否正在运行中.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * 退出到主菜单.
     */
    public void exitToMenu() {
        running = false;
    }

    /**
     *  游戏主控循环，直到用户输入退出命令后结束整个程序.
     */
    public void play()
    {
        printWelcome();

        // Enter the main command loop.  Here we repeatedly read commands and
        // execute them until the game is over.

        boolean finished = false;
        while (! finished) {
            Command command = parser.getCommand();
            finished = processCommand(command);
        }
        System.out.println("Thank you for playing.  Good bye.");
    }

    /**
     * 向用户输出欢迎信息.
     */
    private void printWelcome()
    {
        System.out.println();
        System.out.println("Welcome to the World of Zuul!");
        System.out.println("World of Zuul is a new, incredibly boring adventure game.");
        System.out.println("Type 'help' if you need help.");
        System.out.println();
        // 修改：直接调用 getter
        System.out.println(getCurrentRoom().getShortDescription());
    }

    /**
     * 执行用户输入的游戏指令.
     * @param command 待处理的游戏指令，由解析器从用户输入内容生成.
     * @return 如果执行的是游戏结束指令，则返回true，否则返回false.
     */
    private boolean processCommand(Command command)
    {
        if(command.isUnknown()) {
            System.out.println("I don't know what you mean...");
            return false;
        }

        String commandWord = command.getCommandWord();
        // 改进点：从 CommandWords 中获取执行对象，而不是 Game 自己的 Map
        CommandExecution execution = parser.getCommandWords().get(commandWord);

        if (execution != null) {
            // 多态调用：执行具体命令的逻辑
            return execution.execute(this, command);
        } else {
            System.out.println("This command is not implemented yet!");
            return false;
        }
    }

    /**
     * 外部获取到它的 parser 对象.
     */
    public Parser getParser() {
        return parser;
    }

    /**
     * 【新增】暴露 Player 对象供命令类使用
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 将当前房间压入历史栈中.
     * 应该在玩家移动之前调用此方法.
     */
    public void pushHistory() {
        history.push(getCurrentRoom()); // 使用 getter
    }

    /**
     * 执行回退逻辑.
     * 如果栈为空，说明已经回到了起点，打印提示信息.
     */
    public void goBack() {
        if (history.empty()) {
            System.out.println("You can't go back!");
        } else {
            // 弹出上一个房间并设为当前房间
            setCurrentRoom(history.pop()); // 使用 setter
            // 回退后，显示简短描述
            System.out.println(getCurrentRoom().getShortDescription());
        }
    }

    /**
     * 随机获取一个房间（不包含传送房间本身）.
     * @return 随机选中的房间.
     */
    public Room getRandomRoom() {
        int index = random.nextInt(allRooms.size());
        return allRooms.get(index);
    }

    // --- 修改 Getter/Setter 委托给 Player ---

    public Room getCurrentRoom() {
        return player.getCurrentRoom();
    }

    /**
     * 设置当前房间.
     * 包含对传送房间的特殊处理逻辑.
     * @param room 目标房间.
     */
    public void setCurrentRoom(Room room) {
        // 【关键逻辑】检查是否是传送房间
        if (room instanceof TransporterRoom) {
            System.out.println("You have entered a mysterious room with glowing symbols...");
            System.out.println("A magical force teleports you to a random place!");

            // 随机选择一个新房间
            player.setCurrentRoom(getRandomRoom()); // 修改：更新玩家位置
        } else {
            player.setCurrentRoom(room); // 修改：更新玩家位置
        }
    }

    /**
     * 【新增 for GUI】执行字符串命令（供 Web Server 调用）.
     */
    public void runCommand(String cmdLine) {
        Command command = parser.parseCommand(cmdLine);
        processCommand(command);
    }
}