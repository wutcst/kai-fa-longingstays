package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandWordsTest {
    private CommandWords commands;

    @Before
    public void setUp() {
        commands = new CommandWords();
    }

    @Test
    public void testNewCommandWordsIsEmpty() {
        assertFalse(commands.isCommand("go"));
        assertNull(commands.get("go"));
    }

    @Test
    public void testRegisterAndGet() {
        CommandGo go = new CommandGo();
        commands.register("go", go);
        assertSame(go, commands.get("go"));
    }

    @Test
    public void testIsCommand() {
        assertFalse(commands.isCommand("go"));
        commands.register("go", new CommandGo());
        assertTrue(commands.isCommand("go"));
    }

    @Test
    public void testIsCommandCaseSensitive() {
        commands.register("go", new CommandGo());
        assertFalse(commands.isCommand("GO"));
        assertFalse(commands.isCommand("Go"));
    }

    @Test
    public void testRegisterMultipleCommands() {
        commands.register("go", new CommandGo());
        commands.register("look", new CommandLook());
        commands.register("back", new CommandBack());

        assertTrue(commands.isCommand("go"));
        assertTrue(commands.isCommand("look"));
        assertTrue(commands.isCommand("back"));
        assertFalse(commands.isCommand("quit"));
    }

    @Test
    public void testGetUnknownReturnsNull() {
        assertNull(commands.get("nonexistent"));
    }

    @Test
    public void testOverwriteCommand() {
        CommandGo go1 = new CommandGo();
        CommandGo go2 = new CommandGo();
        commands.register("go", go1);
        commands.register("go", go2);
        assertSame(go2, commands.get("go"));
    }

    @Test
    public void testShowAllOutput() {
        commands.register("go", new CommandGo());
        commands.register("look", new CommandLook());

        String out = TestHelper.captureOutput(() -> commands.showAll());
        assertTrue(out.contains("go"));
        assertTrue(out.contains("look"));
    }

    @Test
    public void testShowAllEmpty() {
        String out = TestHelper.captureOutput(() -> commands.showAll());
        // 空表输出换行符
        assertNotNull(out);
    }

    @Test
    public void testAllRegisteredCommandsCount() {
        // 模拟 Game 中的注册
        commands.register("go", new CommandGo());
        commands.register("help", new CommandHelp());
        commands.register("quit", new CommandQuit());
        commands.register("look", new CommandLook());
        commands.register("back", new CommandBack());
        commands.register("take", new CommandTake());
        commands.register("drop", new CommandDrop());
        commands.register("items", new CommandItems());
        commands.register("eat", new CommandEat());
        commands.register("buy", new CommandBuy());

        // 验证 10 个命令
        String out = TestHelper.captureOutput(() -> commands.showAll());
        assertTrue(out.contains("go"));
        assertTrue(out.contains("help"));
        assertTrue(out.contains("quit"));
        assertTrue(out.contains("look"));
        assertTrue(out.contains("back"));
        assertTrue(out.contains("take"));
        assertTrue(out.contains("drop"));
        assertTrue(out.contains("items"));
        assertTrue(out.contains("eat"));
        assertTrue(out.contains("buy"));
    }
}
