package kr.ac.hansung.remoteDesktop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class VideoOutputStream extends OutputStream {
    private static final int DEFAULT_BUFFER_SIZE = 10 * 1024 * 1024;
    ByteArrayOutputStream byteArrayOutputStream;
    ObjectOutputStream          outputStream;

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public VideoOutputStream(ObjectOutputStream outputStream) {
        this.outputStream     = outputStream;
        byteArrayOutputStream = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
    }

    /**
     * 이 스트림에 담긴 모든 내용을 등록된 소켓으로 전송합니다.
     */
    public void transferAllBytes() {
        try {
            outputStream.writeInt(byteArrayOutputStream.size());
            byteArrayOutputStream.writeTo(outputStream);
//            System.out.printf("server : %d \n", byteArrayOutputStream.size());
            byteArrayOutputStream.flush();
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            byteArrayOutputStream.reset();
        }
    }

    @Override
    public void write(int b) throws IOException {
        byteArrayOutputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
    }

    @Override
    public void flush() throws IOException {
        byteArrayOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        byteArrayOutputStream.close();
    }
}
