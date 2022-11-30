package obdii.starter.automotive.iot.ibm.com.iot4a_obdii;

import android.os.Environment;
import android.support.design.widget.Snackbar;

import com.google.android.gms.games.event.EventEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CSVOperation {
    private static final String FILE_FOLDER =
            Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "CSVData";
    private static List<List<String>> data;
    private static String fileName;
    private static StringBuilder sb;

    private static void createFolder() {
        File fileDir = new File(FILE_FOLDER);
        boolean hasDir = fileDir.exists();
        if (!hasDir) {
            fileDir.mkdirs();// 这里创建的是目录
        }
    }

    public static void writeDataIntoCSV2(List<List<String>> dataList, String filename) {
        createFolder();
        File eFile = new File(FILE_FOLDER + File.separator + filename + ".csv");
        if (!eFile.exists()) {
            try {
                boolean newFile = eFile.createNewFile();
                File file = new File(FILE_FOLDER + File.separator + filename + ".csv");
                BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
                bw.write("TripId" + "," + "Heading" + "," + "GPS(Longtitude)" + "," + "GPS(Latitude)" + "," + "RPM" + "," + "VehicleSpeed" + ","
                        + "EngineOil" + "," + "EngineCoolant" + "," + "fuelLevel" + ","
                        + "PhoneAccX" + "," + "PhoneAccY" + "," + "PhoneAccZ" + ","
                        + "PhoneGravityX" + "," + "PhoneGravityX" + "," + "PhoneGravityZ" + ","
                        + "PhoneOrientationX" + "," + "PhoneOrientationY" + "," + "PhoneOrientationZ" + ","
                        + "PhoneGyroscopeX" + "," + "PhoneGyroscopeY" + "," + "PhoneGyroscopeZ" + ","
                        + "PhoneRotationVectorX" + "," + "PhoneRotationVectorY" + "," + "PhoneRotationVectorZ" + ","
                        + "EarthAccX" + "," +  "EarthAccY" + "," + "EarthAccZ" + ","
                        + "MagneticX" + "," +  "MagneticY" + "," +  "MagneticZ" + "," +"Pressure" + ","
                        + "phoneLinearAccX" + "," + "phoneLinearAccY" + "," + "phoneLinearAccZ" + ","
                        + "phoneRelativeRotationDegreeXToEarth" + "," + "phoneRelativeRotationDegreeYToEarth" + ","+ "phoneRelativeRotationDegreeZToEarth" + ","
                        + "earthLinearAccX" + "," + "earthLinearAccY" + ","+ "earthLinearAccZ" + ","
                        + "earthGyroscopeX" + "," + "earthGyroscopeY" + ","+ "earthGyroscopeZ" + ","
                        + "TimeStamp"+","+"Frequence");
                bw.newLine();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {
                BufferedWriter out = null;
                out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(eFile, true)));
                String rowData = "";
                for (int i = 0; i < dataList.size(); i++) {
                    rowData = "";
                    for (int j = 0; j < dataList.get(i).size(); j++) {
                        rowData = rowData + dataList.get(i).get(j);
                        if (j < dataList.get(i).size()) {
                            rowData = rowData + ",";
                        }
                    }
                    out.write(rowData);
                    out.newLine();
                }
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeDataIntoCSV(List<List<String>> dataList, String filename) {
        try {
            File file = new File(FILE_FOLDER + File.separator + filename + ".csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write("TripId" + "," + "Heading" + "," + "GPS(Longtitude)" + "," + "GPS(Latitude)" + "," + "RPM" + "," + "VehicleSpeed" + ","
                    + "EngineOil" + "," + "EngineCoolant" + "," + "fuelLevel" + ","
                    + "PhoneAccX" + "," + "PhoneAccY" + "," + "PhoneAccZ" + ","
                    + "PhoneGravityX" + "," + "PhoneGravityX" + "," + "PhoneGravityZ" + ","
                    + "PhoneOrientationX" + "," + "PhoneOrientationY" + "," + "PhoneOrientationZ" + ","
                    + "PhoneGyroscopeX" + "," + "PhoneGyroscopeY" + "," + "PhoneGyroscopeZ" + ","
                    + "PhoneRotationVectorX" + "," + "PhoneRotationVectorY" + "," + "PhoneRotationVectorZ" + "," + "TimeStamp");
            bw.newLine();
            String rowData = "";
            for (int i = 0; i < dataList.size(); i++) {
                rowData = "";
                for (int j = 0; j < dataList.get(i).size(); j++) {
                    rowData = rowData + dataList.get(i).get(j);
                    if (j < dataList.get(i).size()) {
                        rowData = rowData + ",";
                    }
                }
                bw.write(rowData);
                bw.newLine();
            }
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String converToJson(List<List<String>> dataList, String username, String password){
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("username", username);
            jsonObj.put("password", password);

            JSONArray array = new JSONArray();
            for (int i = 0; i < dataList.size();i++) {
                JSONObject obj = new JSONObject();
                obj.put("trip_id", dataList.get(i).get(0));
                obj.put("heading", dataList.get(i).get(1));
                obj.put("gps_longtitude", dataList.get(i).get(2));
                obj.put("gps_latitude", dataList.get(i).get(3));
                obj.put("rpm", dataList.get(i).get(4));
                obj.put("vehicle_speed", dataList.get(i).get(5));
                obj.put("engine_oil", dataList.get(i).get(6));
                obj.put("engine_cooltant", dataList.get(i).get(7));
                obj.put("fuel_level", dataList.get(i).get(8));
                obj.put("acc_x", dataList.get(i).get(9));
                obj.put("acc_y", dataList.get(i).get(10));
                obj.put("acc_z", dataList.get(i).get(11));
                obj.put("gravity_x", dataList.get(i).get(12));
                obj.put("gravity_y", dataList.get(i).get(13));
                obj.put("gravity_z", dataList.get(i).get(14));
                obj.put("orientation_x", dataList.get(i).get(15));
                obj.put("orientation_y", dataList.get(i).get(16));
                obj.put("orientation_z", dataList.get(i).get(17));
                obj.put("gyroscope_x", dataList.get(i).get(18));
                obj.put("gyroscope_y", dataList.get(i).get(19));
                obj.put("gyroscope_z", dataList.get(i).get(20));
                obj.put("rotation_vector_x", dataList.get(i).get(21));
                obj.put("rotation_vector_y", dataList.get(i).get(22));
                obj.put("rotation_vector_z", dataList.get(i).get(23));
                obj.put("earth_acc_x", dataList.get(i).get(24));
                obj.put("earth_acc_y", dataList.get(i).get(25));
                obj.put("earth_acc_z", dataList.get(i).get(26));
                obj.put("magnetic_x", dataList.get(i).get(27));
                obj.put("magnetic_y", dataList.get(i).get(28));
                obj.put("magnetic_z", dataList.get(i).get(29));
                obj.put("pressure", dataList.get(i).get(30));
                obj.put("phoneLinearAccX", dataList.get(i).get(31));
                obj.put("phoneLinearAccY", dataList.get(i).get(32));
                obj.put("phoneLinearAccZ", dataList.get(i).get(33));
                obj.put("timestamp", dataList.get(i).get(34));
                obj.put("frequence", dataList.get(i).get(35));
                array.put(obj);
            }
            jsonObj.put("dataList", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObj.toString();
    }


    public static String convertJsonString(List<List<String>> dataList,String userName, String password){
        String jsonString = "{\"name\":\""+userName+"\",\"password\":\""+password+"\",";

        jsonString = jsonString + "\"data\":[";
        for(int i = 0; i < dataList.size();i++){
            jsonString = jsonString + "{";
            jsonString = jsonString + "\"trip_id\":\""+dataList.get(i).get(0)+"\",";
            jsonString = jsonString + "\"heading\":\""+dataList.get(i).get(1)+"\",";
            jsonString = jsonString + "\"gps_longtitude\":\""+dataList.get(i).get(2)+"\",";
            jsonString = jsonString + "\"gps_latitude\":\""+dataList.get(i).get(3)+"\",";
            jsonString = jsonString + "\"rpm\":\""+dataList.get(i).get(4)+"\",";
            jsonString = jsonString + "\"vehicle_speed\":\""+dataList.get(i).get(5)+"\",";
            jsonString = jsonString + "\"engine_oil\":\""+dataList.get(i).get(6)+"\",";
            jsonString = jsonString + "\"engine_cooltant\":\""+dataList.get(i).get(7)+"\",";
            jsonString = jsonString + "\"fuel_level\":\""+dataList.get(i).get(8)+"\",";
            jsonString = jsonString + "\"acc_x\":\""+dataList.get(i).get(9)+"\",";
            jsonString = jsonString + "\"acc_y\":\""+dataList.get(i).get(10)+"\",";
            jsonString = jsonString + "\"acc_z\":\""+dataList.get(i).get(11)+"\",";
            jsonString = jsonString + "\"gravity_x\":\""+dataList.get(i).get(12)+"\",";
            jsonString = jsonString + "\"gravity_y\":\""+dataList.get(i).get(13)+"\",";
            jsonString = jsonString + "\"gravity_z\":\""+dataList.get(i).get(14)+"\",";
            jsonString = jsonString + "\"orientation_x\":\""+dataList.get(i).get(15)+"\",";
            jsonString = jsonString + "\"orientation_y\":\""+dataList.get(i).get(16)+"\",";
            jsonString = jsonString + "\"orientation_z\":\""+dataList.get(i).get(17)+"\",";
            jsonString = jsonString + "\"gyroscope_x\":\""+dataList.get(i).get(18)+"\",";
            jsonString = jsonString + "\"gyroscope_y\":\""+dataList.get(i).get(19)+"\",";
            jsonString = jsonString + "\"gyroscope_z\":\""+dataList.get(i).get(20)+"\",";
            jsonString = jsonString + "\"rotation_vector_x\":\""+dataList.get(i).get(21)+"\",";
            jsonString = jsonString + "\"rotation_vector_y\":\""+dataList.get(i).get(22)+"\",";
            jsonString = jsonString + "\"rotation_vector_z\":\""+dataList.get(i).get(23)+"\",";
            jsonString = jsonString + "\"timestamp\":\""+dataList.get(i).get(24)+"\"";

            jsonString = jsonString + "}";
            if( i < dataList.size()){
                jsonString = jsonString + ",";
            }
        }
        jsonString = jsonString + "]";

        return jsonString + "}";
    }

    public static List<List<String>> readCSVFile(String fileName){
        List<List<String>> result = new ArrayList<>();

        File inFile = new File(FILE_FOLDER + File.separator + fileName + ".csv");
        String inString;
        try {
            BufferedReader reader =
                    new BufferedReader(new FileReader(inFile));
            inString = reader.readLine();
            List<String> rowList = new ArrayList<>();
            while ((inString = reader.readLine()) != null) {
                rowList = new ArrayList<>();
                String item[] = inString.split(",");
                for (int j = 0; j < item.length; j++){
                    rowList.add(item[j]);
                }
                result.add(rowList);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
