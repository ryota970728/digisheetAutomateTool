package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import io.github.bonigarcia.wdm.WebDriverManager;

public class AutomateTool {

  // 有給休暇リスト
  List<String> holiday = new ArrayList<>();
  // 在宅勤務リスト
  List<String> workRemotely = new ArrayList<>();
  // A勤務リスト
  List<String> workTypeA = new ArrayList<>();
  // 残業時間マップ(key:日付 value:終業時間)
  Map<String, String> overtTime = new HashMap<>();

  /*
   * digisheetの自動化
   */
  public void automateDigisheet() {

    // プロパティファイルの読み込み
    FileUtil fileUtil = new FileUtil();
    Boolean propetiesFlg = fileUtil.getPropetiy();

    // プロパティファイルの初期設定に失敗したら処理を終わる
    if (!propetiesFlg) {
      System.out.println("プロパティファイルの読み込みに失敗しました");
      return;
    }
    holiday = fileUtil.getHoliday();
    workRemotely = fileUtil.getWorkRemotely();
    workTypeA = fileUtil.getWorkTypeA();
    overtTime = fileUtil.getOverTime();

    // WebDriverManagerを使用してChromeDriverを自動更新
    WebDriverManager.chromedriver().setup();
    // デフォルトで右記の場所に保存される ~/.cache/selenium
    // (例)/Users/hiraryouta/.cache/selenium/chromedriver/mac-arm64/126.0.6478.182/chromedriver
    System.out.println("ChromeDriverのパス:" + System.getProperty("webdriver.chrome.driver"));

    // Chromeを起動する
    WebDriver driver = new ChromeDriver();

    try {
      // 指定したURLに遷移する
      driver.get(Common.URL_DIGISHEET);

      // ログインする
      login(driver);
      Thread.sleep(2000);

      // 勤怠報告画面に遷移する
      changeAttendancReport(driver);
      Thread.sleep(1000);

      // デフォルトに切り替える
      driver.switchTo().defaultContent();

      // frameをmainに切り替える
      driver.switchTo().frame("main");
      Thread.sleep(1000);

      // 勤怠入力する
      inputAttendance(driver);
      Thread.sleep(2000);

      System.out.println("successed!!");

    } catch (org.openqa.selenium.NoSuchElementException e) {
      System.out.println("要素が見つかりませんでした: " + e.getMessage());
    } catch (org.openqa.selenium.TimeoutException e) {
      System.out.println("要素が表示されませんでした: " + e.getMessage());
    } catch (Exception e) {
      System.out.println("エラー：" + e.getMessage());
    } finally {
      // ブラウザを閉じる
      driver.quit();
    }
  }

