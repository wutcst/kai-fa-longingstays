package cn.edu.whut.sept.zuul;

import java.util.Scanner;

/**
 * 该解析器读取用户输入并将其解释为游戏命令。
 * 每当调用该类时，它都会从终端读取一行输入，并将该行解释为两个单词的命令。
 * 然后它将通过创建 Command 对象来返回该命令。
 * * 解析器拥有一组已知的命令词，并将根据已知命令检查用户输入。
 */
public class Parser
{
    private final CommandWords commands;  // 持有所有合法命令的引用
    private final Scanner reader;         // 用于读取用户输入的扫描器

    /**
     * 创建解析器以读取终端输入。
     */
    public Parser()
    {
        commands = new CommandWords();
        reader = new Scanner(System.in);
    }

    /**
     * @return 从用户终端读取的下一个命令。
     */
    public Command getCommand()
    {
        String inputLine;   // 将保存完整的输入行
        String word1 = null;
        String word2 = null;

        System.out.print("> ");     // 打印提示符

        inputLine = reader.nextLine();

        // 查找行中的最多两个单词
        Scanner tokenizer = new Scanner(inputLine);
        if(tokenizer.hasNext()) {
            word1 = tokenizer.next();      // 获取第一个词
            if(tokenizer.hasNext()) {
                word2 = tokenizer.next();  // 获取第二个词
                // 注意：我们可以忽略这一行的其余部分。
            }
        }

        // 现在检查这个词是否已知。如果是，创建一个命令对象。
        // 如果不是，创建一个 "null" 命令（用于未知命令）。
        if(commands.isCommand(word1)) {
            return new Command(word1, word2);
        }
        else {
            return new Command(null, word2);
        }
    }

    /**
     * 打印出所有合法的命令列表。
     */
    public void showCommands()
    {
        commands.showAll();
    }

    /**
     * 暴露 CommandWords.
     */
    public CommandWords getCommandWords() {
        return commands;
    }

    /**
     * 【新增 for GUI】解析给定的字符串命令.
     * @param inputLine 用户输入的命令行字符串.
     * @return 解析后的 Command 对象.
     */
    public Command parseCommand(String inputLine) {
        String word1 = null;
        String word2 = null;

        java.util.Scanner tokenizer = new java.util.Scanner(inputLine);
        if(tokenizer.hasNext()) {
            word1 = tokenizer.next();
            if(tokenizer.hasNext()) {
                word2 = tokenizer.next();
            }
        }
        tokenizer.close();

        if(commands.isCommand(word1)) {
            return new Command(word1, word2);
        }
        else {
            return new Command(null, word2);
        }
    }
}

