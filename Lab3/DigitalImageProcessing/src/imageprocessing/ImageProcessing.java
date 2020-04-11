package imageprocessing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import static java.awt.image.ConvolveOp.EDGE_NO_OP;
import java.awt.image.Kernel;
import static java.lang.Math.*;

public class ImageProcessing {

    public static BufferedImage adaptiveThresholding(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        double[][] means = new double[width][height];
        double[][] brightness = new double[width][height];
        BufferedImage resultImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
      
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double z = 0;
                double count = pow(19, 2);
                for (int a = -9; a <= 9; a++) {
                    for (int b = -9; b <= 9; b++) {
                        if(a + i >= 0 && a + i < width){
                            if(b + j >= 0 && b + j < height){
                                Color color = new Color(originalImage.getRGB(i + a, j + b));
                                double luminance = color.getRed() * 0.2126 + color.getGreen() * 0.7152 + color.getBlue() * 0.0722;
                                z += luminance;
                                brightness[i + a][j + b] = luminance;
                            }
                            else{
                                count--;
                            }
                        }
                        else{
                            count--;
                        }
                    }
                }
                z /= count;
                means[i][j] = z;
            }
        }
        
        for (int i = 9; i < width - 9; i++) {
            for (int j = 9; j < height - 9; j++) {
                double min = 255;
                double max = 0;
                for (int a = -9; a <= 9; a++) {
                    for (int b = -9; b <= 9; b++) {
                        double luminance = brightness[i + a][j + b];
                        min = min(min, luminance);
                        max = max(max, luminance);
                    }
                }
                double z = means[i][j];
                double dMin = abs(min - z);
                double dMax = abs(max - z);
                double threshold;
                if (dMax > dMin) {
                    threshold = 1.0 / 3 * (2.0 * min + 1.0 * z) / 3;
                } 
                else {
                    threshold = 1.0 / 3 * (1.0 * min + 2.0 * z) / 3;
                }
                boolean isBlack = true;
                for (int a = -9; a <= 9; a++) {
                    for (int b = -9; b <= 9; b++) {
                        if (!(a == 0 && b == 0)) {
                            if (abs(means[i + a][j + b] - brightness[i][j]) <= threshold) {
                                isBlack = false;
                                break;
                            }
                        }
                    }
                }
                if (isBlack) {
                    resultImage.setRGB(i, j, Color.BLACK.getRGB());
                } 
                else {
                    resultImage.setRGB(i, j, Color.WHITE.getRGB());
                }
            }         
        }
        return resultImage;
    }
    
    public static BufferedImage highPassFilter(int filterNum, BufferedImage originalImage){
        BufferedImage resultImage = null;
        switch(filterNum){
            case 0:
                resultImage = filtration(originalImage, new float[] {-1, -1, -1, -1, 9, -1, -1, -1, -1});
                break;
            case 1:
                resultImage = filtration(originalImage, new float[] {1, -2, 1, -2, 5, -2, 1, -2, 1});
                break;  
            case 2:
                resultImage = filtration(originalImage, new float[] {0, -1, 0, -1, 5, -1, 0, -1, 0});
                break;  
            case 3:
                resultImage = filtration(originalImage, new float[] {0, 0, -1, 0, 0, 0, -1, -2, -1, 0, -1, -2, 17, -2, -1, 0, -1, -2, -1, 0, 0, 0, -1, 0, 0}); 
                break;      
        }
        return resultImage;
    }
    
     public static BufferedImage localThreshHoldFilter(int type, BufferedImage originalImage) {
        if (type == 0) {
            BufferedImage resultImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
            int width  = originalImage.getWidth();
            int height = originalImage.getHeight();
         
            for (int i = 5; i < width - 5; i++) {
                for (int j = 5; j < height - 5; j++) {
                    double mean  = 0;
                    double[][] neighbourWindowLuminances = new double[11][11];
                    for (int a = -5; a <= 5; a++) {
                        for (int b = -5; b <= 5; b++) {
                            double luminance = getRGBLuminance(originalImage.getRGB(i + a, j + b));
                            mean += luminance;
                            neighbourWindowLuminances[a + 5][b + 5] = luminance;
                        }
                    }
                    mean /= pow(11, 2);
                    double standardDeviation = 0;
                    for (int a = 0; a < 11; a++) {
                        for (int b = 0; b < 11; b++) {
                            standardDeviation += pow(neighbourWindowLuminances[a][b] - mean, 2);
                        }
                    }
                    standardDeviation = sqrt(standardDeviation / (11 * 11));
               
                    if(neighbourWindowLuminances[5][5] > mean + -0.2 * standardDeviation){
                        resultImage.setRGB(i, j, Color.WHITE.getRGB());
                    }
                    else{
                        resultImage.setRGB(i, j, Color.BLACK.getRGB());
                    }
                }           
            }
            return resultImage;
        }
        BufferedImage resultImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        for (int i = 5; i < width - 5; i++) {
            for (int j = 5; j < height - 5; j++) {
                double min = 255;
                double max = 0;
                for (int a = -5; a <= 5; a++) {
                    for (int b = -5; b <= 5; b++) {
                        double luminance = getRGBLuminance(originalImage.getRGB(i + a, j + b));
                        min = min(min, luminance);
                        max = max(max, luminance);
                    }
                }
                double threshold = (min + max) / 2;
                if (getRGBLuminance(originalImage.getRGB(i, j))>= threshold) {
                    resultImage.setRGB(i, j, Color.WHITE.getRGB());
                } 
                else {
                    resultImage.setRGB(i, j, Color.BLACK.getRGB());
                }
            }
        }
        return resultImage;
    }
    
    private static BufferedImage filtration(BufferedImage original, float[] filter){
        int length = (int)Math.sqrt(filter.length);
        BufferedImageOp conOp = new ConvolveOp(new Kernel(length, length, filter), EDGE_NO_OP, null);
        return conOp.filter(original, null);
    }
    
    private static double getRGBLuminance(int rgb) {
        Color color = new Color(rgb);
        return (color.getRed() * 0.2126 + color.getGreen() * 0.7152 + color.getBlue() * 0.0722);
    }
}