  /**
   * ログイン
   * 
   * @param driver
   */
  public void login(WebDriver driver) {
    try {
      // 派遣先CDの入力フィールドを見つける(テキストボックスの要素をname属性値から取得する)
      WebElement cdField = driver.findElement(By.name("HC"));
      // テキストボックスに値を入力する
      cdField.sendKeys(Common.CD);

      // スタッフID
      WebElement idField = driver.findElement(By.name("UI"));
      idField.sendKeys(Common.ID);

      // パスワード
      WebElement passField = driver.findElement(By.name("Pw"));
      passField.sendKeys(Common.PASSWORD);

      // ログイン
      WebElement loginField = driver.findElement(By.name("loginButton"));

      // ログインボタンをクリックする
      loginField.click();

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * 勤怠報告画面に遷移
   * 
   * @param driver
   */
  public void changeAttendancReport(WebDriver driver) {
    try {
      // frameをmenuに切り替える
      driver.switchTo().frame("menu");

      // 勤務報告ボタンを見つけてクリック
      WebElement attendanceField = driver.findElement(By.linkText("勤務報告"));
      attendanceField.click();

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * 勤怠入力
   * 
   * @param driver
   */
  public void inputAttendance(WebDriver driver) {
    String errorDay = "";
    try {
      // 勤務表のテーブル(table)を取得する
      WebElement tableElement = driver.findElement(By.xpath("/html/body/form/table/tbody/tr[7]/td/table"));

      // 勤務表の行(row)を取得する
      List<WebElement> trElements = tableElement.findElements(By.xpath(".//tr"));

      // 勤怠表のrow属性を繰り返す
      for (int i = 0; i < trElements.size(); i++) {
        WebElement trElement = trElements.get(i);

        // trの背景色を取得する
        String trBgcolor = trElement.getAttribute("bgcolor");

        // 行の背景色が「white」か確認(平日かを判定)
        if (trBgcolor != null && trBgcolor.equals("white")) {
          // 勤務表の列(column)を取得する
          List<WebElement> tdElements = trElement.findElements(By.xpath(".//td"));

          // 勤務表のcolumn属性を繰り返す
          for (int j = 0; j < tdElements.size(); j++) {
            WebElement tdElement = tdElements.get(j);

            // tdの背景色を取得する
            String tdBgcolor = tdElement.getAttribute("bgcolor");

            // セルの背景色が「#0000FF」ならクリックして画面遷移する
            if (tdBgcolor != null && tdBgcolor.equals("#0000FF")) {
              // テキストを取得
              String day = tdElement.getText();
              // エラーログ用
              errorDay = day;
              // 日付をクリック
              tdElement.click();
              Thread.sleep(1000);

              // 勤怠の詳細情報を設定
              detailSetting(driver, day);

              WebElement registButtonElement = driver.findElement(By.xpath("//input[@value='登　録']"));
              registButtonElement.click();
              Thread.sleep(1000);

              // 画面遷移後にテーブルの行を再取得する
              tableElement = driver.findElement(By.xpath("/html/body/form/table/tbody/tr[7]/td/table"));
              trElements = tableElement.findElements(By.xpath(".//tr"));
              break;
            }
          }
        }
      }
    } catch (Exception e) {
      System.out.println(errorDay + "日入力分のエラー：" + e.getMessage());
    }
  }

  /*
   * 勤務入力の詳細設定
   */
  public void detailSetting(WebDriver driver, String day) {
    try {
      // B勤務をデフォルトで設定する
      setWorkTypeB(driver);

      // 有給休暇の判定
      if (holiday.contains(day)) {
        setHoliday(driver);
      }

      // 在宅勤務の判定
      if (workRemotely.contains(day)) {
        setWorkRemotely(driver);
      }

      // A勤務の判定
      if (workTypeA.contains(day)) {
        setWorkTypeA(driver);
      }

      // 残業時間の判定
      if (overtTime.containsKey(day)) {
        setOverTime(driver, day);
      }
    } catch (Exception e) {
      throw e;
    }
  }

  /**
   * B勤務を設定
   * 
   * @param driver
   */
  public void setWorkTypeB(WebDriver driver) {
    try {
      // AttendSelect：届出選択
      WebElement selectElement = driver.findElement(By.name("AttendSelect"));
      Select select = new Select(selectElement);
      // B0：B勤務 7:50-16:10
      select.selectByValue("B0");
      Thread.sleep(1000);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * 年次有給休暇を設定
   */
  public void setHoliday(WebDriver driver) {
    try {
      // AttendSecSelect：シフト選択
      WebElement selectElement = driver.findElement(By.name("AttendSecSelect"));
      Select select = new Select(selectElement);
      // 12：年次有給休暇（有給）
      select.selectByValue("12");
      Thread.sleep(1000);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * 在宅勤務を設定
   */
  public void setWorkRemotely(WebDriver driver) {
    try {
      // ContentSelect：その他業務
      WebElement selectElement = driver.findElement(By.name("ContentSelect"));
      Select select = new Select(selectElement);
      // 0000000600：在宅 所定時間以上
      select.selectByValue("0000000600");
      Thread.sleep(1000);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * A勤務を設定
   */
  public void setWorkTypeA(WebDriver driver) {
    try {
      // AttendSelect：届出選択
      WebElement selectElement = driver.findElement(By.name("AttendSelect"));
      Select select = new Select(selectElement);
      // A0：A勤務 8:50-17:10
      select.selectByValue("A0");
      Thread.sleep(1000);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * 残業時間の設定
   */
  public void setOverTime(WebDriver driver, String day) {
    try {
      // キーに含まれる値を取得する
      String overTimeDay = overtTime.get(day);

      // 1610 → hour:16 minutes:10のように分解する
      String hour = overTimeDay.substring(0, 2);
      String minutes = overTimeDay.substring(2, 4).replaceFirst("^0+(?!$)", "");

      // HourEnd：就業時間(hour)
      WebElement hourElement = driver.findElement(By.name("HourEnd"));
      Select selectHour = new Select(hourElement);
      selectHour.selectByValue(hour);

      // MinuteEnd：就業時間(minutes)
      WebElement minutesElement = driver.findElement(By.name("MinuteEnd"));
      Select selectMinutes = new Select(minutesElement);
      selectMinutes.selectByValue(minutes);

      Thread.sleep(1000);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
