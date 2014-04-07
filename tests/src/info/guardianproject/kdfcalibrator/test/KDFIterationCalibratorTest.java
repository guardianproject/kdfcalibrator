
package info.guardianproject.kdfcalibrator.test;

import android.test.AndroidTestCase;
import android.util.Log;

import info.guardianproject.kdfcalibrator.KDFIterationCalibrator;
import info.guardianproject.kdfcalibrator.SystemInformation;

import java.security.GeneralSecurityException;

public class KDFIterationCalibratorTest extends AndroidTestCase {

    public final static String TAG = "KDFIterationCalibratorTest";


    @Override
    public void testAndroidTestCaseSetupProperly() {
        // TODO Auto-generated method stub
        super.testAndroidTestCaseSetupProperly();
    }


    public void testKDFCalibration(){
        try {
            SystemInformation info = new SystemInformation();
            KDFIterationCalibrator kdfc = new KDFIterationCalibrator();
            int method1 = kdfc.calibrateMethod1(1000);
            int method2 = kdfc.calibrateMethod2(1000);
            double recommended = kdfc.timeSingleIteration(64000);
            double current = kdfc.timeSingleIteration(100);
            double halfrecommended = kdfc.timeSingleIteration(32000);
            double method1time = kdfc.timeSingleIteration(method1);
            double method2time = kdfc.timeSingleIteration(method2);

            Log.d(TAG, "method1 result: " + method1);
            Log.d(TAG, "method2 result: " + method2);
            Log.d(TAG, "64k      iters: " + recommended);
            Log.d(TAG, "32k      iters: " + halfrecommended);
            Log.d(TAG, "curr     iters: " + current);
            Log.d(TAG, "method1  iters: " + method1time);
            Log.d(TAG, "method2  iters: " + method2time);

            String cpu = info.procCpuInfo();
            String android = info.androidBuild();
            String version = info.procVersion();

            Log.d(TAG, "====================");
            Log.d(TAG, "android");
            Log.d(TAG, android);

            Log.d(TAG, "====================");
            Log.d(TAG, "cpu");
            Log.d(TAG, cpu);

            Log.d(TAG, "====================");
            Log.d(TAG, "version");
            Log.d(TAG, version);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }
}
