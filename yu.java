import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.LinkedList;
import java.util.Queue;

public class yu {

    static int width, height;
    static boolean[][] mask;
    static boolean[][] visited;

    public static void main(String[] args) {

        try {
            String inputPath = "C:\\Users\\user\\Desktop\\新增資料夾\\1.jpg";
            String outputPath = "C:\\Users\\user\\Desktop\\新增資料夾\\tiger_result.png";

            BufferedImage img = ImageIO.read(new File(inputPath));

            if (img == null) {
                System.out.println("讀不到圖片");
                return;
            }

            width = img.getWidth();
            height = img.getHeight();

            mask = new boolean[width][height];
            visited = new boolean[width][height];

            // 找老虎區域
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {

                    int rgb = img.getRGB(x, y);

                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;

                    boolean isOrangeTiger = r > 80 &&
                            g > 35 &&
                            b < 150 &&
                            r > g + 5 &&
                            g >= b - 10;

                    boolean isWhiteTiger = r > 130 &&
                            g > 120 &&
                            b > 90 &&
                            Math.abs(r - g) < 60;

                    boolean isBlackStripe = r < 110 &&
                            g < 105 &&
                            b < 105;

                    boolean isBackground = g > r + 25 ||
                            b > r + 25;

                    if ((isOrangeTiger || isWhiteTiger || isBlackStripe) && !isBackground) {
                        mask[x][y] = true;
                    }
                }
            }

            boolean[][] tigerMask = keepLargestComponent();

            BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            // 黑白輸出，強化老虎背部紋路
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {

                    if (tigerMask[x][y]) {

                        int rgb = img.getRGB(x, y);

                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;

                        int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                        int localAvg = getLocalAverage(img, tigerMask, x, y);

                        // 比附近暗很多，就當成條紋
                        boolean isStripe = gray < localAvg - 18 ||
                                gray < 105;

                        if (isStripe) {
                            output.setRGB(x, y, Color.BLACK.getRGB());
                        } else {
                            output.setRGB(x, y, Color.WHITE.getRGB());
                        }

                    } else {
                        output.setRGB(x, y, Color.BLACK.getRGB());
                    }
                }
            }

            ImageIO.write(output, "png", new File(outputPath));

            System.out.println("完成！");
            System.out.println("老虎背部紋路已加強");
            System.out.println("背景全黑，老虎黑白顯示");
            System.out.println("輸出圖片：" + outputPath);
            System.out.println("時間複雜度：O(width × height)");

        } catch (Exception e) {
            System.out.println("錯誤：" + e.getMessage());
        }
    }

    // 計算附近平均亮度，讓背部較淡的紋路也能變明顯
    public static int getLocalAverage(BufferedImage img, boolean[][] tigerMask, int x, int y) {

        int sum = 0;
        int count = 0;

        for (int dy = -3; dy <= 3; dy++) {
            for (int dx = -3; dx <= 3; dx++) {

                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && nx < width && ny >= 0 && ny < height && tigerMask[nx][ny]) {

                    int rgb = img.getRGB(nx, ny);

                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;

                    int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                    sum += gray;
                    count++;
                }
            }
        }

        if (count == 0) {
            return 128;
        }

        return sum / count;
    }

    public static boolean[][] keepLargestComponent() {

        int[] dx = { 1, -1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };

        int maxSize = 0;
        LinkedList<int[]> bestComponent = new LinkedList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (mask[x][y] && !visited[x][y]) {

                    LinkedList<int[]> component = new LinkedList<>();
                    Queue<int[]> queue = new LinkedList<>();

                    queue.add(new int[] { x, y });
                    visited[x][y] = true;

                    while (!queue.isEmpty()) {

                        int[] p = queue.poll();
                        component.add(p);

                        for (int i = 0; i < 4; i++) {

                            int nx = p[0] + dx[i];
                            int ny = p[1] + dy[i];

                            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {

                                if (mask[nx][ny] && !visited[nx][ny]) {
                                    visited[nx][ny] = true;
                                    queue.add(new int[] { nx, ny });
                                }
                            }
                        }
                    }

                    if (component.size() > maxSize) {
                        maxSize = component.size();
                        bestComponent = component;
                    }
                }
            }
        }

        boolean[][] result = new boolean[width][height];

        for (int[] p : bestComponent) {
            result[p[0]][p[1]] = true;
        }

        return result;
    }
}