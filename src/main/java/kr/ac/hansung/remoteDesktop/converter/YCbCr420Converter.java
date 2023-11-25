package kr.ac.hansung.remoteDesktop.converter;

import org.xerial.snappy.Snappy;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class YCbCr420Converter {

    //    static BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
    static BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_3BYTE_BGR);

    public static BufferedImage convert(byte[] abgrData) {
        int width  = 1920;
        int height = 1080;

        int    frameSize  = width * height;
        int    chromaSize = frameSize / 4;
        byte[] yCbCr      = new byte[frameSize + 2 * chromaSize]; // Y + Cb + Cr

        {
            int yIndex  = 0;
            int cbIndex = frameSize;
            int crIndex = frameSize + chromaSize;
//            BufferedImage.TYPE_4BYTE_ABGR

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = (y * width + x) * 4;
                    int a     = abgrData[index] & 0xff;
                    int b     = abgrData[index + 1] & 0xff;
                    int g     = abgrData[index + 2] & 0xff;
                    int r     = abgrData[index + 3] & 0xff;

                    // ABGR to YCbCr conversion
                    int Y  = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                    int Cb = (int) (128 - 0.168736 * r - 0.331264 * g + 0.5 * b);
                    int Cr = (int) (128 + 0.5 * r - 0.418688 * g - 0.081312 * b);

                    yCbCr[yIndex++] = (byte) (Y < 0 ? 0 : (Math.min(Y, 255)));

                    // 4:2:0 subsampling
                    if (y % 2 == 0 && x % 2 == 0) {
                        yCbCr[cbIndex++] = (byte) (Cb < 0 ? 0 : (Math.min(Cb, 255)));
                        yCbCr[crIndex++] = (byte) (Cr < 0 ? 0 : (Math.min(Cr, 255)));
                    }
                }
            }
        }

//        var      dataBuffer      = ((DataBufferByte) (image.getRaster().getDataBuffer()));
//        Deflater deflater = new Deflater();
//        deflater.setLevel(9);
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        DeflaterOutputStream  deflaterOutputStream  = new DeflaterOutputStream(byteArrayOutputStream, deflater);
////        deflaterOutputStream.write(yCbCr);
//        deflaterOutputStream.write(dataBuffer.getData());
//        deflaterOutputStream.flush();
//        deflaterOutputStream.close();

////        // 압축된 데이터를 BufferedImage에 저장합니다.
//        byte[] compressedData = byteArrayOutputStream.toByteArray();
////        System.out.printf("%d kb\n", compressedData.length / 1000);
//        System.arraycopy(compressedData, 0, dataBuffer.getData(), 0, compressedData.length);
        var dataBuffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                int yIndex = y * width + x;
//                int uvIndex = (y / 2) * (width / 2) + (x / 2);
//
//                int Y = yCbCr[yIndex] & 0xFF;
//                int Cb = yCbCr[width * height + uvIndex] & 0xFF;
//                int Cr = yCbCr[width * height + width * height / 4 + uvIndex] & 0xFF;
//
//                int R = Math.max(0, Math.min(255, Y + (int)(1.402 * (Cr - 128))));
//                int G = Math.max(0, Math.min(255, Y - (int)(0.344136 * (Cb - 128)) - (int)(0.714136 * (Cr - 128))));
//                int B = Math.max(0, Math.min(255, Y + (int)(1.772 * (Cb - 128))));
//
//                // 배열에 RGB 값 직접 설정
//                dataBuffer[y * width + x] = (byte) ((R << 16) | (G << 8) | B);
//            }
//        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int yIndex  = y * width + x;
                int uvIndex = (y / 2) * (width / 2) + (x / 2);

                int Y  = yCbCr[yIndex] & 0xFF;
                int Cb = yCbCr[width * height + uvIndex] & 0xFF;
                int Cr = yCbCr[width * height + width * height / 4 + uvIndex] & 0xFF;

                int R = Math.max(0, Math.min(255, Y + (int) (1.402 * (Cr - 128))));
                int G = Math.max(0, Math.min(255, Y - (int) (0.344136 * (Cb - 128)) - (int) (0.714136 * (Cr - 128))));
                int B = Math.max(0, Math.min(255, Y + (int) (1.772 * (Cb - 128))));

                int bgrIndex = y * width * 3 + x * 3;
                dataBuffer[bgrIndex]     = (byte) B;
                dataBuffer[bgrIndex + 1] = (byte) G;
                dataBuffer[bgrIndex + 2] = (byte) R;
            }
        }
