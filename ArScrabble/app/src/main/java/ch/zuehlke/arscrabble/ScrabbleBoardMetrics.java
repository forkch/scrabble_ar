package ch.zuehlke.arscrabble;

import org.opencv.core.Mat;

/**
 * Created with love by fork on 26.11.14.
 */
public class ScrabbleBoardMetrics {


    private static final float marginLeftPercent = 6.76f / 100.f;
    private static final float marginRightPercent = 6.76f / 100.f;
    private static final float marginTopPercent = 4.19f / 100.f;
    private static final float marginBottomPercent = 9.21f / 100.f;
    private static final float cellHeightPercent = 5.726f / 100.f;


    private int marginLeft = 44;
    private int marginRight = 44;
    private int marginTop = 20;
    private int marginBottom = 80;
    private int cellWidth = 42;
    private int cellHeight = 42;

    private ScrabbleBoardMetrics(Mat image) {
        marginLeft = (int) (image.cols() * marginLeftPercent);
        marginRight = (int) (image.cols() * marginRightPercent);
        marginTop = (int) (image.rows() * marginTopPercent);
        marginBottom = (int) (image.rows() * marginBottomPercent);
        cellWidth = (int) (image.rows() * cellHeightPercent);
        cellHeight = (int) (image.rows() * cellHeightPercent);
    }

    public static ScrabbleBoardMetrics metricsFromImage(Mat image) {
        return new ScrabbleBoardMetrics(image);
    }

    public int getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(int marginLeft) {
        this.marginLeft = marginLeft;
    }

    public int getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(int marginRight) {
        this.marginRight = marginRight;
    }

    public int getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }

    public int getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = marginBottom;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public void setCellWidth(int cellWidth) {
        this.cellWidth = cellWidth;
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public void setCellHeight(int cellHeight) {
        this.cellHeight = cellHeight;
    }


}
