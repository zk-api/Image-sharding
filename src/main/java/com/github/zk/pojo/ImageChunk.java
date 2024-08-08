package com.github.zk.pojo;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * @author zhaokai
 * @since 1.0
 */
public class ImageChunk implements Serializable {

    private static final long serialVersionUID = 5576932937733950484L;

    /**
     * 图像索引
     */
    private Integer index;
    /**
     * 像素块
     */
    private BufferedImage pixel;
    /**
     * 像素所在行号
     */
    private Integer line;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public BufferedImage getPixel() {
        return pixel;
    }

    public void setPixel(BufferedImage pixel) {
        this.pixel = pixel;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }
}