//         BufferedImage를 생성합니다.

        // JPEG 압축을 적용합니다.
//        var      dataBuffer      = ((DataBufferByte) (image.getRaster().getDataBuffer()));
        Deflater deflater = new Deflater();
        deflater.setLevel(3);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream  deflaterOutputStream  = new DeflaterOutputStream(byteArrayOutputStream, deflater);
////        deflaterOutputStream.write(yCbCr);
        try {
            deflaterOutputStream.write(dataBuffer);
            deflaterOutputStream.flush();
            deflaterOutputStream.close();
            byte[] compressedData = byteArrayOutputStream.toByteArray();
            System.arraycopy(compressedData, 0, dataBuffer, 0, compressedData.length);
            System.out.printf("%d kb\n", compressedData.length / 1000);
        } catch (IOException e) {
        }
//
//        // 압축된 데이터를 BufferedImage에 저장합니다.

        return image;
    }

    public static void convertAndStore(byte[] abgrData) {
        int width  = 1920;
        int height = 1080;

        int    frameSize  = width * height;
        int    chromaSize = frameSize / 4;
        byte[] yCbCr      = new byte[frameSize + 2 * chromaSize]; // Y + Cb + Cr

        {
            int yIndex  = 0;
            int cbIndex = frameSize;
            int crIndex = frameSize + chromaSize;
//            BufferedImage.TYPE_4BYTE_ABGR

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = (y * width + x) * 4;
                    int a     = abgrData[index] & 0xff;
                    int b     = abgrData[index + 1] & 0xff;
                    int g     = abgrData[index + 2] & 0xff;
                    int r     = abgrData[index + 3] & 0xff;

                    // ABGR to YCbCr conversion
                    int Y  = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                    int Cb = (int) (128 - 0.168736 * r - 0.331264 * g + 0.5 * b);
                    int Cr = (int) (128 + 0.5 * r - 0.418688 * g - 0.081312 * b);

                    yCbCr[yIndex++] = (byte) (Y < 0 ? 0 : (Math.min(Y, 255)));

                    // 4:2:0 subsampling
                    if (y % 2 == 0 && x % 2 == 0) {
                        yCbCr[cbIndex++] = (byte) (Cb < 0 ? 0 : (Math.min(Cb, 255)));
                        yCbCr[crIndex++] = (byte) (Cr < 0 ? 0 : (Math.min(Cr, 255)));
                    }
                }
            }
        }

        try (FileOutputStream stream = new FileOutputStream(new File("examplebytes"));) {
            stream.write(yCbCr);
            stream.flush();
            System.out.printf("%d kb\n", yCbCr.length / 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        var      dataBuffer      = ((DataBufferByte) (image.getRaster().getDataBuffer()));
//        Deflater deflater = new Deflater();
//        deflater.setLevel(9);
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        DeflaterOutputStream  deflaterOutputStream  = new DeflaterOutputStream(byteArrayOutputStream, deflater);
////        deflaterOutputStream.write(yCbCr);
//        deflaterOutputStream.write(dataBuffer.getData());
//        deflaterOutputStream.flush();
//        deflaterOutputStream.close();

////        // 압축된 데이터를 BufferedImage에 저장합니다.
//        byte[] compressedData = byteArrayOutputStream.toByteArray();
////        System.out.printf("%d kb\n", compressedData.length / 1000);
//        System.arraycopy(compressedData, 0, dataBuffer.getData(), 0, compressedData.length);

//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                int yIndex = y * width + x;
//                int uvIndex = (y / 2) * (width / 2) + (x / 2);
//
//                int Y = yCbCr[yIndex] & 0xFF;
//                int Cb = yCbCr[width * height + uvIndex] & 0xFF;
//                int Cr = yCbCr[width * height + width * height / 4 + uvIndex] & 0xFF;
//
//                int R = Math.max(0, Math.min(255, Y + (int)(1.402 * (Cr - 128))));
//                int G = Math.max(0, Math.min(255, Y - (int)(0.344136 * (Cb - 128)) - (int)(0.714136 * (Cr - 128))));
//                int B = Math.max(0, Math.min(255, Y + (int)(1.772 * (Cb - 128))));
//
//                // 배열에 RGB 값 직접 설정
//                dataBuffer[y * width + x] = (byte) ((R << 16) | (G << 8) | B);
//            }
//        }
//
//        // 압축된 데이터를 BufferedImage에 저장합니다.

    }

    public static BufferedImage convertTest(byte[] abgrData) {
        int width  = 1920;
        int height = 1080;

        int    frameSize  = width * height;
        int    chromaSize = frameSize / 4;
        byte[] yCbCr      = new byte[frameSize + 2 * chromaSize]; // Y + Cb + Cr

        {
            int yIndex  = 0;
            int cbIndex = frameSize;
            int crIndex = frameSize + chromaSize;
//            BufferedImage.TYPE_4BYTE_ABGR

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = (y * width + x) * 4;
                    int a     = abgrData[index] & 0xff;
                    int b     = abgrData[index + 1] & 0xff;
                    int g     = abgrData[index + 2] & 0xff;
                    int r     = abgrData[index + 3] & 0xff;

                    // ABGR to YCbCr conversion
                    int Y  = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                    int Cb = (int) (128 - 0.168736 * r - 0.331264 * g + 0.5 * b);
                    int Cr = (int) (128 + 0.5 * r - 0.418688 * g - 0.081312 * b);

                    yCbCr[yIndex++] = (byte) (Y < 0 ? 0 : (Math.min(Y, 255)));

                    // 4:2:0 subsampling
                    if (y % 2 == 0 && x % 2 == 0) {
                        yCbCr[cbIndex++] = (byte) (Cb < 0 ? 0 : (Math.min(Cb, 255)));
                        yCbCr[crIndex++] = (byte) (Cr < 0 ? 0 : (Math.min(Cr, 255)));
                    }
                }
            }
        }
        try {
            long start        = System.nanoTime();
            var  compressed   = Snappy.compress(yCbCr);
            var  uncompressed = Snappy.uncompress(compressed);
            yCbCr = uncompressed;
            System.out.printf("snappy : %d ms -> %d mb\n", (System.nanoTime() - start) / 1_000_000, (uncompressed.length / 1024 / 1024));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        var      dataBuffer      = ((DataBufferByte) (image.getRaster().getDataBuffer()));
//        Deflater deflater = new Deflater();
//        deflater.setLevel(9);
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        DeflaterOutputStream  deflaterOutputStream  = new DeflaterOutputStream(byteArrayOutputStream, deflater);
////        deflaterOutputStream.write(yCbCr);
//        deflaterOutputStream.write(dataBuffer.getData());
//        deflaterOutputStream.flush();
//        deflaterOutputStream.close();

////        // 압축된 데이터를 BufferedImage에 저장합니다.
//        byte[] compressedData = byteArrayOutputStream.toByteArray();
////        System.out.printf("%d kb\n", compressedData.length / 1000);
//        System.arraycopy(compressedData, 0, dataBuffer.getData(), 0, compressedData.length);
        var dataBuffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                int yIndex = y * width + x;
//                int uvIndex = (y / 2) * (width / 2) + (x / 2);
//
//                int Y = yCbCr[yIndex] & 0xFF;
//                int Cb = yCbCr[width * height + uvIndex] & 0xFF;
//                int Cr = yCbCr[width * height + width * height / 4 + uvIndex] & 0xFF;
//
//                int R = Math.max(0, Math.min(255, Y + (int)(1.402 * (Cr - 128))));
//                int G = Math.max(0, Math.min(255, Y - (int)(0.344136 * (Cb - 128)) - (int)(0.714136 * (Cr - 128))));
//                int B = Math.max(0, Math.min(255, Y + (int)(1.772 * (Cb - 128))));
//
//                // 배열에 RGB 값 직접 설정
//                dataBuffer[y * width + x] = (byte) ((R << 16) | (G << 8) | B);
//            }
//        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int yIndex  = y * width + x;
                int uvIndex = (y / 2) * (width / 2) + (x / 2);

                int Y  = yCbCr[yIndex] & 0xFF;
                int Cb = yCbCr[width * height + uvIndex] & 0xFF;
                int Cr = yCbCr[width * height + width * height / 4 + uvIndex] & 0xFF;

                int R = Math.max(0, Math.min(255, Y + (int) (1.402 * (Cr - 128))));
                int G = Math.max(0, Math.min(255, Y - (int) (0.344136 * (Cb - 128)) - (int) (0.714136 * (Cr - 128))));
                int B = Math.max(0, Math.min(255, Y + (int) (1.772 * (Cb - 128))));

                int bgrIndex = y * width * 3 + x * 3;
                dataBuffer[bgrIndex]     = (byte) B;
                dataBuffer[bgrIndex + 1] = (byte) G;
                dataBuffer[bgrIndex + 2] = (byte) R;
            }
        }
//
//        // 압축된 데이터를 BufferedImage에 저장합니다.

        return image;
    }
}
