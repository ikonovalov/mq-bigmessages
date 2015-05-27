package ru.codeunited.wmq;

import java.util.Random;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 27.05.15.
 */
public class Utils {

    public static byte[] generateRandomBytes(int size) {
        byte[] randomSeq = new byte[size];
        new Random().nextBytes(randomSeq);
        return randomSeq;
    }

}
