package cn.edu.whut.sept.zuul;

import java.util.HashMap;

/**
 * 该类持有游戏所有的命令词汇。
 * 它用于识别命令是否合法。
 */
public class CommandWords
{
    // 使用 HashMap 替代原来的 String[] 数组
    // 键：命令单词 (String)，值：命令执行对象 (CommandExecution)
    private final HashMap<String, CommandExecution> commands;

    /**
     * 初始化命令词汇表。
     */
    public CommandWords()
    {
        commands = new HashMap<>();
    }

    /**
     * 注册一个新的命令。
     * @param name 命令名称 (如 "go")
     * @param command 命令执行逻辑对象
     */
    public void register(String name, CommandExecution command) {
        commands.put(name, command);
    }

    /**
     * 获取指定命令词对应的执行对象。
     * @param word 命令单词
     * @return 对应的 CommandExecution 对象，如果不存在则返回 null
     */
    public CommandExecution get(String word) {
        return commands.get(word);
    }

    /**
     * 检查给定的字符串是否是合法的命令词。
     */
    public boolean isCommand(String aString)
    {
        return commands.containsKey(aString);
    }

    /**
     * 打印所有合法的命令。
     */
    public void showAll()
    {
        for(String command : commands.keySet()) {
            System.out.print(command + "  ");
        }
        System.out.println();
    }
}
