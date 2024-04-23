package top.codejourney;


import net.coobird.thumbnailator.Thumbnails;
import org.apache.axis.utils.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.codejourney.contant.ImageAlign;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class ImageUtils {
    static Logger logger = LoggerFactory.getLogger(ImageUtils.class);

    /**
     * 默认样式的文字合成
     *
     * @param initX       初始坐标 X
     * @param initY       初始坐标 Y
     * @param endX        截止坐标 X
     * @param endY        截止坐标 Y
     * @param spacing     行距
     * @param sourceImage 原图
     * @param word        文字
     * @return
     */
    public static BufferedImage mergeWord(int initX, int initY,
                                          int endX, int endY, int spacing,
                                          BufferedImage sourceImage,
                                          String word
    ) {
        return mergeWord(initX, initY, endX, endY, spacing, sourceImage, word, "宋体", 30);
    }

    /**
     * 可以自定义字体的文字合成
     *
     * @param initX       初始坐标 X
     * @param initY       初始坐标 Y
     * @param endX        截止坐标 X
     * @param endY        截止坐标 Y
     * @param spacing     行距
     * @param sourceImage 原图
     * @param word        文字
     * @param fontName    字体名称
     * @param fontSize    字体大小
     * @return
     */
    public static BufferedImage mergeWord(int initX, int initY,
                                          int endX, int endY, int spacing,
                                          BufferedImage sourceImage,
                                          String word, String fontName, Integer fontSize
    ) {

        Font font = new Font(fontName, Font.PLAIN, fontSize);
        Color color = new Color(0, 0, 0);
        return mergeWord(initX, initY, endX, endY, spacing, sourceImage, word,
                font, color, ImageAlign.LEFT, true, true, true);
    }

    /**
     * 在指定区域绘制文字
     *
     * @param initX             初始坐标 X
     * @param initY             初始坐标 Y
     * @param endX              截止坐标 X
     * @param endY              截止坐标 Y
     * @param spacing           行距
     * @param sourceImage       原图
     * @param word              文字
     * @param font              字体
     * @param color             颜色
     * @param align             文字对齐方式
     * @param isLineFeed        是否换行
     * @param isProhibitExceedX 是否禁止超出X
     * @param isProhibitExceedY 是否禁止超出Y
     * @return
     */
    public static BufferedImage mergeWord(int initX, int initY,
                                          int endX, int endY, int spacing,
                                          BufferedImage sourceImage,
                                          String word,
                                          Font font, Color color,
                                          ImageAlign align, boolean isLineFeed,
                                          boolean isProhibitExceedY,
                                          boolean isProhibitExceedX
    ) {
        Graphics2D graphics2D = sourceImage.createGraphics();
        initX = drawXAndY(align, initX, endX, word.length());
//        initY = drawXAndY(align, initY, endY, image.getHeight());
        // 绘制文字
        graphics2D.setFont(font);
        graphics2D.setColor(color);

        // 如果到最右侧文字换行
        int wordWidth = graphics2D.getFontMetrics().stringWidth(word);
        int wordPixel = wordWidth / word.length();
        int end = (endX - initX) / wordPixel;
        if (isLineFeed) {
            while ((graphics2D.getFontMetrics().stringWidth(word)) > 0) {
                end = Math.min(end, word.length());
                // 换行
                String line = word.substring(0, end);
                word = word.substring(end);

                graphics2D.drawString(line, initX, initY);
                initY += graphics2D.getFontMetrics().getHeight() + spacing;
                // 到最下方且剩余高度不足最后一行时不显示
                if (isProhibitExceedY && endY - initY < graphics2D.getFontMetrics().getHeight()) {
                    break;
                }
            }
        } else {
            if (isProhibitExceedX) {
                end = Math.min(end, word.length());
                word = word.substring(0, end);
            }
            graphics2D.drawString(word, initX, initY);
        }

        graphics2D.dispose();
        return sourceImage;
    }

    /**
     * 合并图片到指定区域，只能在[initX, initY] [initX, endY] [endX, initY] [endX, endY] 四个点划定的区域内进行合并
     *
     * @param initX       目标区域起始坐标 initX
     * @param initY       目标区域起始坐标 initY
     * @param endX        目标区域截止坐标 initX
     * @param endY        目标区域截止坐标 initY
     * @param sourceImage 原图
     * @return
     */
    public static BufferedImage mergeImage(int initX, int initY,
                                           int endX, int endY,
                                           BufferedImage sourceImage,
                                           BufferedImage image,
                                           ImageAlign align
    ) {

        Graphics2D graphics2D = sourceImage.createGraphics();

        return drawImage(initX, initY, endX, endY, sourceImage, image, align, graphics2D);
    }

    /**
     * 合并图片到指定区域，只能在[initX, initY] [initX, endY] [endX, initY] [endX, endY] 四个点划定的区域内进行合并
     *
     * @param initX       目标区域起始坐标 initX
     * @param initY       目标区域起始坐标 initY
     * @param endX        目标区域截止坐标 initX
     * @param endY        目标区域截止坐标 initY
     * @param sourceImage 原图
     * @param image       合成图
     * @param align       样式
     * @param isScale     是否等比缩放
     * @return
     */
    public static BufferedImage mergeImage(int initX, int initY,
                                           int endX, int endY,
                                           BufferedImage sourceImage,
                                           BufferedImage image,
                                           ImageAlign align, boolean isScale
    ) {

        Graphics2D graphics2D = sourceImage.createGraphics();

        // 缩放图片
        if (isScale) {
            // 等比缩放
            double scaleFactor = Math.min((double) (endX - initX) / image.getWidth(),
                    (double) (endY - initY) / image.getHeight());
            if (scaleFactor < 1) {
                image = scaleImage(image, scaleFactor);
            }
        }

        return drawImage(initX, initY, endX, endY, sourceImage, image, align, graphics2D);
    }

    /**
     * 绘制图片
     *
     * @param initX       起始坐标 x
     * @param initY       y
     * @param endX        结束坐标 x
     * @param endY        y
     * @param sourceImage 原图
     * @param image       合成图
     * @param align       样式
     * @param graphics2D  工具
     * @return
     */
    private static BufferedImage drawImage(int initX, int initY, int endX, int endY, BufferedImage sourceImage, BufferedImage image, ImageAlign align, Graphics2D graphics2D) {
        initX = drawXAndY(align, initX, endX, image.getWidth());
        // 横向只居中
        if (align == ImageAlign.CENTER) {
            initY = drawXAndY(align, initY, endY, image.getHeight());
        }

        graphics2D.drawImage(image, initX, initY, Math.min(image.getWidth(), endX - initX), Math.min(image.getHeight(), endY - initY), null);
        graphics2D.dispose();
        return sourceImage;
    }

    /**
     * 绘制坐标
     *
     * @param align  类型
     * @param start  开始坐标
     * @param end    截止坐标
     * @param length 长度
     */
    private static int drawXAndY(ImageAlign align, Integer start, Integer end, Integer length) {
        int result;
        switch (align) {
            case CENTER:
                result = length < (end - start) ? (end - start - length) / 2 + start : start;
                break;
            case RIGHT:
                result = length < (end - start) ? end - length : start;
                break;
            case LEFT:
            default:
                result = start;
                break;
        }
        return result;
    }


    /**
     * 等比缩放
     *
     * @param originalImage
     * @param scaleFactor
     * @return
     */
    public static BufferedImage scaleImage(BufferedImage originalImage, double scaleFactor) {
        int targetWidth = (int) (originalImage.getWidth() * scaleFactor);
        int targetHeight = (int) (originalImage.getHeight() * scaleFactor);
        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = scaledImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return scaledImage;
    }

    /**
     * 根据指定大小压缩图片
     *
     * @param imageBytes  源图片字节数组
     * @param desFileSize 指定图片大小，单位kb
     * @return 压缩质量后的图片字节数组
     */
    public static byte[] compressPicForScale(byte[] imageBytes, long desFileSize) {
        if (imageBytes == null || imageBytes.length <= 0) {
            return null;
        }
        if (imageBytes.length < desFileSize * 1024) {
            return imageBytes;
        }
        long srcSize = imageBytes.length;
        double accuracy = getAccuracy(srcSize / 1024);
        try {
            while (imageBytes.length > desFileSize * 1024) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(imageBytes.length);
                Thumbnails.of(inputStream)
                        .scale(accuracy)
                        .outputQuality(accuracy)
                        .toOutputStream(outputStream);
                imageBytes = outputStream.toByteArray();
            }
            logger.info("【图片压缩】| 图片原大小={" + srcSize / 1024 + "}kb | 压缩后大小={" + imageBytes.length / 1024 + "}kb | ");
        } catch (Exception e) {
            logger.error("【图片压缩】msg=图片压缩失败!" + e);
        }
        return imageBytes;
    }

    /**
     * 自动调节精度(经验数值)
     *
     * @param size 源图片大小
     * @return 图片压缩质量比
     */
    private static double getAccuracy(long size) {
        double accuracy;
        if (size < 900) {
            accuracy = 0.85;
        } else if (size < 2047) {
            accuracy = 0.6;
        } else if (size < 3275) {
            accuracy = 0.44;
        } else {
            accuracy = 0.4;
        }
        return accuracy;
    }


}
