package com.github.zk;

import com.github.zk.core.AbstractImageFactory;
import com.github.zk.core.ImageHandler;
import com.github.zk.core.PixelsHandler;
import com.github.zk.pojo.ImageChunk;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws IOException {
        String imagePath = "C:\\Users\\zhaokai\\Pictures\\a.jpg";
        AbstractImageFactory factory = new AbstractImageFactory() {
            @Override
            protected ImageHandler createHandle() {
                return new PixelsHandler();
            }

            @Override
            protected void doSend(byte[] chunksByteArray) {
                PixelsHandler pixelsHandler = new PixelsHandler();
                List<ImageChunk> imageChunks = pixelsHandler.binary2ObjectHandle(chunksByteArray);
                System.out.println("发送到服务端");
                BufferedImage bufferedImage = pixelsHandler.mergeImage(imageChunks);
                pixelsHandler.saveImage(bufferedImage, "C:\\Users\\zhaokai\\Pictures\\create\\1.jpg");
            }
        };
        factory.sendImage(imagePath);
    }
}
