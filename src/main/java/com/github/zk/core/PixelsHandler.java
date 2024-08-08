package com.github.zk.core;

import com.github.zk.pojo.ImageChunk;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 像素处理
 *
 * @author zhaokai
 * @since 1.0
 */
public class PixelsHandler implements ImageHandler {

    private final static String FORMAT_NAME = "jpg";

    /**
     * 块大小
     */
    private int chunkSize = 10;
    /**
     * 压缩率
     */
    private int compressibility = 5;
    /**
     * 头长度
     */
    private final static int HEAD_LEN = 4;
    /**
     * 索引长度
     */
    private final static int INDEX_LEN = 4;
    /**
     * 行号长度
     */
    private final static int LINE_LEN = 4;

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void setCompressibility(int compressibility) {
        this.compressibility = compressibility;
    }

    @Override
    public BufferedImage loadImage(File imagePath) throws IOException {
        return ImageIO.read(imagePath);
    }

    @Override
    public List<ImageChunk>[] splitImage(BufferedImage image) {
        List<ImageChunk>[] imageChunksArray = new ArrayList[compressibility];
        // 原始图像宽度
        int width = image.getWidth();
        //原始图像高度
        int height = image.getHeight();
        // 像素块索引
        int chunkIndex = 0;
        int line = 0;
        for (int y = 0; y < height; y += chunkSize) {
            for (int x = 0; x < width; x += chunkSize) {
                BufferedImage chunk = getChunk(image, x, y, chunkSize);
                int arrayIndex = chunkIndex % compressibility;
                if (imageChunksArray[arrayIndex] == null) {
                    imageChunksArray[arrayIndex] = new ArrayList<>();
                }
                ImageChunk imageChunk = new ImageChunk();
                imageChunk.setIndex(chunkIndex);
                imageChunk.setPixel(chunk);
                imageChunk.setLine(line);
                imageChunksArray[arrayIndex].add(imageChunk);
                chunkIndex++;
            }
            line++;
        }
        return imageChunksArray;
    }

    @Override
    public byte[] object2BinaryHandle(List<ImageChunk> chunks) {
        byte[] chunksByteArray = null;
        for (ImageChunk chunk : chunks) {
            byte[] chunkByteArray = imageChunk2ByteArray(chunk);
            if (chunksByteArray == null) {
                chunksByteArray = chunkByteArray;
            } else {
                byte[] tempByteArray = new byte[chunksByteArray.length + chunkByteArray.length];
                System.arraycopy(chunksByteArray, 0, tempByteArray, 0, chunksByteArray.length);
                System.arraycopy(chunkByteArray, 0, tempByteArray, chunksByteArray.length, chunkByteArray.length);
                chunksByteArray = tempByteArray;
            }
        }
        return chunksByteArray;
    }

    /**
     * 图片对象转字节数组
     *
     * @param imageChunk 图片对象
     * @return 字节数组
     */
    private byte[] imageChunk2ByteArray(ImageChunk imageChunk) {
        int index = imageChunk.getIndex();
        int line = imageChunk.getLine();
        BufferedImage pixel = imageChunk.getPixel();

        //数据格式 4字节头（字节总长度） + 4字节索引 + 4字节行号 + 变长图片长度
        byte[] indexByteArray = int2ByteArray(index);
        byte[] lineByteArray = int2ByteArray(line);
        byte[] imageByteArray = bufferedImage2ByteArray(pixel);
        byteArray2BufferedImage(imageByteArray);
        int length = HEAD_LEN + INDEX_LEN + LINE_LEN + imageByteArray.length;
        byte[] headByteArray = int2ByteArray(length);

        byte[] chunkByteArray = new byte[length];
        System.arraycopy(headByteArray, 0, chunkByteArray, 0, HEAD_LEN);
        System.arraycopy(indexByteArray, 0, chunkByteArray, HEAD_LEN, INDEX_LEN);
        System.arraycopy(lineByteArray, 0, chunkByteArray, HEAD_LEN + INDEX_LEN, LINE_LEN);
        System.arraycopy(imageByteArray, 0, chunkByteArray, HEAD_LEN + INDEX_LEN + LINE_LEN, imageByteArray.length);
        return chunkByteArray;
    }

