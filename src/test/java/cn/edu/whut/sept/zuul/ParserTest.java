package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {
    private Parser parser;

    @Before
    public void setUp() {
        // 创建一个完整的 Game 来初始化 Parser（因为 Parser 需要 CommandWords 注册）
        Game game = new Game();
        parser = game.getParser();
    }

    // ===== parseCommand =====

    @Test
    public void testParseCommandTwoWords() {
        Command cmd = parser.parseCommand("go east");
        assertEquals("go", cmd.getCommandWord());
        assertEquals("east", cmd.getSecondWord());
        assertFalse(cmd.isUnknown());
    }

    @Test
    public void testParseCommandOneWord() {
        Command cmd = parser.parseCommand("look");
        assertEquals("look", cmd.getCommandWord());
        assertNull(cmd.getSecondWord());
        assertFalse(cmd.isUnknown());
    }

    @Test
    public void testParseCommandEmpty() {
        Command cmd = parser.parseCommand("");
        assertNull(cmd.getCommandWord());
        assertTrue(cmd.isUnknown());
    }

    @Test
    public void testParseCommandUnknown() {
        Command cmd = parser.parseCommand("xyz123 something");
        assertNull(cmd.getCommandWord());
        assertEquals("something", cmd.getSecondWord());
        assertTrue(cmd.isUnknown());
    }

    @Test
    public void testParseCommandNull() {
        try {
            parser.parseCommand(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // 已知缺陷：null 输入导致 NPE
        }
    }

    @Test
    public void testParseCommandThreeTokens() {
        // "drop 道具剑 30 70" — 第三个 token 起被拼入 secondWord
        Command cmd = parser.parseCommand("drop 道具剑 30 70");
        assertEquals("drop", cmd.getCommandWord());
        // Parser.parseCommand 将剩余部分拼接到 secondWord
        assertTrue(cmd.getSecondWord().contains("道具剑"));
        assertTrue(cmd.getSecondWord().contains("30"));
        assertTrue(cmd.getSecondWord().contains("70"));
    }

    @Test
    public void testParseCommandExtraSpaces() {
        Command cmd = parser.parseCommand("  go   east  ");
        assertEquals("go", cmd.getCommandWord());
        assertEquals("east", cmd.getSecondWord());
    }

    @Test
    public void testParseCommandCaseSensitive() {
        // 命令词区分大小写
        Command cmd = parser.parseCommand("GO east");
        assertTrue(cmd.isUnknown());
    }

    // ===== showCommands =====

    @Test
    public void testShowCommandsOutput() {
        String out = TestHelper.captureOutput(() -> parser.showCommands());
        assertTrue(out.contains("go"));
        assertTrue(out.contains("quit"));
    }
}
