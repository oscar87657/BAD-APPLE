import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BadAppleASCII_GUI {
    // 밝기에 따른 문자 세트 (검정에서 흰색 순으로)
    private static final String CHARS = " .:-=+*#%@";

    public static void main(String[] args) {
        String videoPath = "bad_apple.mp4.mp4";

        // 1. GUI 창 및 텍스트 영역 설정
        JFrame frame = new JFrame("Bad Apple - ASCII GUI Player");
        JTextArea textArea = new JTextArea();
        
        // 폰트 설정 (고정폭 폰트 필수!)
        textArea.setFont(new Font("Monospaced", Font.BOLD, 10));
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.WHITE);
        textArea.setEditable(false);
        
        frame.add(new JScrollPane(textArea));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800); // 적당한 창 크기
        frame.setVisible(true);

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();
            
            // 창 크기에 맞춰 텍스트 해상도 조절
            int targetWidth = 150; // 가로 글자 수 (픽셀 수처럼 작동)
            int targetHeight = 60;  // 세로 줄 수
            
            long startTime = System.currentTimeMillis();
            double fps = grabber.getFrameRate();
            long frameDurationMs = (long) (1000 / fps);

            while (true) {
                var frameData = grabber.grabImage();
                if (frameData == null) break;

                BufferedImage bi = converter.convert(frameData);
                if (bi == null) continue;

                // 2. 이미지 리사이징 및 ASCII 변환
                BufferedImage resized = resizeImage(bi, targetWidth, targetHeight);
                StringBuilder sb = new StringBuilder();
                
                for (int y = 0; y < resized.getHeight(); y++) {
                    for (int x = 0; x < resized.getWidth(); x++) {
                        int pixel = resized.getRGB(x, y);
                        int gray = getGrayscale(pixel);
                        int charIdx = (int) (gray * (CHARS.length() - 1) / 255.0);
                        sb.append(CHARS.charAt(charIdx));
                    }
                    sb.append("\n");
                }

                // 3. 화면 업데이트 (글자 전체 교체)
                textArea.setText(sb.toString());

                // 4. 타이밍 조절
                long nextFrameTime = startTime + (long) (grabber.getFrameNumber() * frameDurationMs);
                long waitTime = nextFrameTime - System.currentTimeMillis();
                if (waitTime > 0) {
                    Thread.sleep(waitTime);
                }
            }

            grabber.stop();
            JOptionPane.showMessageDialog(frame, "재생이 끝났습니다!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getGrayscale(int rgb) {
        Color c = new Color(rgb);
        return (int) (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
    }

    private static BufferedImage resizeImage(BufferedImage original, int width, int height) {
        Image tmp = original.getScaledInstance(width, height, Image.SCALE_FAST);
        BufferedImage dimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }
}
