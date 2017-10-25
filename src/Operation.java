/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author huanwenxu
 */
public class Operation {
    public static byte[] toByte(int n){
        byte[] result;
        result = new byte[4];
        result[0] = (byte) (n / (256*256*256));
        result[1] = (byte) (n / (256*256));
        result[2] = (byte) (n / 256);
        result[3] = (byte) n;
        return result;
    }

    static int upperDiv(int fileSize, int chunkSize) {
        int result;
        result = fileSize/chunkSize;
        if(chunkSize == fileSize) return result*result;
        else return result*(result+1);
    }

    public static int toInt(byte[] array) {
        int val=0, i;
        i=0;
        while (array.length > i) {
            int shift= (3 - i) << 3;
            val = val + ((array[i] & 0x000000FF) << shift);
            i++;
        }
        return val;
    }
}
