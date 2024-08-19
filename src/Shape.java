import java.awt.*;
import java.util.Arrays;

public class Shape {
    private int[][] shape;
    private int maxRotate;
    private int currentRotate;

    Shape(int[][] shape) {
        this.shape = shape;
        currentRotate = 0;
        calculateMaxRotate();
    }

    private boolean boxesMatch(int[][]boxes1,int[][]boxes2){
        int h = boxes1.length;
        int w = boxes1[0].length;
        if(boxes2.length!=h || boxes2[0].length!=w) return false;
        for(int y=0;y<h;++y) {
            for(int x = 0;x<w;++x){
                if(boxes1[y][x]!=boxes2[y][x]) return false;
            }
        }
        return true;
    }
    private void calculateMaxRotate() {
        int[][] newBoxes;
        newBoxes = shape;
        maxRotate = 0;
        do{
           maxRotate++;
           newBoxes=rotateClockWise(newBoxes);
           if(boxesMatch(shape,newBoxes)) break;
        }while(maxRotate<4);

    }
    public int getMaxRotate() {
        return maxRotate;
    }

    public int getCurrentRotate() {
        return currentRotate;
    }


    public int[][] getShape() {
        return shape;
    }

    private static int[][] rotateClockWise(int[][] boxes){
        int rows = boxes.length;
        int cols = boxes[0].length;
        int[][] rotatedShape = new int[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                rotatedShape[j][rows - 1 - i] = boxes[i][j];
            }
        }
        return rotatedShape;

    }
    private int[][] testRotateClockwise() {
        return rotateClockWise(shape);
    }


    public void doRotateClockwise() {
        int[][] rotatedShape = testRotateClockwise();
        currentRotate = ++currentRotate % maxRotate;
        setShape(rotatedShape);
    }

    public void setShape(int[][] newShape) {
        this.shape = newShape;
    }

}
