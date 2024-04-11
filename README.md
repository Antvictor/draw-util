# draw-util
在指定区域合并图片/文字

## 使用

```java
BufferedImage sourceImage=ImageIO.read(new File(file));
// 合并图片
ImageUtils.mergeImage(327,827,1265,1727,
sourceImage,ImageIO.read(new File(minFile)),ImageAlign.CENTER);
// 合并文字
ImageUtils.mergeWord(1520,900,1520,900,170,
sourceImage,word)
ImageIO.write(sourceImage,"png",new File(file));
```