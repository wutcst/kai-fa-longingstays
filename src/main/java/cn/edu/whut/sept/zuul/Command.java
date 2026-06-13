package cn.edu.whut.sept.zuul;

/**
 * 该类持有用户输入的命令信息。
 * 命令由两个字符串组成：命令词（CommandWord）和第二词（SecondWord）。
 * * 例如用户输入 "go east"，那么：
 * - commandWord = "go"
 * - secondWord = "east"
 * * 如果用户只输入了 "help"，那么：
 * - commandWord = "help"
 * - secondWord = null
 * @version 1.0
 */
public class Command
{
    private final String commandWord;
    private final String secondWord;

    /**
     * 创建一个命令对象。
     * 第一词和第二词必须由调用者提供。
     * @param firstWord  用户输入的第一个词（命令词）。
     * @param secondWord 用户输入的第二个词。
     */
    public Command(String firstWord, String secondWord)
    {
        commandWord = firstWord;
        this.secondWord = secondWord;
    }

    /**
     * 返回该命令的命令词（第一个词）。
     * 如果命令未被识别，则返回 null。
     * @return 命令词。
     */
    public String getCommandWord()
    {
        return commandWord;
    }

    /**
     * 返回该命令的第二个词。
     * 如果只有命令词而没有参数，则返回 null。
     * @return 命令的第二个参数。
     */
    public String getSecondWord()
    {
        return secondWord;
    }

    /**
     * @return 如果该命令未被理解（即命令词为 null），则返回 true。
     */
    public boolean isUnknown()
    {
        return (commandWord == null);
    }

    /**
     * @return 如果该命令有第二个词，则返回 true；否则返回 false。
     */
    public boolean hasSecondWord()
    {
        return (secondWord != null);
    }
}
