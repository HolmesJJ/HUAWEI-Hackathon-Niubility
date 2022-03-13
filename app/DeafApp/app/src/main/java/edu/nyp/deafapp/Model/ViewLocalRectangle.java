package edu.nyp.deafapp.Model;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class ViewLocalRectangle {
    private float width;//宽度
    private float minX;
    private float minY;
    private float maxX;
    private float maxY;
    private float area;//相交面积

    public ViewLocalRectangle(float width, float minX, float minY) {
        this.width = width;
        this.minX = minX;
        this.minY = minY;
        this.maxX = width + minX;
        this.maxY = width + minY;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getMinX() {
        return minX;
    }

    public void setMinX(float minX) {
        this.minX = minX;
    }

    public float getMinY() {
        return minY;
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    public float getMaxX() {
        return maxX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    public float getMaxY() {
        return maxY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }

    public float getArea() {
        return area;
    }

    public void setArea(float area) {
        this.area = area;
    }

    /**
     * 求两个矩形的相交面积
     *
     * @param viewLocalRectangle1
     * @return
     */
    public float calculatedArea(ViewLocalRectangle viewLocalRectangle1) {
        area = 0;
        if (viewLocalRectangle1.minX > maxX || viewLocalRectangle1.maxX < minX || viewLocalRectangle1.maxY < minY || viewLocalRectangle1.minY > maxY)
            return area;
        float minx = Math.max(minX, viewLocalRectangle1.getMinX());
        float miny = Math.max(minY, viewLocalRectangle1.getMinY());
        float maxx = Math.max(maxX, viewLocalRectangle1.getMaxX());
        float maxy = Math.max(maxY, viewLocalRectangle1.getMaxY());
        area = Math.abs(minx - maxx) * Math.abs(miny - maxy);
        return area;

    }


}
