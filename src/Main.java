import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import javax.swing.filechooser.FileNameExtensionFilter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.Scanner;

// Main class — filename must match (Main.java)
public class Main {

    // Static initializer: load OpenCV native library
    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("OpenCV library loaded successfully!");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load OpenCV native library: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        System.out.println("Please select a video file:");

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        FileNameExtensionFilter videoFilter = new FileNameExtensionFilter(
                "Video files (MP4, AVI, MOV, MKV, WMV, FLV)",
                "mp4", "avi", "mov", "mkv", "wmv", "flv", "mpeg", "mpg"
        );
        fileChooser.setFileFilter(videoFilter);
        fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());

        int result = fileChooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            System.err.println("No file selected.");
            return;
        }
        String inputPath = fileChooser.getSelectedFile().getAbsolutePath();

        // Фиксированные значения количества потоков
        int[] threadCounts = {2, 4, 6, 8, 10};
        String outputPath = inputPath.substring(0, inputPath.lastIndexOf('.')) + "_output.avi";

        for (int numThreads : threadCounts) {
            System.out.println("\n=== Testing with " + numThreads + " threads ===");
            for (int run = 1; run <= 5; run++) {
                System.out.println("--- Run " + run + " ---");
                processVideo(inputPath, outputPath, numThreads);
            }
        }

        System.out.println("\nAll tests completed.");
    }

    public static void processVideo(String inputPath, String outputPath, int numThreads) {
        VideoCapture capture = new VideoCapture(inputPath);
        if (!capture.isOpened()) {
            System.err.println("Failed to open video: " + inputPath);
            return;
        }

        double fps = capture.get(Videoio.CAP_PROP_FPS);
        int frameWidth = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int frameHeight = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        long totalFrames = (long) capture.get(Videoio.CAP_PROP_FRAME_COUNT);

        System.out.println("Processing video: " + totalFrames + " frames, " + fps + " FPS");

        VideoWriter writer = new VideoWriter();
        writer.open(outputPath, VideoWriter.fourcc('M', 'J', 'P', 'G'), fps, new Size(frameWidth, frameHeight), true);

        if (!writer.isOpened()) {
            System.err.println("Failed to create output video file.");
            capture.release();
            return;
        }

        List<Mat> frames = new ArrayList<>();
        Mat frame = new Mat();

        while (capture.read(frame)) {
            if (!frame.empty()) {
                frames.add(frame.clone());
            }
        }
        capture.release();

        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Mat>> futures = new ArrayList<>();

        for (int i = 0; i < frames.size(); i++) {
            final Mat currentFrame = frames.get(i);
            futures.add(executor.submit(() -> processFrame(currentFrame)));
        }

        try {
            for (Future<Mat> future : futures) {
                Mat processed = future.get();
                writer.write(processed);
                processed.release();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            writer.release();
            frames.forEach(Mat::release);
        }

        long endTime = System.currentTimeMillis();
        double processingTimeSec = (endTime - startTime) / 1000.0;
        System.out.println("Completed with " + numThreads + " threads in " + String.format("%.2f", processingTimeSec) + " seconds.");
    }

    private static Mat processFrame(Mat originalFrame) {
        Mat result = originalFrame.clone();
        int rows = originalFrame.rows();
        int cols = originalFrame.cols();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                double[] pixel = originalFrame.get(y, x);
                if (pixel.length < 3) continue;

                double intensity = (pixel[2] + pixel[1] + pixel[0]) / 3.0;

                if (intensity < 64) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            int ny = y + dy;
                            int nx = x + dx;

                            if (ny >= 0 && ny < rows && nx >= 0 && nx < cols) {
                                if (Math.abs(dy) == 1 || Math.abs(dx) == 1) {
                                    result.put(ny, nx, new double[]{0, 0, 255});
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }
}