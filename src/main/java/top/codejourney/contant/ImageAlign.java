package top.codejourney.contant;

/**
 * @author Antvictor
 * @date 2024/4/10
 **/
public enum ImageAlign {
    LEFT(0),
    CENTER(1),
    RIGHT(2);

    private int value;

    ImageAlign( int value)  {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
