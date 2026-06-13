package cn.edu.whut.sept.zuul;

/**
 * 传送房间.
 * 这是一个特殊的房间，玩家进入后会被随即传送到其他房间.
 */
public class TransporterRoom extends Room {
    /**
     * 创建一个传送房间.
     * @param description 房间描述.
     */
    public TransporterRoom(String description) {
        super(description);
    }
}

