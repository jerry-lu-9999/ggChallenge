import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Solution {
    public static int solution(int[] l) {
        if(l == null)   return 0;
        List<Integer> al = new ArrayList<>();
        int sum = 0;
        int result = 0;
        for(int a : l) { 
            al.add(a);  
            sum += a;
        }
        if(sum % 3 == 0){
            Arrays.sort(l);
            for(int i = 0; i < l.length; i++){
                result+= l[i] * Math.pow(10, i);
            }
        }else{
            int[] returnArr = find(al, sum);
            if(returnArr == null)   return 0;
            else{
                Arrays.sort(returnArr);
                for(int i = 0; i < returnArr.length; i++){
                    result+= returnArr[i] * Math.pow(10, i);
                }
            }
        }
        return result;
    }

    public static int[] find(List<Integer> li, int sum){
        for(int i = 0; i < li.size();i++){
            Map<Integer, List<Integer>> hs = new HashMap<>();
            List<Integer> sumList = new ArrayList<>();
            int left = 0, right = i+left;
            boolean found = false;
            while(right < li.size()){
                int tempSum = 0;
                List<Integer> indexes = new ArrayList<>();
                for(int j = left; j <= right; j++){
                    tempSum += li.get(j);
                    indexes.add(j);
                }
                if((sum-tempSum) % 3 == 0){
                    found = true;
                    hs.put(tempSum, indexes);
                    sumList.add(tempSum);
                }
                left++;
                right++;
            }
            if(found){
                Collections.sort(sumList);
                List<Integer> toTakeOut = hs.get(sumList.get(0));
                for(int index :toTakeOut){
                    li.remove(index);
                }
                int[] arr = new int[li.size()];
                for(int k = 0; k < arr.length; k++){
                    arr[k] = li.get(k);
                }
                return arr;
            }
        }
        return null;
    }
}





public class Solution {
    public static int solution(String x) {
        if(Integer.parseInt(x) == 1){
            return 1;
        }

        int number = Integer.parseInt(x);
        int steps = 0;
        while(number > 1){
            if(number % 2 == 0){
                number = number/2;
            }else{
                if((number == 3) || ((number+1) & number) > ((number-1)&(number-2))){
                    number--;
                }else{
                    number++;
                }
                int big = number + 1;
                String binbig = Integer.toBinaryString(big);
                System.out.println(binbig);
                int countbig = 0;   int i = binbig.length()-1;  int allCountBig = Integer.bitCount(0);
;                while(binbig.charAt(i) != '1'){
                    i--;
                    countbig++;
                }
                int small = number - 1;
                String binsmall = Integer.toBinaryString(small);
                int countsmall = 0;   int j = binsmall.length()-1;
                while(binsmall.charAt(j) != '1'){
                    j--;
                    countsmall++;
                }
                if(countbig > countsmall){
                    number = big;
                }else{
                    number = small;
                }
            }
            steps++;
        }
        return steps;
    }

    public static void main(String[]args) {
        System.out.println(solution("15"));
    }
}