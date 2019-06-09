package Learning.DeepQ;

import java.io.Serializable;
import java.util.Random;

public class MLP implements Serializable {
    private double inputHiddenWeights[][];
    private double hiddenOutputWeights[][];
    private double hiddenBias[][];
    private double outputBias[][];

    private double alpha;
    private int batchSize;
    private double cost;

    private Random random;

    public MLP(int inputNr, int hiddenNr, int outputNr, double alpha, int batchSize) {
        inputHiddenWeights = np.random(hiddenNr, inputNr);
        hiddenOutputWeights = np.random(outputNr,hiddenNr);
        hiddenBias = new double[hiddenNr][batchSize];
        outputBias = new double[outputNr][batchSize];

        this.alpha = alpha;
        this.batchSize = batchSize;

        random = new Random();
    }

    public void updateMLP(double in[][], double out[][]){

        double input[][] = np.T(in);
        double output[][] = np.T(out);

        if (input.length!=inputHiddenWeights[0].length){
            System.out.println("Incorrect input-array size input.length: " + input.length + " input[0].length: " + input[0].length + " inputHiddenWeights.length: " + inputHiddenWeights.length + " inputHiddenWeights[0].length: " + inputHiddenWeights[0].length);
            System.out.println("Input matrix");
            np.printMatrix(input);
            System.out.println("Weight Matrix");
            np.printMatrix(inputHiddenWeights);
            return;
        }
        if (input[0].length!=batchSize){
            System.out.println("Incorrect input-batch size");
            return;
        }
        if (hiddenOutputWeights.length!=output.length){
            System.out.println("Incorrect output-array size input.length: " + output.length + " output[0].length: " + output[0].length + " outputHiddenWeights.length: " + hiddenOutputWeights.length + " outputHiddenWeights[0].length: " + hiddenOutputWeights[0].length);
            System.out.println("Weight Matrix");
            np.printMatrix(hiddenOutputWeights);
            System.out.println("Output matrix");
            np.printMatrix(output);
            return;
        }
        if (output[0].length!=batchSize){
            System.out.println("Incorrect output-batch size");
            return;
        }

        //Feed forward
        double Z1[][] = np.add(np.dot(inputHiddenWeights,input),hiddenBias);
        double A1[][] = np.sigmoid(Z1);

        double Z2[][] = np.add(np.dot(hiddenOutputWeights,A1),outputBias);
        double A2[][] = Z2;

        this.cost = np.cross_entropy(batchSize, output, A2);

        // Back Prop
        // Output layer
        double dZ2[][] = np.subtract(A2, output);
        double dW2[][] = np.divide(np.dot(dZ2, np.T(A1)), batchSize);
        double dOutputBias[][] = np.divide(dZ2, batchSize);

        // Hidden layer
        double dZ1[][] = np.multiply(np.dot(np.T(hiddenOutputWeights), dZ2), np.subtract(1.0, np.power(A1, 2)));
        double dW1[][] = np.divide(np.dot(dZ1, np.T(input)), batchSize);
        double dHiddenBias[][] = np.divide(dZ1, batchSize);

        inputHiddenWeights = np.subtract(inputHiddenWeights, np.multiply(alpha, dW1));
        hiddenBias = np.subtract(hiddenBias, np.multiply(alpha, dHiddenBias));

        hiddenOutputWeights = np.subtract(hiddenOutputWeights, np.multiply(alpha, dW2));
        outputBias = np.subtract(outputBias, np.multiply(alpha, dOutputBias));


    }

    public void printOutput(double in[][]){

        double A2[][] = getOutput(in);

        np.printMatrix(A2);
    }

    public double[][] getOutput(double in[][]){
        double input[][] = np.T(in);

        double Z1[][] = np.add(np.dot(inputHiddenWeights,input),hiddenBias);
        double A1[][] = np.sigmoid(Z1);

        double Z2[][] = np.add(np.dot(hiddenOutputWeights,A1),outputBias);
        double A2[][] = Z2; //Making the final output linear instead of sigmoid.

        return A2;
    }

    public double getCost() {
        return cost;
    }

    public void printWheights(){
        System.out.println("Input -> Hidden");
        np.printMatrix(inputHiddenWeights);
        System.out.println("Hidden bias");
        np.printMatrix(hiddenBias);

        System.out.println("Hidden -> Output");
        np.printMatrix(hiddenOutputWeights);
        System.out.println("Output bias");
        np.printMatrix(outputBias);
    }
}

