package com.example;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class FileUtil {

  // 有給休暇リスト
  private List<String> holiday = new ArrayList<>();
  // 在宅勤務リスト
  private List<String> workRemotely = new ArrayList<>();
  // A勤務リスト
  private List<String> workTypeA = new ArrayList<>();
  // 残業時間マップ(key:日付 value:終業時間)
  private Map<String, String> overtTime = new HashMap<>();

  public List<String> getHoliday() {
    return this.holiday;
  }

  public List<String> getWorkRemotely() {
    return this.workRemotely;
  }

  public List<String> getWorkTypeA() {
    return this.workTypeA;
  }

  public Map<String, String> getOverTime() {
    return this.overtTime;
  }

  /*
   * プロパティファイルを取得する
   */
  public Boolean getPropetiy() {
    Properties properties = new Properties();
    try {
      InputStream inputStream = new FileInputStream(Common.PROPETIY_FILE_PATH);
      properties.load(inputStream);
      holiday = spritString(properties.getProperty("HOLIDAY"));
      workRemotely = spritString(properties.getProperty("WORKREMOTELY"));
      workTypeA = spritString(properties.getProperty("WORKTYPE_A"));
      overtTime = getOverTimeMap(properties.getProperty("OVERTIME"));
      return true;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /*
   * リストにカンマ区切りで値を格納する
   */
  public List<String> spritString(String str) {
    try {
      List<String> dayList = new ArrayList<>();

      if (str.equals("")) {
        return dayList;
      }
      // カンマ区切りで配列に値を格納する
      String[] days = str.split(",");
      // Listに格納する
      for (String day : days) {
        dayList.add(day);
      }
      return dayList;
    } catch (Exception e) {
      throw e;
    }
  }

  /*
   * マップにカンマ区切りでキーと値を格納する
   */
  public Map<String, String> getOverTimeMap(String str) {
    try {
      Map<String, String> overTimeMap = new HashMap<>();

      if (str.equals("")) {
        return overTimeMap;
      }
      String[] tmp = str.split(",");
      for (String overTime : tmp) {
        String[] item = overTime.split("-");
        // キー:item[0]→残業日, 値:item[1]→終業時間
        overTimeMap.put(item[0], item[1]);
      }
      return overTimeMap;
    } catch (Exception e) {
      throw e;
    }
  }
}
