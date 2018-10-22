package com.example.wuzhiyun.tglj;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread() {
            @Override
            public void run() {
                super.run();
                Document doc = null;
                int year = 2018;
                int jidu = 4;
                String date = "10-22";
                String lundar = "";
                try {
                    lundar = CalendarUtil.solarToLunar("20181022");
                    if (!TextUtils.isEmpty(lundar)) {
                        lundar = lundar.substring(8);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int zhangDieNum = 0;
                int zuiZhiNum = 0;
                int dieNum = 0;
                int zuiDieNum = 0;
                int zhangDieNumLundar = 0;
                int zuiZhiNumLundar = 0;
                int dieNumLundar = 0;
                int zuiDieNumLundar = 0;
                for (int k = 0; k < 12; k++) {
                    try {
//                            String url= "http://quotes.money.163.com/service/chddata.html?code=";
//                            String code = "600516";
//                            String start = (System.currentTimeMillis()- 30*24*60*60*1000)+"";
//                                String end = System.currentTimeMillis() + "";
//                                Log.e("end:", end);
//                            url += code;
//                            url += "&start="+ start;
//                            url += "&end=" + end;
//                            url += "&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;VOTURNOVER;VATURNOVER";
                        String code = "399001";
//                            String url = "http://flashquote.stock.hexun.com/Quotejs/DA/2_600516_DA.html";
//
                        String url = "http://flashquote.stock.hexun.com/Quotejs/DA/";
                        if ("600000".compareTo(code) < 0) {
                            url += "1_";
                        } else {
                            url += "2_";
                        }
                        url += code + "_DA.html";

                        year -= 1;
                        url = "http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/000669.phtml?year=" + year + "&jidu=" + jidu;
                        doc = Jsoup.connect(url).get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Element table = doc.getElementById("FundHoldSharesTable");
                    if (table == null) {
                        continue;
                    }
                    Elements trs = table.select("tr");
                    for (int i = 0; i < trs.size(); i++) {
                        Elements tds = trs.get(i).select("td");
                        boolean isFind = false;
                        boolean isFindLundar = false;
                        for (int j = 0; j < tds.size(); j++) {
                            String txt = tds.get(j).text();
                            if (!TextUtils.isEmpty(txt)) {
                                try {
                                    if (txt.endsWith(date)) {
                                        isFind = true;
                                        Log.e("data", txt);
                                        txt = tds.get(j + 1).text();
                                        Double kaipan = Double.valueOf(txt);
                                        Log.e("data", txt);
                                        txt = tds.get(j + 2).text();
                                        Double zuigaozhi = Double.valueOf(txt);
                                        Log.e("data", txt);
                                        txt = tds.get(j + 3).text();
                                        Double shoupan = Double.valueOf(txt);
                                        Log.e("data", txt);
                                        txt = tds.get(j + 4).text();
                                        Double zuidizhi = Double.valueOf(txt);
                                        Log.e("data", txt);
                                        txt = tds.get(j + 5).text();
                                        Log.e("data", txt);
                                        txt = tds.get(j + 6).text();
                                        Log.e("data", txt);
                                        Log.e("data", "涨跌：" + ((shoupan - kaipan) > 0 ? "涨" : "跌"));
                                        if (shoupan - kaipan > 0) {
                                            zhangDieNum++;
                                        } else {
                                            dieNum++;
                                        }
                                        if ((zuigaozhi - shoupan) / shoupan > 0.02) {
                                            zuiZhiNum++;
                                        } else if ((shoupan - zuidizhi) / zuidizhi > 0.02) {
                                            zuiDieNum++;
                                        }
                                        break;
                                    } else if (txt.contains("-") && CalendarUtil.solarToLunar(txt.replace("-", "")).endsWith(lundar)) {
                                        isFindLundar = true;
                                        Log.e("阴历data", txt);
                                        txt = tds.get(j + 1).text();
                                        Double kaipan = Double.valueOf(txt);
                                        Log.e("阴历data", txt);
                                        txt = tds.get(j + 2).text();
                                        Double zuigaozhi = Double.valueOf(txt);
                                        Log.e("阴历data", txt);
                                        txt = tds.get(j + 3).text();
                                        Double shoupan = Double.valueOf(txt);
                                        Log.e("阴历data", txt);
                                        txt = tds.get(j + 4).text();
                                        Double zuidizhi = Double.valueOf(txt);
                                        Log.e("阴历data", txt);
                                        txt = tds.get(j + 5).text();
                                        Log.e("阴历data", txt);
                                        txt = tds.get(j + 6).text();
                                        Log.e("阴历data", txt);
                                        Log.e("阴历data", "阴历涨跌：" + ((shoupan - kaipan) > 0 ? "涨" : "跌"));
                                        if (shoupan - kaipan > 0) {
                                            zhangDieNumLundar++;
                                        } else {
                                            dieNumLundar++;
                                        }
                                        if ((zuigaozhi - shoupan) / shoupan > 0.02) {
                                            zuiZhiNumLundar++;
                                        } else if ((shoupan - zuidizhi) / zuidizhi > 0.02) {
                                            zuiDieNumLundar++;
                                        }
                                        break;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (isFind && isFindLundar) {
                            break;
                        }
                    }
                }
                Log.e("data", "涨次数：" + zhangDieNum + " 跌次数：" + dieNum);
                Log.e("data", "最值涨次数：" + zuiZhiNum + "最值跌次数：" + zuiDieNum);
                Log.e("data", "阴历：" + lundar);
                Log.e("data", "涨次数：" + zhangDieNumLundar + " 跌次数：" + dieNumLundar);
                Log.e("data", "最值涨次数：" + zuiZhiNumLundar + " 最值跌次数：" + zuiDieNumLundar);

//                        String data = doc.toString();
//                        String [] dataArray = data.substring(data.indexOf("[[")+ 2, data.lastIndexOf("]]")).split(",");

//                        Log.e("data", data);
//                        Log.e("一、dataArray.length", dataArray.length+"");
//                        Log.e("一、dataArray[0]", dataArray[0]);
////                       【交易日期、昨收盘、开盘、最高、最低、最新价、成交量、成交额】
//                        Log.e("一、dataArray[8]", dataArray[dataArray.length -8]);
//                        Log.e("一、dataArray[7]", dataArray[dataArray.length -7]);
//                        Log.e("一、dataArray[6]", dataArray[dataArray.length -6]);
//                        Log.e("一、dataArray[5]", dataArray[dataArray.length -5]);
//                        Log.e("一、dataArray[4]", dataArray[dataArray.length -4]);
//                        Log.e("一、dataArray[3]", dataArray[dataArray.length -3]);
//                        Log.e("一、dataArray[2]", dataArray[dataArray.length -2]);
//                        Log.e("一、dataArray[1]", dataArray[dataArray.length -1]);
            }
        }.start();
    }
}
