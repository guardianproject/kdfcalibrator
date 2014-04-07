
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


    public void testSystemInformation() {
        SystemInformation info = new SystemInformation();
        String cpu = info.procCpuInfo();
        String android = info.androidBuild();
        String version = info.procVersion();

        String sysinfoTag = TAG+"-sysinfo";
        Log.d(sysinfoTag+"-sysinfo", "====================BEGIN-SYSINFO====================");
        Log.d(sysinfoTag, "android");
        Log.d(sysinfoTag, android);
        Log.d(sysinfoTag, "====================");
        Log.d(sysinfoTag, "cpu");
        Log.d(sysinfoTag, cpu);
        Log.d(sysinfoTag, "====================");
        Log.d(sysinfoTag, "version");
        Log.d(sysinfoTag, version);
        Log.d(sysinfoTag, "====================END-SYSINFO====================");
    }


    public void testKDFCalibration(){
        try {

            KDFIterationCalibrator kdfc = new KDFIterationCalibrator();
            int method1 = kdfc.calibrateMethod1(1000);
            int method2 = kdfc.calibrateMethod2(1000);
            double recommended = kdfc.timeSingleIteration(64000);
            double current = kdfc.timeSingleIteration(100);
            double halfrecommended = kdfc.timeSingleIteration(32000);
            double method1time = kdfc.timeSingleIteration(method1);
            double method2time = kdfc.timeSingleIteration(method2);

            String calcTag = TAG+"-calc";
            Log.d(calcTag, "====================BEGIN-CALC====================");
            Log.d(calcTag, "method1 result: " + method1);
            Log.d(calcTag, "method2 result: " + method2);
            Log.d(calcTag, "64k      iters: " + recommended);
            Log.d(calcTag, "32k      iters: " + halfrecommended);
            Log.d(calcTag, "curr     iters: " + current);
            Log.d(calcTag, "method1  iters: " + method1time);
            Log.d(calcTag, "method2  iters: " + method2time);
            Log.d(calcTag, "====================END-CALC====================");


        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            fail("Security Exception");
        }
    }
}
