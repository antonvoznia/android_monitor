package cti.com.androidmonitor.linpack;

import java.util.Random;

import cti.com.androidmonitor.linpack.adapters.AdapterLinpack;

public class CustomLinpack {

    private int sizeMatirx;

    private double[][] matrix;

    AdapterLinpack adapterLinpack;

    public CustomLinpack(int size, AdapterLinpack al) {
        sizeMatirx = size;
        adapterLinpack = al;
    }

    public void generateMatrix() {
        matrix = new double[sizeMatirx][sizeMatirx];
        Random rand = new Random();
        double min = 1, max = Double.MAX_VALUE;
        for (int i = 0; i < sizeMatirx; i++) {
            for (int j = 0; j < sizeMatirx; j++) {
                matrix[i][j] = rand.nextDouble() * (max - min) + min;
            }
        }
    }

    private long getCountOperations() {
        long countExecute2 = (sizeMatirx*sizeMatirx-sizeMatirx)/2;
        long count1 = sizeMatirx-1;
        long count2 = countExecute2*3;
        long count3 = 0;
        for (int i = 0; i < sizeMatirx-1; i++) {
            count3 += (sizeMatirx-1-i)*(sizeMatirx-i);
        }
        count3 = count3*3;
        return count1+count2+count3;
    }

    public String getFlops() {
        double time = (double) stepMatrix()/1000.;
        double flops = (double) getCountOperations()/time;
        String str = "";
        if (flops > 1000000) {
            flops /= 1000000;
            str = String.format("%d MFlops", (long)flops);
        } else if (flops < 1000000) {
            flops/=1000;
            str = String.format("%d KFlops", (long)flops);
        }
        return str;
    }

    public long stepMatrix() {
        long start, end;
        start = System.currentTimeMillis();
        for (int i = 0, n = sizeMatirx - 1; i < n; i++) {
            for (int j = i+1; j < sizeMatirx; j++) {
                double devider = -matrix[j][i]/matrix[i][i];
                for (int ij = i; ij < sizeMatirx; ij++) {
                    matrix[j][ij] = matrix[j][ij]+devider*matrix[i][ij];
                }
            }
            int percent = i*100/sizeMatirx;
            adapterLinpack.setPercent(percent);
        }
        adapterLinpack.setPercent(100);
        end = System.currentTimeMillis();
        return (end-start);
    }
}