/**
 *
 * @author Deus Jeraldy
 * @Email: deusjeraldy@gmail.com
 */
class np {

    private static Random random;
    private static long seed;

    static {
        seed = System.currentTimeMillis();
        random = new Random(seed);
    }

    /**
     * Sets the seed of the pseudo-random number generator. This method enables
     * you to produce the same sequence of "random" number for each execution of
     * the program. Ordinarily, you should call this method at most once per
     * program.
     *
     * @param s the seed
     */
    public static void setSeed(long s) {
        seed = s;
        random = new Random(seed);
    }

    /**
     * Returns the seed of the pseudo-random number generator.
     *
     * @return the seed
     */
    public static long getSeed() {
        return seed;
    }

    /**
     * Returns a random real number uniformly in [0, 1).
     *
     * @return a random real number uniformly in [0, 1)
     */
    public static double uniform() {
        return random.nextDouble();
    }

    /**
     * Returns a random integer uniformly in [0, n).
     *
     * @param n number of possible integers
     * @return a random integer uniformly between 0 (inclusive) and {@code n}
     * (exclusive)
     * @throws IllegalArgumentException if {@code n <= 0}
     */
    public static int uniform(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("argument must be positive: " + n);
        }
        return random.nextInt(n);
    }

    /**
     * Returns a random long integer uniformly in [0, n).
     *
     * @param n number of possible {@code long} integers
     * @return a random long integer uniformly between 0 (inclusive) and
     * {@code n} (exclusive)
     * @throws IllegalArgumentException if {@code n <= 0}
     */
    public static long uniform(long n) {
        if (n <= 0L) {
            throw new IllegalArgumentException("argument must be positive: " + n);
        }

        long r = random.nextLong();
        long m = n - 1;

        // power of two
        if ((n & m) == 0L) {
            return r & m;
        }

        // reject over-represented candidates
        long u = r >>> 1;
        while (u + m - (r = u % n) < 0L) {
            u = random.nextLong() >>> 1;
        }
        return r;
    }

    /**
     * Returns a random integer uniformly in [a, b).
     *
     * @param a the left endpoint
     * @param b the right endpoint
     * @return a random integer uniformly in [a, b)
     * @throws IllegalArgumentException if {@code b <= a}
     * @throws IllegalArgumentException if {@code b - a >= Integer.MAX_VALUE}
     */
    public static int uniform(int a, int b) {
        if ((b <= a) || ((long) b - a >= Integer.MAX_VALUE)) {
            throw new IllegalArgumentException("invalid range: [" + a + ", " + b + ")");
        }
        return a + uniform(b - a);
    }

    /**
     * Returns a random real number uniformly in [a, b).
     *
     * @param a the left endpoint
     * @param b the right endpoint
     * @return a random real number uniformly in [a, b)
     * @throws IllegalArgumentException unless {@code a < b}
     */
    public static double uniform(double a, double b) {
        if (!(a < b)) {
            throw new IllegalArgumentException("invalid range: [" + a + ", " + b + ")");
        }
        return a + uniform() * (b - a);
    }

    /**
     * @param m
     * @param n
     * @return random m-by-n matrix with values between 0 and 1
     */
    public static double[][] random(int m, int n) {
        double[][] a = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = uniform(0.0, 1.0);
            }
        }
        return a;
    }

    /**
     * Transpose of a matrix
     *
     * @param a matrix
     * @return b = A^T
     */
    public static double[][] T(double[][] a) {
        int m = a.length;
        int n = a[0].length;
        double[][] b = new double[n][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                b[j][i] = a[i][j];
            }
        }
        return b;
    }

    /**
     * @param a matrix
     * @param b matrix
     * @return c = a + b
     */
    public static double[][] add(double[][] a, double[][] b) {
        int m = a.length;
        int n = a[0].length;
        double[][] c = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                c[i][j] = a[i][j] + b[i][j];
            }
        }
        return c;
    }

    /**
     * @param a matrix
     * @param b matrix
     * @return c = a - b
     */
    public static double[][] subtract(double[][] a, double[][] b) {
        int m = a.length;
        int n = a[0].length;
        double[][] c = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                c[i][j] = a[i][j] - b[i][j];
            }
        }
        return c;
    }

    /**
     * Element wise subtraction
     *
     * @param a scaler
     * @param b matrix
     * @return c = a - b
     */
    public static double[][] subtract(double a, double[][] b) {
        int m = b.length;
        int n = b[0].length;
        double[][] c = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                c[i][j] = a - b[i][j];
            }
        }
        return c;
    }

    /**
     * @param a matrix
     * @param b matrix
     * @return c = a * b
     */
    public static double[][] dot(double[][] a, double[][] b) {
        int m1 = a.length;
        int n1 = a[0].length;
        int m2 = b.length;
        int n2 = b[0].length;
        if (n1 != m2) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        double[][] c = new double[m1][n2];
        for (int i = 0; i < m1; i++) {
            for (int j = 0; j < n2; j++) {
                for (int k = 0; k < n1; k++) {
                    c[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return c;
    }

    /**
     * Element wise multiplication
     *
     * @param a matrix
     * @param x matrix
     * @return y = a * x
     */
    public static double[][] multiply(double[][] x, double[][] a) {
        int m = a.length;
        int n = a[0].length;

        if (x.length != m || x[0].length != n) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        double[][] y = new double[m][n];
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                y[j][i] = a[j][i] * x[j][i];
            }
        }
        return y;
    }

    /**
     * Element wise multiplication
     *
     * @param a matrix
     * @param x scaler
     * @return y = a * x
     */
    public static double[][] multiply(double x, double[][] a) {
        int m = a.length;
        int n = a[0].length;

        double[][] y = new double[m][n];
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                y[j][i] = a[j][i] * x;
            }
        }
        return y;
    }

    /**
     * Element wise power
     *
     * @param x matrix
     * @param a scaler
     * @return y
     */
    public static double[][] power(double[][] x, int a) {
        int m = x.length;
        int n = x[0].length;

        double[][] y = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                y[i][j] = Math.pow(x[i][j], a);
            }
        }
        return y;
    }

    /**
     * @param a matrix
     * @return shape of matrix a
     */
    public static String shape(double[][] a) {
        int m = a.length;
        int n = a[0].length;
        String Vshape = "(" + m + "," + n + ")";
        return Vshape;
    }

    /**
     * @param a matrix
     * @return sigmoid of matrix a
     */
    public static double[][] sigmoid(double[][] a) {
        int m = a.length;
        int n = a[0].length;
        double[][] z = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                z[i][j] = (1.0 / (1 + Math.exp(-a[i][j])));
            }
        }
        return z;
    }

    /**
     * Element wise division
     *
     * @param a scaler
     * @param x matrix
     * @return x / a
     */
    public static double[][] divide(double[][] x, int a) {
        int m = x.length;
        int n = x[0].length;

        double[][] z = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                z[i][j] = (x[i][j] / a);
            }
        }
        return z;
    }

    /**
     * Element wise division
     *
     * @param A          matrix
     * @param Y          matrix
     * @param batch_size scaler
     * @return loss
     */
    public static double cross_entropy(int batch_size, double[][] Y, double[][] A) {
        int m = A.length;
        int n = A[0].length;
        double[][] z = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                z[i][j] = -(Y[i][j] * Math.log(A[i][j])) + ((1 - Y[i][j]) * Math.log(1 - A[i][j]));
            }
        }

        double sum = sum(z);
        return sum / (double) batch_size;
    }

    public static double rms(int batch_size, double[][] Y, double[][] A) {
        int m = A.length;
        int n = A[0].length;
        double[][] z = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                z[i][j] = Math.sqrt((Y[i][j]-A[i][j])*(Y[i][j]-A[i][j]));
            }
        }

        double sum = sum(z);
        return sum/(double) batch_size;

        //TODO: FINISH A LINEAR COST FUNCTION
    }

    public static double sum(double[][] X){
        double sum=0;
        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < X[0].length; j++) {
                sum += X[i][j];
            }
        }
        return sum;
    }



    public static double[][] softmax(double[][] z) {
        double[][] zout = new double[z.length][z[0].length];
        double sum = 0.;
        for (int i = 0; i < z.length; i++) {
            for (int j = 0; j < z[0].length; j++) {
                sum += Math.exp(z[i][j]);
            }
        }
        for (int i = 0; i < z.length; i++) {
            for (int j = 0; j < z[0].length; j++) {
                zout[i][j] = Math.exp(z[i][j]) / sum;
            }
        }
        return zout;
    }

    public static void print(String val) {
        System.out.println(val);
    }

    public static void printArray(double array[]){
        for (double y:array){
            System.out.print("" + y);
        }
    }

    public static void printMatrix(double array[][]){
        for (double[] x : array)
        {
            for (double y : x)
            {
                System.out.print(y + " ");
            }
            System.out.println();
        }
    }

}
