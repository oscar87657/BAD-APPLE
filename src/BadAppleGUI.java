import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BadAppleGUI {
    public static void main(String[] args) {
        String videoPath = "bad_apple.mp4.mp4";

        // 1. GUI 창 설정
        JFrame frame = new JFrame("Bad Apple - GUI Player");
        JLabel label = new JLabel();
        frame.getContentPane().add(label);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();

            // 영상 크기에 맞춰 창 크기 조절
            frame.setSize(grabber.getImageWidth(), grabber.getImageHeight());
            frame.setVisible(true);

            Java2DFrameConverter converter = new Java2DFrameConverter();
            
            long startTime = System.currentTimeMillis();
            double fps = grabber.getFrameRate();
            long frameDurationMs = (long) (1000 / fps);

            while (true) {
                var frameData = grabber.grabImage();
                if (frameData == null) break;

                BufferedImage bi = converter.convert(frameData);
                if (bi == null) continue;

                // 2. 창에 이미지 업데이트
                label.setIcon(new ImageIcon(bi));

                // 3. 타이밍 조절
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
}
