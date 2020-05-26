import java.util.ArrayList;
import java.util.List;

import org.graalvm.compiler.lir.LIRInstruction.Temp;

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

        for(int i = 0; i < R.length; i++){
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
                    R[i][j] = newM[i+absorting.size()][j];
                }
            }
        }

        //computing F = (I-Q)^(-1)
        Fraction[][] inverse = new Fraction[F.length][F[0].length];
        F = inverse(F, inverse);
        //matrix multiplication F*R



        for(int i = 0; i < F.length; i++){
            for(int j = 0; j < F[i].length; j++){
                System.out.print(F[i][j] + " ");
            }
            System.out.println();
        }
        return null;
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
            A[0][f].numinator = A[0][f].numinator * sign;
            D = addition(multiply(A[0][f], findDet(temp, n-1)), D);
            //alternate sign
            sign =-sign;
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
                inverse[i][j] = division(adj[i][j], det);
            }
        }
        return inverse;
    }
    public static Fraction multiply(Fraction a, Fraction b){
        Fraction c = new Fraction(a.numinator * b.numinator, a.denominator * b.denominator);
        return c;
    }

    public static Fraction division(Fraction a, Fraction b){
        int temp = b.denominator;
        b.denominator = b.numinator;
        b.numinator = temp;
        return multiply(a, b);
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

    public static Fraction addition(Fraction a, Fraction b){
        int lcm = lcm(a.denominator, b.denominator);
        Fraction c = new Fraction(0, lcm);
        c.numinator = lcm / a.denominator * a.numinator + lcm / b.denominator * b.numinator;
        return c;
    }

    public static void main(String[] args) {
        int[][] test = new int[][]{{0,1,0,0,0,1},
                                   {4,0,0,3,2,0},
                                   {0,0,0,0,0,0},{0,0,0,0,0,0},{0,0,0,0,0,0},{0,0,0,0,0,0}};
        Fraction a = division(new Fraction(2, 3), new Fraction(1, 4));
        System.out.println(a);
        System.out.println(solution(test));
    }
}