import java.util.ArrayList;
import java.util.Random;

public class Simulation {
    private final int[][] cells;
    private final Shape shape;
    private final Shape nextShape;
    private ArrayList<Situation> situations;
    private final int fieldWidth, fieldHeight;
    private int[] cellsDeeps;
    private int[] cellsDeepsNext;
    private int[] boxesHeights;
    private int[] boxesHeightsNext;
    private final static int HOLE_PENALTY = 10;

    private boolean processNextShape;
    public Simulation(int[][] cells, int[][] boxes, int[][]nextBoxes) {
        this.cells = cells;
        this.shape = new Shape(boxes);
        this.nextShape=new Shape(nextBoxes);
        fieldWidth = cells[0].length;
        fieldHeight = cells.length;
        processNextShape = true;
        cellsDeeps = new int[fieldWidth];
        cellsDeepsNext = new int[fieldWidth];

        calCellsDeeps();
    }

    private void calCellsDeeps() {
        for (int x = 0; x < fieldWidth; ++x) {
            cellsDeeps[x] = fieldHeight;
            for (int y = 0; y < fieldHeight; ++y) {
                if (cells[y][x] != 0) {
                    cellsDeeps[x] = y;
                    break;
                }
            }
        }
    }

    private void calCellsDeepsNext() {
        for (int x = 0; x < fieldWidth; ++x) {
            cellsDeepsNext[x] = fieldHeight;
            for (int y = 0; y < fieldHeight; ++y) {
                if (cells[y][x] != 0) {
                    cellsDeepsNext[x] = y;
                    break;
                }
            }
        }
    }

    private void calBoxesBottom(int[][] boxes) {
        boxesHeights = new int[boxes[0].length];
        for (int y = 0; y < boxes.length; ++y) {
            for (int x = 0; x < boxes[0].length; ++x) {
                if (boxes[y][x] == 1) boxesHeights[x] = y;
            }
        }
    }

    private void calBoxesBottomNext(int[][] boxes) {
        boxesHeightsNext = new int[boxes[0].length];
        for (int y = 0; y < boxes.length; ++y) {
            for (int x = 0; x < boxes[0].length; ++x) {
                if (boxes[y][x] == 1) boxesHeightsNext[x] = y;
            }
        }
    }


    public int[] getOptimizedMove() {
        situations = new ArrayList<>();
        startSimulate();
        int minValue = situations.stream()
                .map(Situation::getValue)
                .min(Integer::compare).orElse(-1);
        if (minValue < 0) return new int[]{-1, -1};
        Situation[] sitArray = situations.stream()
                .filter(situation -> situation.getValue() == minValue)
                .toArray(Situation[]::new);
        Random random = new Random();
        Situation opSituation = sitArray[random.nextInt(sitArray.length)];
        int[] result = new int[2];
        result[0] = opSituation.getX();
        result[1] = opSituation.getRotate();
        return result;
    }

    private synchronized void startSimulate() {

        int maxRotate = shape.getMaxRotate();
        for (int rotate = 0; rotate < maxRotate; ++rotate) {
            int[][] boxes = shape.getShape();
            calBoxesBottom(boxes);
            int len = boxes[0].length;
            int maxX = fieldWidth - len;
            for (int x = 0; x <= maxX; ++x) {
                Situation sit = new Situation(x, rotate);
                int val = evaluate(boxes, x);
                if (val < 0) continue; // no need to add invalid move.
                sit.setValue(val);
                situations.add(sit);
            }
            shape.doRotateClockwise(); // due to rotate will not do real rotate.
        }
    }

    private int evaluate(int[][] boxes, int x) {
        int maxDis = fieldHeight;
        for (int offset = 0; offset < boxes[0].length; ++offset) {
            int col = offset + x;
            maxDis = Math.min(maxDis, cellsDeeps[col] - boxesHeights[offset]);
        }
        if (maxDis <= 0) return -1; // no need to consider as it is an invalid move.
        fillBoxesToCells(boxes, x, maxDis);
        int value;

        if(processNextShape)
            value = evaluateNextShape();
        else
            value = evaluateCells();
        removeBoxesFromCells(boxes, x, maxDis);
        return value;
    }

    private int evaluateNext(int[][] boxes, int x) {
        int maxDis = fieldHeight;
        for (int offset = 0; offset < boxes[0].length; ++offset) {
            int col = offset + x;
            maxDis = Math.min(maxDis, cellsDeepsNext[col] - boxesHeightsNext[offset]);
        }
        if (maxDis <= 0) return -1; // no need to consider as it is an invalid move.
        fillBoxesToCells(boxes, x, maxDis);
        int value = evaluateCells();
        removeBoxesFromCells(boxes, x, maxDis);
        return value;
    }

    private int evaluateNextShape(){
        int value = -1;
        calCellsDeepsNext();
        int maxRotate = nextShape.getMaxRotate();
        for (int rotate = 0; rotate < maxRotate; ++rotate) {
            int[][] boxes = nextShape.getShape();
            calBoxesBottomNext(boxes);
            int len = boxes[0].length;
            int maxX = fieldWidth - len;
            for (int x = 0; x <= maxX; ++x) {
                int val = evaluateNext(boxes, x);
                if(val<0) continue;
                if(value<0 || val<value) value=val;
            }
            nextShape.doRotateClockwise();
        }
        return value;
    }


    private void removeBoxesFromCells(int[][] boxes, int x, int d) {
        int moveD = d - 1;
        for (int r = 0; r < boxes.length; ++r) {
            for (int c = 0; c < boxes[0].length; ++c) {
                if (boxes[r][c] == 1) {
                    cells[r + moveD][c + x] = 0;
                }
            }
        }
    }

    private void fillBoxesToCells(int[][] boxes, int x, int d) {
        int moveD = d - 1;
        for (int r = 0; r < boxes.length; ++r) {
            for (int c = 0; c < boxes[0].length; ++c) {
                if (boxes[r][c] == 1) {
                    if (cells[r + moveD][c + x] != 0) System.out.println("ERROR in fillBoxesToCells!!!!!");
                    cells[r + moveD][c + x] = 1;
                }
            }
        }
    }

    private int evaluateCells() {
        int ret = 0;
        int levelPoint = 1;
        for (int y = fieldHeight - 1; y >= 0; --y) {
            boolean fullrow = true;
            for (int x = 0; x < fieldWidth; ++x) {
                if (cells[y][x] == 0) {
                    fullrow = false;
                    if (y > 0 && cells[y - 1][x] != 0) {// this is a whole
                        ret += HOLE_PENALTY;
                    }
                } else {
                    ret += levelPoint;
                }
            }
            levelPoint++;
            if (fullrow) {
                ret = 0; // fullrow clear frevious penalty
                levelPoint--;
            }
        }
        return ret;
    }
}