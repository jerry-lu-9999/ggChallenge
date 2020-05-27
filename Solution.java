import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Solution {
    
    //0 is represented as 0/1, the default denominator is 1
    static class Fraction{
        int numinator;
        int denominator;

        public Fraction(int num, int denom){
            this.numinator = num;
            this.denominator = denom;
        }

        private boolean isZero(){
            if (numinator == 0) {
                return true;
            }
            return false;
        }
        @Override
        public String toString(){
            return String.format(numinator + "/" + denominator);
        }
    }

    public static int[] solution(int[][] m) {
        //1. reorganize the matrix so that first few lines will be absorting states
        List<Integer> absorting = new ArrayList<>();
        for(int i = 0; i < m.length; i++){
            int zeroCount = 0;
            for(int j = 0; j < m[i].length; j++){
                if(m[i][j] == 0) zeroCount++;
            }
            if(zeroCount == m[i].length) absorting.add(i);   //means the whole row is 0, an absorting state
        }
        //2. Create a new matrix with absorting states first
        Fraction[][] newM = new Fraction[m.length][m[0].length];
        for(int i = 0; i < absorting.size(); i++){
            for(int j = 0; j < m[i].length; j++){
                if(i == j){newM[i][j] = new Fraction(1,1);}
                else{newM[i][j] = new Fraction(0,1);}
            }
        }
        //now populate the nonabsorting states
        for(int i = absorting.size(); i < m.length; i++){
            int remainder = absorting.get(0);   //assuming the absorting list are continuous
            int rowSum = 0;
            List<Integer> indexList = new ArrayList<>();    //so that we know which value needs to be normalized
            for(int j = 0; j < m[i].length; j++, remainder++){
                int num = m[i-absorting.size()][ remainder % m.length];
                newM[i][j] = new Fraction(num, 1);
                if(num != 0){ rowSum += num; indexList.add(j);}
            }
            for(int index : indexList){
                newM[i][index].denominator = rowSum;
            }
        }

        //now cut the newM into four parts, GET R and Q
        int nonAbsorting = m.length - absorting.size();
        Fraction[][] R = new Fraction[nonAbsorting][absorting.size()];
        Fraction[][] Q = new Fraction[nonAbsorting][nonAbsorting];
        Fraction[][] F = new Fraction[nonAbsorting][nonAbsorting];//computing I-Q first

        for(int i = 0; i < R.length; i++){  //R.length means the last few lines
            for(int j = 0; j < m[i].length; j++){
                if(j >= absorting.size()){
                    Q[i][j-absorting.size()] = newM[i+absorting.size()][j];
                    F[i][j-absorting.size()] = new Fraction(0,1);
                    if(Q[i][j-absorting.size()].isZero() && i == j-absorting.size()){
                        F[i][j-absorting.size()].numinator = 1;
                        F[i][j-absorting.size()].denominator = 1;
                    }else{
                        F[i][j-absorting.size()].numinator = 0 - Q[i][j-absorting.size()].numinator;
                        F[i][j-absorting.size()].denominator = Q[i][j-absorting.size()].denominator;
                    }
                }else{
                    R[i][j] = (newM[i+absorting.size()][j] == null) ? new Fraction(0,1) : newM[i+absorting.size()][j];
                }
            }
        }

        //computing F = (I-Q)^(-1)
        Fraction[][] inverse = new Fraction[F.length][F[0].length];
        F = inverse(F, inverse);

        //matrix multiplication F*R
        int r1 = F.length;  int c1 = F[0].length;
                            int c2 = R[0].length;
        Fraction[][]FR = matrixMulti(F, R, r1, c1, c2);

        //FINAL STEP: create array and return 
        List<Integer> denoList = new ArrayList<>();
        int[] result = new int[FR[0].length+1]; Arrays.fill(result, -1);
        for(int i = 0; i < FR[0].length; i++){
            if(FR[0][i].isZero()){
                result[i] = 0;
            }else{
                FR[0][i] = reduceFraction(FR[0][i].numinator, FR[0][i].denominator);
                denoList.add(FR[0][i].denominator);
            }
        }
        int commonDeno = lcmForList(denoList);
        result[result.length-1] = commonDeno;
        //System.out.println(commonDeno);
        for(int i = 0; i < result.length-1; i++){
            if(result[i] == -1){    //not prefilled by anything
                result[i] = commonDeno / FR[0][i].denominator * FR[0][i].numinator;
            }
        }

        for(int i = 0; i < result.length; i++){
            System.out.println(result[i] + " ");
        }
        return result;
    }
    
    public static int lcmForList(List<Integer> li){
        int result = li.get(0);
        for(int i = 1; i < li.size(); i++){
            result = lcm(result, li.get(i));
        }
        return result;
    }
    public static Fraction reduceFraction(int a, int b) {
        int d;
        d = gcd(a, b);

        a = a / d;  b = b / d;
        return new Fraction(a, b);
    }

    public static Fraction[][] matrixMulti(Fraction[][] first, Fraction[][] sec, int r1, int c1, int c2) {
        Fraction[][] product = new Fraction[r1][c2];
        for(int i = 0; i < r1; i++){
            for(int j = 0; j < c2; j++){
                for(int k = 0; k < c1; k++){
                    if(product[i][j] == null)   product[i][j] = new Fraction(0,1);
                    product[i][j] = addition(multiply(first[i][k], sec[k][j]), product[i][j]);
                }
            }
        }
        return product;
    }

    public static void adjoint(Fraction A[][], Fraction[][] adj){
        int sign = 1;
        Fraction[][]temp = new Fraction[A.length][A[0].length];

        for(int i = 0; i < A.length; i++){
            for(int j = 0; j < A.length; j++){
                findCofactor(A, temp, i, j, A.length);
                sign = ((i+j) % 2 == 0) ? 1 : -1;
                
                Fraction det = findDet(temp, A.length-1);
                det.numinator = sign * det.numinator;
                adj[j][i] = det;
            }
        }
    }

    public static Fraction findDet(Fraction A[][], int n){
        Fraction D = new Fraction(0, 1);
        if(n == 1){return A[0][0];}
        Fraction[][] temp = new Fraction[A.length][A[0].length];
        int sign = 1;
        //iterate for each element in the first row
        for(int f = 0; f < n; f++){
            findCofactor(A, temp, 0, f, n);
            Fraction fractSign = new Fraction((sign == 1) ? 1 : -1, 1);
            D = addition(multiply(multiply(A[0][f], findDet(temp, n-1)), fractSign), D);
            //alternate sign
            sign = -sign;
        }
        return D;
    }

    public static void findCofactor(Fraction A[][], Fraction temp[][], int p, int q, int n){
        int i = 0, j = 0;
        for(int row = 0; row < n; row++){
            for(int col = 0; col < n; col++){
                if(row != p && col != q){
                    temp[i][j++] = A[row][col];
                    if(j == n-1){
                        j = 0;
                        i++;
                    }
                }
            }
        }
    }

    public static Fraction[][] inverse(Fraction[][]A, Fraction[][] inverse){
        Fraction det = findDet(A, A.length);
        Fraction[][] adj = new Fraction[A.length][A[0].length];
        adjoint(A, adj);

        for(int i = 0; i < A.length; i++){
            for(int j = 0; j < A.length; j++){
                //System.out.println("The adj is " + adj[i][j] + "and the det is " + det);
                inverse[i][j] = division(adj[i][j], det);   
            }
        }
        return inverse;
    }

    public static Fraction multiply(Fraction a, Fraction b){
        if(a.isZero() || b.isZero())  return new Fraction(0, 1);
        Fraction c = new Fraction(a.numinator * b.numinator, a.denominator * b.denominator);
        return c;
    }

    public static Fraction division(Fraction a, Fraction b){
        Fraction bcopy = new Fraction(b.numinator, b.denominator);
        int temp = bcopy.denominator;
        bcopy.denominator = bcopy.numinator;
        bcopy.numinator = temp;

        return multiply(a, bcopy);
    }

    public static int lcm(int num1, int num2){
        if(num1 == 0 || num2 == 0){return 0;}
        int higher = Math.max(num1, num2);
        int lower = Math.min(num1, num2);
        int lcm = higher;
        while(lcm % lower != 0){
            lcm += higher;
        }
        return lcm;
    }
    //greatest common divisor
    public static int gcd(int a, int b){
        if(b == 0)   return a;
        return gcd(b, a % b);
    }

    public static Fraction addition(Fraction a, Fraction b){
        if(a.isZero() || a == null)  return b;
        if(b.isZero() || b == null)  return a;
        int lcm = lcm(a.denominator, b.denominator);
        Fraction c = new Fraction(0, lcm);
        c.numinator = lcm / a.denominator * a.numinator + lcm / b.denominator * b.numinator;
        return c;
    }

    public static void main(String[] args) {
        //  int[][] test = new int[][]{{0,1,0,0,0,1},
        //                             {1,0,0,1,1,0},
        //                             {0,0,0,0,0,0},{0,0,0,0,0,0},{0,0,0,0,0,0},{0,0,0,0,0,0}};
        // int[][]test = new int[][]{
        //     {0,2,1,0,0},
        //     {0,0,0,3,4},
        //     {0,0,0,0,0},
        //     {0,0,0,0,0},
        //     {0,0,0,0,0}
        // };
        int[][]test = new int[][]{
            {1,1,0,1},
            {1,1,0,0},
            {0,0,0,0},
            {0,0,0,0}
        };
        System.out.println(solution(test));
    }
}