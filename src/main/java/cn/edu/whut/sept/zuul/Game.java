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

    /** 任务物品候选池（确保 4 件总重 ≤ 20kg，吃饼干后可完成） */
    private static final List<String> QUEST_ITEM_POOL = java.util.Arrays.asList(
        "生锈的铁锹", "破旧的地图", "空鸟巢", "鹅卵石堆",
        "道具剑", "乐谱架", "威士忌酒瓶",
        "飞镖盘", "酒吧凳", "显微镜", "烧杯组",
        "化学试剂瓶", "实验记录本", "魔法饼干",
        "笔记本电脑", "咖啡杯", "文件堆"
    );

    /** 本次游戏需收集的任务物品 */
    private List<String> requiredItems;

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
        requiredItems = new ArrayList<>();
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
        outside.addItem(new Item("生锈的铁锹：一把木柄生锈的铁锹，看起来被遗弃了很久", 3.2, 5, 65));
        outside.addItem(new Item("破旧的地图：一张泛黄的羊皮纸地图，边缘有烧焦的痕迹，大部分字迹已模糊", 0.1, 75, 70));
        outside.addItem(new Item("空鸟巢：一个由树枝和干草筑成的鸟巢，里面有几片破碎的蛋壳", 0.4, 35, 12));
        outside.addItem(new Item("鹅卵石堆：一堆光滑的鹅卵石，其中一块上面刻着奇怪的符号", 5.0, 70, 46));

        // office (办公室)
        office.addItem(new Item("笔记本电脑：一台黑色的ThinkPad笔记本电脑，屏幕有细微划痕，电池续航约3小时", 2.5, 45, 48));
        office.addItem(new Item("咖啡杯：印有公司logo的白色陶瓷杯，杯底有褐色咖啡渍", 0.3, 62, 44));
        office.addItem(new Item("文件堆：一沓未处理的财务报表和项目计划书，最上面的文件标着“紧急”", 1.2, 28, 50));
        office.addItem(new Item("转椅：黑色皮质办公椅，右侧扶手有破损", 8.5, 48, 75));

        // lab (实验室)
        lab.addItem(new Item("显微镜：一台老式光学显微镜，目镜有些模糊，载物台上有玻璃切片", 4.7, 25, 55));
        lab.addItem(new Item("烧杯组：三个不同尺寸的玻璃烧杯，最小的那个有裂痕", 1.8, 55, 50));
        lab.addItem(new Item("化学试剂瓶：棕色玻璃瓶，标签写着“H₂SO₄ 浓硫酸 危险”，瓶盖紧闭", 0.9, 78, 30));
        lab.addItem(new Item("实验记录本：皮质封面的笔记本，记录着奇怪的化学公式和潦草的笔记", 0.6, 70, 68));

        // pub (酒吧)
        pub.addItem(new Item("木制吧台：长长的桃花心木吧台，表面有多年使用留下的划痕和酒杯印", 45.0, 50, 55));
        pub.addItem(new Item("威士忌酒瓶：半空的杰克丹尼威士忌，瓶身标签已磨损", 1.1, 55, 42));
        pub.addItem(new Item("飞镖盘：挂在墙上的标准飞镖盘，中心区域有几个深深的孔洞", 3.2, 82, 25));
        pub.addItem(new Item("酒吧凳：高脚旋转凳，红色皮革坐垫有开裂", 6.3, 35, 78));

        // theater (剧院)
        theater.addItem(new Item("红色帷幕：厚重的天鹅绒帷幕，边缘有金线刺绣，有些地方已经褪色", 22.0, 40, 5));
        theater.addItem(new Item("舞台聚光灯：老式的金属聚光灯，镜片有裂纹，但仍能工作", 12.5, 50, 5));
        theater.addItem(new Item("道具剑：舞台用的仿古长剑，剑身是钝的铝合金，剑柄有假宝石", 1.8, 60, 70));
        theater.addItem(new Item("乐谱架：木制乐谱架，上面还放着一份泛黄的乐谱", 2.4, 80, 60));

        // 【新增】添加魔法饼干到某个房间（比如 Lab）
        lab.addItem(new Item("魔法饼干", 0.5, 42, 72));

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
            dto.getInventory().add(new ItemDTO(item.getDescription(), item.getWeight(), item.getX(), item.getY()));
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
                roomDTO.getItems().add(new ItemDTO(item.getDescription(), item.getWeight(), item.getX(), item.getY()));
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

        // 序列化任务物品
        dto.setRequiredItems(new ArrayList<>(requiredItems));

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
                double ix = itemDTO.getX();
                double iy = itemDTO.getY();

                // 【核心修改】：旧存档坐标缺失为 (0,0) 时，严格按照设定表恢复坐标
                if (ix == 0.0 && iy == 0.0) {
                    String desc = itemDTO.getDescription();
                    if (desc.contains("铁锹")) { ix = 5.0; iy = 65.0; }
                    else if (desc.contains("地图")) { ix = 75.0; iy = 70.0; }
                    else if (desc.contains("空鸟巢")) { ix = 35.0; iy = 12.0; }
                    else if (desc.contains("鹅卵石")) { ix = 70.0; iy = 46.0; }
                    else if (desc.contains("笔记本电脑")) { ix = 45.0; iy = 48.0; }
                    else if (desc.contains("咖啡杯")) { ix = 62.0; iy = 44.0; }
                    else if (desc.contains("文件堆")) { ix = 28.0; iy = 50.0; }
                    else if (desc.contains("转椅")) { ix = 48.0; iy = 75.0; }
                    else if (desc.contains("显微镜")) { ix = 25.0; iy = 55.0; }
                    else if (desc.contains("烧杯")) { ix = 55.0; iy = 50.0; }
                    else if (desc.contains("化学试剂")) { ix = 78.0; iy = 30.0; }
                    else if (desc.contains("实验记录本")) { ix = 70.0; iy = 68.0; }
                    else if (desc.contains("吧台")) { ix = 50.0; iy = 55.0; }
                    else if (desc.contains("威士忌")) { ix = 55.0; iy = 42.0; }
                    else if (desc.contains("飞镖盘")) { ix = 82.0; iy = 25.0; }
                    else if (desc.contains("酒吧凳")) { ix = 35.0; iy = 78.0; }
                    else if (desc.contains("帷幕")) { ix = 40.0; iy = 5.0; }
                    else if (desc.contains("聚光灯")) { ix = 50.0; iy = 5.0; }
                    else if (desc.contains("道具剑")) { ix = 60.0; iy = 70.0; }
                    else if (desc.contains("乐谱架")) { ix = 80.0; iy = 60.0; }
                    else if (desc.contains("魔法饼干")) { ix = 42.0; iy = 72.0; }
                    else { ix = 50.0; iy = 50.0; } // 兜底：未知物品放中间
                }
                room.getItems().add(new Item(itemDTO.getDescription(), itemDTO.getWeight(), ix, iy));
            }
        }

        // 4. 重建玩家
        String currentRoomId = dto.getCurrentRoomId();
        Room currentRoom = roomIdMap.get(currentRoomId);
        if (currentRoom == null) currentRoom = roomIdMap.get("outside");
        String playerName = dto.getPlayerName() != null ? dto.getPlayerName() : "冒险者";
        player = new Player(playerName, currentRoom, dto.getMaxWeight() > 0 ? dto.getMaxWeight() : 10.0);

        // 重建背包 (紧接着重建玩家的代码)
        if (dto.getInventory() != null) {
            for (ItemDTO itemDTO : dto.getInventory()) {
                double ix = itemDTO.getX();
                double iy = itemDTO.getY();

                // 【核心修改】：背包里的物品防错处理
                if (ix == 0.0 && iy == 0.0) {
                    ix = 20 + Math.random() * 60;
                    iy = 20 + Math.random() * 60;
                }
                player.getInventory().add(new Item(itemDTO.getDescription(), itemDTO.getWeight(), ix, iy));
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

        // 6. 恢复任务物品列表（兼容旧存档无此字段）
        this.requiredItems = dto.getRequiredItems() != null
            ? new ArrayList<>(dto.getRequiredItems())
            : new ArrayList<>();

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
     * 获取本次游戏需要收集的任务物品列表.
     */
    public List<String> getRequiredItems() {
        return requiredItems;
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
        // 随机选择 4 个任务物品
        List<String> pool = new ArrayList<>(QUEST_ITEM_POOL);
        java.util.Collections.shuffle(pool, random);
        requiredItems = new ArrayList<>(pool.subList(0, Math.min(4, pool.size())));
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
        // 【修改】：移除了一进入传送房间就强制传送的逻辑
        // 让玩家可以安全地走入这个房间，并在里面自由探索
        player.setCurrentRoom(room);
    }

    /**
     * 【新增 for GUI】执行字符串命令（供 Web Server 调用）.
     */
    public void runCommand(String cmdLine) {
        // 【新增】：专门开放给前端地图引擎的隐藏触发器（不在游戏 help 列表里显示）
        if ("trigger_teleport".equals(cmdLine.trim())) {
            if (getCurrentRoom() instanceof TransporterRoom) {
                pushHistory(); // 将传送前的房间存入历史栈
                System.out.println("✨ 你踏入了发光的魔法阵，剧烈的空间扭曲将你吞噬...");
                player.setCurrentRoom(getRandomRoom()); // 触发真正的随机传送
            }
            return;
        }

        Command command = parser.parseCommand(cmdLine);
        processCommand(command);
    }
}