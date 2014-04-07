package info.guardianproject.kdfcalibrator;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

public class SystemInformation {

    private final static String TAG = "KDFIterationCalibrator";

    public SystemInformation() {}

    public String procCpuInfo() {
        return cat("/proc/cpuinfo");
    }

    public String procVersion() {
        return cat("/proc/version");
    }

    public String procMeminfo() {
        return cat("/proc/meminfo");
    }

    public String javaSystemInfo()
    {
        StringBuffer SYSinfoBuffer = new StringBuffer();

        getProperty("os.name", "os.name", SYSinfoBuffer);
        getProperty("os.version", "os.version", SYSinfoBuffer);

        getProperty("java.vendor.url", "java.vendor.url", SYSinfoBuffer);
        getProperty("java.version", "java.version", SYSinfoBuffer);
        getProperty("java.class.path", "java.class.path", SYSinfoBuffer);
        getProperty("java.class.version", "java.class.version", SYSinfoBuffer);
        getProperty("java.vendor", "java.vendor", SYSinfoBuffer);
        getProperty("java.home", "java.home", SYSinfoBuffer);

        getProperty("user.name", "user.name", SYSinfoBuffer);
        getProperty("user.home", "user.home", SYSinfoBuffer);
        getProperty("user.dir", "user.dir", SYSinfoBuffer);

        return SYSinfoBuffer.toString();
    }

    public String androidBuild() {
        StringBuffer buildInfoBuf = new StringBuffer();
        try {
            Field[] fields = android.os.Build.class.getDeclaredFields();

            for(Field field : fields) {
                if( field.getType() == java.lang.String.class ) {
                    String value = (String) field.get(null);
                    buildInfoBuf.append(field.getName())
                                .append(" : ")
                                .append(value)
                                .append("\n");
                }

            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return buildInfoBuf.toString();
    }


    private void getProperty(String desc, String property, StringBuffer buffer)
    {
        buffer.append(desc);
        buffer.append(" : ");
        buffer.append(System.getProperty(property));
        buffer.append("\n");
    }

    private String cat(String path) {
        StringBuffer sb = new StringBuffer();
        File f = new File(path);
        if (f.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, path+ " doesn't exist");
        }
     return sb.toString();
    }
}

