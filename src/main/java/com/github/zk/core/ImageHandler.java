package com.github.zk.core;


import com.github.zk.pojo.ImageChunk;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author zhaokai
 * @since 1.0
 */
public interface ImageHandler {
    /**
     * 读取图片
     *
     * @param imagePath 图片地址
     * @return 图片对象
     */
    BufferedImage loadImage(File imagePath) throws IOException;

    /**
     * 拆分图片
     *
     * @param image 图片对象
     * @return 图片块集合
     */
    List<ImageChunk>[] splitImage(BufferedImage image);

    /**
     * 图片对象集合转换字节
     *
     * @param chunks 图片块集合
     * @return 字节数组
     */
    byte[] object2BinaryHandle(List<ImageChunk> chunks);

    /**
     * 字节转图片对象集合
     *
     * @param chunksByteArray 像素字节数组
     * @return 图片对象集合
     */
    List<ImageChunk> binary2ObjectHandle(byte[] chunksByteArray);

    /**
     * 合并图片
     *
     * @param imageChunks 图片块集合
     * @return 完整图片
     */
    BufferedImage mergeImage(List<ImageChunk> imageChunks);

    /**
     * 生成本地图片
     *
     * @param image 图片对象
     * @param fileName 图片名称（绝对路径）
     * @return 生成是否成功结果
     */
    boolean saveImage(BufferedImage image, String fileName);

}
