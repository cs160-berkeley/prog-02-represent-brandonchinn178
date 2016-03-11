package cs160.brandonchinn178.smartelect;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ZipCode {
    private static List<Integer> zipCodes;
    private static ApiClient mApiClient;

    private static void checkZipCodes(Context context) {
        if (zipCodes == null) {
            zipCodes = new ArrayList<>();
            InputStream stream = null;
            try {
                stream = context.getResources().openRawResource(R.raw.zip_codes);
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;

                while ((line = reader.readLine()) != null) {
                    zipCodes.add(Integer.parseInt(line));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Exception e) {}
                }
            }
        }
    }

    private static void checkClient(Context context) {
        if (mApiClient == null) {
            mApiClient = new ApiClient(context);
        }
    }

    public static boolean isValid(Context context, int zipCode) {
        checkZipCodes(context);
        return zipCodes.contains(zipCode);
    }

    public static int getRandom(Context context) {
        checkZipCodes(context);
        checkClient(context);

        Random random = new Random();
        int total = zipCodes.size();
        int zipCode;

        do {
            int index = random.nextInt(total);
            zipCode = zipCodes.get(index);
        } while (!mApiClient.hasDistricts(zipCode));

        return zipCode;
    }
}
