package info.guardianproject.kdfcalibrator;

import android.os.SystemClock;
import android.util.Log;

import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.digests.SHA1Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.util.Strings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
* Adapted from Briar Project's briar-core (relicensed with permission)
* Copyright (C) 2013 Sublime Software Ltd
**/
public class KDFIterationCalibrator {
    private static final String TAG = "KDFIterationCalibrator";
    private static final int PBKDF2_SALT_LEN_BYTES   = 16;  // bytes, 128 bits
    private static final int PBKDF2_KEY_LEN_BITS     = 128; // bits
    private static final int PBKDF2_ITER_SAMPLES     = 30;  // number of samples to run for the adaptive iteration calibration
    private static final int PBKDF2_TARGET_MILLIS    = 1000; // the number of milliseconds to target w/ pbkdf2 iterations

    /**
     * The number of iteration samples to perform during the calibration.
     */
    private int mNumberSamples = 30;


    public KDFIterationCalibrator() {
    }

    public KDFIterationCalibrator(int number_samples) {
        mNumberSamples = number_samples;
    }

/**
method 1: from briar project
            30 samples w/ 1 iteration
            30 samples w/ 2 iterations
            alternating among the two, take the median of each group

             estimate the initialisation time and the execution time per
             iteration. then given a target execution time, subtract initialisation time and
             divide by the iteration time to give the iteration count.
*/
    public int calibrateMethod1(int targetMillis) throws GeneralSecurityException {
        List<Long> quickSamples = new ArrayList<Long>(mNumberSamples);
        List<Long> slowSamples = new ArrayList<Long>(mNumberSamples);
        long iterationNanos = 0, initNanos = 0;
        while(iterationNanos <= 0 || initNanos <= 0) {
            // Sample the running time with one iteration and two iterations
            for(int i = 0; i < mNumberSamples; i++) {
                quickSamples.add(sampleRunningTime(1));
                slowSamples.add(sampleRunningTime(2));
            }
            // Calculate the iteration time and the initialization time
            long quickMedian = median(quickSamples);
            long slowMedian = median(slowSamples);
            iterationNanos = slowMedian - quickMedian;
            initNanos = quickMedian - iterationNanos;
        }
        long targetNanos = targetMillis * 1000L * 1000L;
        long iterations = (targetNanos - initNanos) / iterationNanos;
        Log.i(TAG, "method1 chose " + iterations );
        if(iterations < 1) return 1;
        if(iterations > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) iterations;
    }

    /**
     * perform one 10,000 iteration derivation and derive a scaling factor
     * from: https://github.com/joeykrim/TextSecure/blob/41d28230dd74383ce1fdf984730733c900b39edf/src/org/thoughtcrime/securesms/crypto/MasterSecretUtil.java
     */
    public int calibrateMethod2(int targetMillis) throws GeneralSecurityException {
        char[] passphrase = { 'p', 'a', 's', 's', 'w', 'o', 'r', 'd' };
        byte[] salt = generateSalt(PBKDF2_SALT_LEN_BYTES);

        int BASELINE_ITERATION_COUNT = 10000; //baseline starting iteration count

        long startTime = SystemClock.elapsedRealtime();
        pbkdf2_jce(passphrase, salt, BASELINE_ITERATION_COUNT);
        long finishTime = SystemClock.elapsedRealtime();
        int scaledIterationTarget = (int)(((double)BASELINE_ITERATION_COUNT / (double)(finishTime - startTime)) * targetMillis);

        Log.d(TAG, "method2 chose " + scaledIterationTarget + " (took " + (finishTime - startTime) + "ms)");

        return scaledIterationTarget;
    }

    public double timeSingleIteration(int iter_count) throws GeneralSecurityException {
        char[] passphrase = { 'p', 'a', 's', 's', 'w', 'o', 'r', 'd' };
        byte[] salt = generateSalt(PBKDF2_SALT_LEN_BYTES);
        long startTime = SystemClock.elapsedRealtime();
        pbkdf2_jce(passphrase, salt, iter_count);
        long finishTime = SystemClock.elapsedRealtime();
        double elapsed = finishTime - startTime;
        Log.d(TAG, "timeSingleIteration " + iter_count + " in " + elapsed + "ms");
        return elapsed;
    }

    private long sampleRunningTime(int iterations) throws GeneralSecurityException {
        char[] password = { 'p', 'a', 's', 's', 'w', 'o', 'r', 'd' };

        byte[] salt = generateSalt(PBKDF2_SALT_LEN_BYTES);

        long start = System.nanoTime();
        pbkdf2_jce(password, salt, iterations);

        return System.nanoTime() - start;
    }

    public byte[] pbkdf2_jce(char[] password, byte[] salt, int iterations) throws GeneralSecurityException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return factory.generateSecret(new PBEKeySpec(password, salt, iterations, PBKDF2_KEY_LEN_BITS)).getEncoded();
    }

    public byte[] pbkdf2_bouncy(char[] password, byte[] salt, int iterations) throws GeneralSecurityException {
        byte[] utf8 = toUtf8ByteArray(password);
        Digest digest = new SHA1Digest();
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(digest);
        gen.init(utf8, salt, iterations);
        int keyLengthInBits = 128;
        CipherParameters p = gen.generateDerivedParameters(keyLengthInBits);
        return ((KeyParameter) p).getKey();
    }

    private long median(List<Long> list) {
        int size = list.size();
        if(size == 0) throw new IllegalArgumentException();
        Collections.sort(list);
        if(size % 2 == 1) return list.get(size / 2);
        return list.get(size / 2 - 1) + list.get(size / 2) / 2;
    }

    private byte[] toUtf8ByteArray(char[] c) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Strings.toUTF8ByteArray(c, out);
            byte[] utf8 = out.toByteArray();
            // Erase the output stream's buffer
            out.reset();
            out.write(new byte[utf8.length]);
            return utf8;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] generateSalt(int length) throws NoSuchAlgorithmException {
        byte[] salt = new byte[length];
        SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);
        return salt;
    }


}
