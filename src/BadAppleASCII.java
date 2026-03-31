import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import java.awt.image.BufferedImage;
import java.awt.*;

public class BadAppleASCII {
    // 밝기에 따른 문자 세트 (검정에서 흰색 순으로)
    private static final String CHARS = " .:-=+*#%@";

    public static void main(String[] args) {
        String videoPath = "bad_apple.mp4.mp4"; // 파일명이 두 번 붙어 있어서 그대로 사용합니다.
        
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();
            
            // 영상 정보 (터미널 크기에 맞춰 축소)
            int targetWidth = 120; // 가로 길이 (문자 개수)
            int targetHeight = 40; // 세로 길이 (문자 줄 수)
            
            // 재생 타이밍 계산을 위한 정보
            long startTime = System.currentTimeMillis();
            double fps = grabber.getFrameRate();
            long frameDurationMs = (long) (1000 / fps);

            System.out.println("준비되었습니다. 재생을 시작합니다...");
            Thread.sleep(2000); // 시작 전 잠시 대기

            while (true) {
                var frame = grabber.grabImage();
                if (frame == null) break;

                BufferedImage bi = converter.convert(frame);
                if (bi == null) continue;

                // 1. 이미지 크기 조절
                BufferedImage resized = resizeImage(bi, targetWidth, targetHeight);
                
                // 2. ASCII 변환 및 출력
                StringBuilder sb = new StringBuilder("\033[H"); // 터미널 커서를 맨 위로 이동
                for (int y = 0; y < resized.getHeight(); y++) {
                    for (int x = 0; x < resized.getWidth(); x++) {
                        int pixel = resized.getRGB(x, y);
                        int gray = getGrayscale(pixel);
                        // 밝기에 따라 CHARS에서 문자 선택
                        int charIdx = (int) (gray * (CHARS.length() - 1) / 255.0);
                        sb.append(CHARS.charAt(charIdx));
                    }
                    sb.append("\n");
                }
                System.out.print(sb.toString());

                // 3. 실제 영상 속도에 맞추기 위해 대기
                long nextFrameTime = startTime + (long) (grabber.getFrameNumber() * frameDurationMs);
                long waitTime = nextFrameTime - System.currentTimeMillis();
                if (waitTime > 0) {
                    Thread.sleep(waitTime);
                }
            }
            
            grabber.stop();
            System.out.println("재생이 끝났습니다!");
        } catch (Exception e) {
            System.err.println("오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 흑백(Grayscale) 계산
    private static int getGrayscale(int rgb) {
        Color c = new Color(rgb);
        // 밝기 계산 (가중치 평균법)
        return (int) (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
    }

    // 이미지 크기 조절 (성능을 위해 SCALE_FAST 사용)
    private static BufferedImage resizeImage(BufferedImage original, int width, int height) {
        Image tmp = original.getScaledInstance(width, height, Image.SCALE_FAST);
        BufferedImage dimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }
}
