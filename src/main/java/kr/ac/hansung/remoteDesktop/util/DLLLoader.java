package kr.ac.hansung.remoteDesktop.util;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DLLLoader {
    public static void LoadDLL(String libraryName) throws RuntimeException {
        Path temp = null;
        try (InputStream in = DLLLoader.class.getResourceAsStream("/" + libraryName)
        ) {
            temp = temp = Files.createTempFile(null, libraryName);
            FileOutputStream out = new FileOutputStream(temp.toFile());
            if (in == null) {
                throw new IllegalArgumentException(libraryName + " not found");
            }
            // 임시 파일에 DLL을 씁니다.
            byte[] buffer = new byte[1024];
            int readBytes;
            while ((readBytes = in.read(buffer)) != -1) {
                out.write(buffer, 0, readBytes);
            }
            out.close();
            System.load(temp.toString());
        } catch (UnsatisfiedLinkError | IOException e) {
            if (temp == null) throw new RuntimeException("임시파일을 생성할 수 없습니다.");
            else {
                System.out.println(e.getMessage());
            }
        }
    }
}
