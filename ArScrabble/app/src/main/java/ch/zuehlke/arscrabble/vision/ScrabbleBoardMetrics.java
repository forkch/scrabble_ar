package ch.zuehlke.arscrabble.vision;

import org.opencv.core.Mat;

/**
 * Created with love by fork on 26.11.14.
 */
public class ScrabbleBoardMetrics {


    private static final float marginLeftPercent = 6.76f / 100.f;
    private static final float marginRightPercent = 6.76f / 100.f;
    private static final float marginTopPercent = 4.19f / 100.f;
    private static final float marginBottomPercent = 9.21f / 100.f;
    private static final float cellHeightPercent = 5.8f / 100.f;
    private static final float cellWidthPercent = 5.726f / 100.f;

    public static final int TOP_CORRECTION = -7;
    public static final int BOTTOM_CORRECTION = 0;
    public static final int RIGHT_CORRECTION = -4;
    public static final int LEFT_CORRECTION = -2;
    public static final float WIDTH_CORRECTION = 0.35f;
    public static final float HEIGHT_CORRECTION = 0.45f;
    private final float cols;
    private final float rows;

    private int marginLeft = 44;
    private int marginRight = 44;
    private int marginTop = 20;
    private int marginBottom = 80;
    private int cellWidth = 42;
    private int cellHeight = 42;

    private ScrabbleBoardMetrics(float cols, float rows, boolean addCorrection) {
        this.cols = cols;
        this.rows = rows;

        if (addCorrection) {
            marginLeft = (int) (cols * marginLeftPercent + LEFT_CORRECTION);
            marginRight = (int) (cols * marginRightPercent + RIGHT_CORRECTION);
            marginTop = (int) (rows * marginTopPercent + TOP_CORRECTION);
            marginBottom = (int) (rows * marginBottomPercent + BOTTOM_CORRECTION);

            float boardWidth = cols - marginLeft - marginRight;
            float boardHeight = rows - marginTop - marginBottom;
            cellWidth = (int) ((boardWidth / 15.f));
            cellHeight = (int) ((boardHeight / 15.f));
        } else {
            // TODO: This code is used to place jmonkey spatials on the board.. 
            marginLeft = (int) (cols * marginLeftPercent);
            marginRight = (int) (cols * marginRightPercent);
            marginTop = (int) (rows * marginTopPercent);
            marginBottom = (int) (rows * marginBottomPercent);
            cellWidth = (int) (rows * cellHeightPercent);
            cellHeight = (int) (rows * cellHeightPercent);
        }
    }

    public static ScrabbleBoardMetrics metricsFromImage(Mat image) {
        return new ScrabbleBoardMetrics(image.cols(), image.rows(), true);
    }

    public static ScrabbleBoardMetrics metricsFromImageFrom3D(Mat image) {
        return new ScrabbleBoardMetrics(image.cols(), image.rows(), false);
    }

    public int getMarginLeft() {
        return marginLeft;
    }


    public int getMarginRight() {
        return marginRight;
    }


    public int getMarginTop() {
        return marginTop;
    }


    public int getMarginBottom() {
        return marginBottom;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public float getX(int verticalIdx) {
        final float x = this.getMarginLeft() + verticalIdx * this.getCellWidth() + verticalIdx * ScrabbleBoardMetrics.WIDTH_CORRECTION;
        if (x < 0 || x > cols)
            throw new IllegalStateException("x out of bounds: " + x);
        return x;
    }

    public float getY(int horizontalIdx) {
        final float y = this.getMarginTop() + horizontalIdx * this.getCellHeight() + horizontalIdx * ScrabbleBoardMetrics.HEIGHT_CORRECTION;

        if (y < 0 || y > rows)
            throw new IllegalStateException("y out of bounds: " + y);
        return y;
    }

}