    @Override
    public List<ImageChunk> binary2ObjectHandle(byte[] chunksByteArray) {
        if (chunksByteArray.length < 12) {
            throw new RuntimeException("数据不完整");
        }

        List<ImageChunk> chunks = new ArrayList<>();
        while (chunksByteArray.length > 12) {
            byte[] headByteArray = new byte[HEAD_LEN];
            System.arraycopy(chunksByteArray, 0, headByteArray, 0, HEAD_LEN);
            //获取一个ImageChunk对象长度
            int length = byteArray2Int(headByteArray);
            byte[] chunkByteArray = new byte[length];
            System.arraycopy(chunksByteArray, 0, chunkByteArray, 0, chunkByteArray.length);
            ImageChunk imageChunk = byteArray2ImageChunk(chunkByteArray);
            chunks.add(imageChunk);
            byte[] newChunksByteArray = new byte[chunksByteArray.length - length];
            System.arraycopy(chunksByteArray, length, newChunksByteArray, 0, newChunksByteArray.length);
            chunksByteArray = newChunksByteArray;
        }
        return chunks;
    }

    private ImageChunk byteArray2ImageChunk(byte[] chunkByteArray) {
        byte[] indexByteArray = new byte[INDEX_LEN];
        System.arraycopy(chunkByteArray, HEAD_LEN, indexByteArray, 0, INDEX_LEN);
        int index = byteArray2Int(indexByteArray);
        byte[] lineByteArray = new byte[LINE_LEN];
        System.arraycopy(chunkByteArray, HEAD_LEN + INDEX_LEN, lineByteArray, 0, LINE_LEN);
        int line = byteArray2Int(lineByteArray);
        byte[] imageByteArray = new byte[chunkByteArray.length - (HEAD_LEN + INDEX_LEN + LINE_LEN)];
        System.arraycopy(chunkByteArray, HEAD_LEN + INDEX_LEN + LINE_LEN, imageByteArray, 0, imageByteArray.length);
        BufferedImage bufferedImage = byteArray2BufferedImage(imageByteArray);

        ImageChunk imageChunk = new ImageChunk();
        imageChunk.setIndex(index);
        imageChunk.setLine(line);
        imageChunk.setPixel(bufferedImage);
        return imageChunk;
    }

    /**
     * 获取指定区域的图像块
     *
     * @param image  原始图像
     * @param startX 起始横坐标
     * @param startY 起始纵坐标
     * @param size   像素块大小
     * @return 指定区域图像块
     */
    private BufferedImage getChunk(BufferedImage image, int startX, int startY, int size) {
        int width = Math.min(size, image.getWidth() - startX);
        int height = Math.min(size, image.getHeight() - startY);
        return image.getSubimage(startX, startY, width, height);
    }

    @Override
    public BufferedImage mergeImage(List<ImageChunk> imageChunks) {
        //根据index将列表排序
        imageChunks.sort(Comparator.comparingInt(ImageChunk::getIndex));
        //初始化画布
        BufferedImage canvas = initCanvas(imageChunks);
        Graphics2D graphics = canvas.createGraphics();
        //绘制
        //列索引
        int i = 0;
        int lastLine = 0;
        for (ImageChunk imageChunk : imageChunks) {
            BufferedImage pixel = imageChunk.getPixel();
            int line = imageChunk.getLine();
            if (line != lastLine) {
                //重置列索引
                i = 0;
                //更新最新行
                lastLine = line;
            }
            graphics.drawImage(pixel, chunkSize * i, chunkSize * line, null);
            i++;
        }
        return canvas;
    }

    private BufferedImage initCanvas(List<ImageChunk> imageChunks) {
        //计算画布宽度
        int canvasWidth = imageChunks.stream()
                .filter(chunk -> chunk.getLine() == 0)
                .mapToInt(chunk -> chunk.getPixel().getWidth())
                .sum();
        //计算不同行的像素块
        Map<Integer, BufferedImage> collect = imageChunks.stream().collect(Collectors.toMap(ImageChunk::getLine
                , ImageChunk::getPixel, (existingChunk, newChunk) -> existingChunk));
        //计算画布高度
        int canvasHeight = collect.values().stream().mapToInt(BufferedImage::getHeight).sum();

        return new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public boolean saveImage(BufferedImage image, String fileName) {
        try {
            return ImageIO.write(image, FORMAT_NAME, new File(fileName));
        } catch (IOException e) {
            return false;
        }
    }

    private byte[] int2ByteArray(int num) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((num >> 24) & 0xFF);
        bytes[1] = (byte) ((num >> 16) & 0xFF);
        bytes[2] = (byte) ((num >> 8) & 0xFF);
        bytes[3] = (byte) (num & 0xFF);
        return bytes;
    }

    private int byteArray2Int(byte[] bytes) {
        int num = 0;
        for (int i = 0; i < bytes.length; i++) {
            num += (bytes[i] & 0xFF) << (8 * (3 - i));
        }
        return num;
    }

    private byte[] bufferedImage2ByteArray(BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, FORMAT_NAME, baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    private BufferedImage byteArray2BufferedImage(byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 图像缩放
     *
     * @param originalImage
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        return resizedImage;
    }
}
