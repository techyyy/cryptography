public class Operations {

    public static byte[] XOR(byte[] one, byte[] two) {
        byte[] result = new byte[one.length];
        for (int i = 0; i < one.length; i++) {
            result[i] = (byte) (one[i] ^ two[i]);
        }
        return result;
    }

    public static byte[] permute(byte[] input, int[] mapping) {
        int byteCount = 1 + (mapping.length - 1) / 8;
        byte[] output = new byte[byteCount];
        int pos;

        for (int i = 0; i < mapping.length; i++) {
            pos = mapping[i] - 1;
            int value = getBitFromArray(input, pos);
            setBitInArray(output, i, value);
        }
        return output;
    }

    public static int getBitFromArray(byte[] array, int pos) {
        int value;
        int bytePos = pos / 8;
        int bitPos = pos % 8;
        value = (array[bytePos] >> (8 - (bitPos + 1))) & 0x0001;
        return value;
    }

    public static void setBitInArray(byte[] input, int pos, int value) {
        int bytePos = pos / 8;
        int bitPos = pos % 8;
        byte old = input[bytePos];
        old = (byte) (((0xFF7F >> bitPos) & old) & 0x00FF);
        byte newByte = (byte) ((value << (8 - (bitPos + 1))) | old);
        input[bytePos] = newByte;
    }

    public static byte[] getBits(byte[] input, int startPos, int length) {
        int noOfBytes = (length - 1) / 8 + 1;
        byte[] output = new byte[noOfBytes];
        for (int i = 0; i < length; i++) {
            int value = getBitFromArray(input, startPos + i);
            setBitInArray(output, i, value);
        }
        return output;
    }

    public static byte[] rotateLeft(byte[] input, int step, int length) {
        int noOfBytes = (length - 1) / 8 + 1;
        byte[] output = new byte[noOfBytes];
        for (int i = 0; i < length; i++) {
            int value = getBitFromArray(input, (i + step) % length);
            setBitInArray(output, i, value);
        }
        return output;
    }

    public static byte[] concatenateBytes(byte[] one, int oneLength,
                                          byte[] two, int twoLength) {
        int noOfBytes = (oneLength + twoLength - 1) / 8 + 1;
        byte[] output = new byte[noOfBytes];
        int i = 0, j = 0;
        for (; i < oneLength; i++) {
            int value = getBitFromArray(one, i);
            setBitInArray(output, j, value);
            j++;
        }
        for (i = 0; i < twoLength; i++) {
            int value = getBitFromArray(two, i);
            setBitInArray(output, j, value);
            j++;
        }
        return output;
    }

    public static byte[] XORBytes(byte[] in1, byte[] in2) {
        byte[] out = new byte[in1.length];
        for (int i = 0; i < in1.length; i++) {
            out[i] = (byte) ((in1[i] ^ in2[i]) & 0xff);
        }
        return out;
    }

    public static int[] getSBox(int i) {
        return switch (i) {
            case 0 -> Constants.S1;
            case 1 -> Constants.S2;
            case 2 -> Constants.S3;
            case 3 -> Constants.S4;
            case 4 -> Constants.S5;
            case 5 -> Constants.S6;
            case 6 -> Constants.S7;
            case 7 -> Constants.S8;
            default -> null;
        };
    }

    public static byte[] split(byte[] input, int length) {
        int noOfBytes = (8 * input.length - 1) / length + 1;
        byte[] output = new byte[noOfBytes];
        for (int i = 0; i < noOfBytes; i++) {
            for (int j = 0; j < length; j++) {
                int value = Operations.getBitFromArray(input, length * i + j);
                Operations.setBitInArray(output, 8 * i + j, value);
            }
        }
        return output;
    }

    public static byte[][] getSubKeys(byte[] masterKey) {
        int noOfSubKeys = Constants.SHIFTS.length;
        int keySize = Constants.PC1.length;
        byte[] key = Operations.permute(masterKey, Constants.PC1);
        byte[][] subKeys = new byte[noOfSubKeys][keySize];
        byte[] leftHalf = Operations.getBits(key, 0, keySize / 2);
        byte[] rightHalf = Operations.getBits(key, keySize / 2, keySize / 2);
        for (int i = 0; i < noOfSubKeys; i++) {
            leftHalf = Operations.rotateLeft(leftHalf, Constants.SHIFTS[i], keySize / 2);
            rightHalf = Operations.rotateLeft(rightHalf, Constants.SHIFTS[i], keySize / 2);
            byte[] subKey = Operations.concatenateBytes(leftHalf, keySize / 2, rightHalf, keySize / 2);
            subKeys[i] = Operations.permute(subKey, Constants.PC2);
        }
        return subKeys;
    }

    public static byte[] sBox(byte[] input) {
        input = Operations.split(input, 6);
        byte[] output = new byte[input.length / 2];
        int leftHalf = 0;
        for (int i = 0; i < input.length; i++) {
            byte block = input[i];
            int row = 2 * (block >> 7 & 0x0001) + (block >> 2 & 0x0001);
            int col = block >> 3 & 0x000F;
            int[] selectedSBox = Operations.getSBox(i);
            int rightHalf = selectedSBox[16 * row + col];
            if (i % 2 == 0) {
                leftHalf = rightHalf;
            } else {
                output[i / 2] = (byte) (16 * leftHalf + rightHalf);
                leftHalf = 0;
            }
        }
        return output;
    }
}
