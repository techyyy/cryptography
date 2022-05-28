import java.util.Arrays;

public class DESCBC {

    public byte[] process(byte[] message, byte[] key, byte[] iv, Mode operation) {
        int length = message.length;
        int n = (length + 7) / 8 * 8;
        byte[] cipher = new byte[n];
        try {
            if (length == 8) {
                message = Operations.XORBytes(message, iv);
                cipher = processPlainText(message, key, operation);
                return cipher;
            }
            int i = 0;
            int k = 0;
            byte[] feedback = iv;
            while (i < length) {
                byte[] block = new byte[8];
                byte[] result = new byte[8];
                int j = 0;
                for (; j < 8 && i < length; j++, i++) {
                    block[j] = message[i];
                }
                while (j < 8) {
                    block[j++] = 0x20;
                }
                if (operation.equals(Mode.ENCRYPT)) {
                    block = Operations.XORBytes(block, feedback);
                    result = processPlainText(block, key, operation);
                    feedback = Arrays.copyOfRange(result, 0, 8);
                } else if (operation.equals(Mode.DECRYPT)) {
                    result = processPlainText(block, key, operation);
                    result = Operations.XORBytes(result, feedback);
                    feedback = Arrays.copyOfRange(block, 0, 8);
                }
                for (j = 0; j < 8 && k < cipher.length; j++, k++) {
                    cipher[k] = result[j];
                }
            }
            return cipher;
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] processPlainText(byte[] message, byte[] key, Mode operation) {
        byte[] result;
        int blockSize = Constants.IP.length;
        byte[][] subKeys = Operations.getSubKeys(key);
        int noOfRounds = subKeys.length;
        message = Operations.permute(message, Constants.IP);
        byte[] leftHalf = Operations.getBits(message, 0, blockSize / 2);
        byte[] rightHalf = Operations.getBits(message, blockSize / 2, blockSize / 2);
        for (int i = 0; i < noOfRounds; i++) {
            byte[] temp = rightHalf;
            rightHalf = Operations.permute(rightHalf, Constants.E);
            byte[] roundKey = null;
            if (operation.equals(Mode.ENCRYPT)) {
                roundKey = subKeys[i];
            } else if (operation.equals(Mode.DECRYPT)) {
                roundKey = subKeys[noOfRounds - i - 1];
            } else {
                System.exit(0);
            }
            rightHalf = Operations.XOR(rightHalf, roundKey);
            rightHalf = Operations.sBox(rightHalf);
            rightHalf = Operations.permute(rightHalf, Constants.P);
            rightHalf = Operations.XOR(rightHalf, leftHalf);
            leftHalf = temp;
        }
        byte[] concatHalves = Operations.concatenateBytes(rightHalf, blockSize / 2, leftHalf, blockSize / 2);
        result = Operations.permute(concatHalves, Constants.IIP);
        return result;
    }

    public static void main(String[] args) {
        DESCBC des = new DESCBC();

        byte[] key = "12345678".getBytes();
        byte[] iv = "12345678".getBytes();
        byte[] message = "TESTCASE".getBytes();
        byte[] cipher = des.process(message, key, iv, Mode.ENCRYPT);
        byte[] plainText = des.process(cipher, key, iv, Mode.DECRYPT);

        System.out.println("CBC crypt " + Arrays.toString(cipher));
        System.out.println("CBC decrypt " + Arrays.toString(plainText));

    }
}