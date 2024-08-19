
public class Situation {
    private final int x; // the starting point of shape
    private final int rotate;// the number of rotate
    private int value;

    public Situation(int x, int rotate) {
        this.x = x;
        this.rotate = rotate;
    }

    public int getX() {
        return x;
    }

    public int getRotate() {
        return rotate;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}