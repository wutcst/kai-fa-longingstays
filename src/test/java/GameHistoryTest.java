package test; // 【修改点】

import cn.edu.whut.sept.zuul.*; // 【修改点】

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameHistoryTest {
    private Game game;
    private Room room1;
    private Room room2;

    @Before
    public void setUp() {
        game = new Game();
        room1 = new Room("Room 1");
        room2 = new Room("Room 2");

        room1.setExit("east", room2);
        room2.setExit("west", room1);

        // 注意：这里假设你之前已经为 Game 类添加了 getPlayer() 方法
        // 如果报错说找不到 getPlayer，请确保 Game.java 中有 public Player getPlayer() { return player; }
        game.getPlayer().setCurrentRoom(room1);
    }

    @Test
    public void testBackFunction() {
        // 1. 模拟移动： Room1 -> Room2
        game.pushHistory();
        game.getPlayer().setCurrentRoom(room2);

        assertEquals("当前应在 Room 2", room2, game.getCurrentRoom());

        // 2. 执行回退
        game.goBack();

        // 3. 验证是否回到了 Room1
        assertEquals("执行 back 后应回到 Room 1", room1, game.getCurrentRoom());
    }

    @Test
    public void testBackAtStart() {
        Room startRoom = game.getCurrentRoom();
        game.goBack();
        assertEquals("在起点回退应无效", startRoom, game.getCurrentRoom());
    }
}

