package com.github.zk.core;

import com.github.zk.pojo.ImageChunk;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author zhaokai
 * @since 图像处理抽象工厂
 */
public abstract class AbstractImageFactory {

    public void sendImage(String absoluteFilePath) throws IOException {
        File file = new File(absoluteFilePath);
        ImageHandler imageHandler = createHandle();
        BufferedImage srcImage = imageHandler.loadImage(file);
        List<ImageChunk>[] splitImage = imageHandler.splitImage(srcImage);
        for (List<ImageChunk> imageChunks : splitImage) {
            byte[] chunksByteArray = imageHandler.object2BinaryHandle(imageChunks);
            doSend(chunksByteArray);
        }

    }

    protected abstract ImageHandler createHandle();

    protected abstract void doSend(byte[] chunksByteArray);
}
