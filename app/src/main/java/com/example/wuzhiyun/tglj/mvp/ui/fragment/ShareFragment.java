package com.example.wuzhiyun.tglj.mvp.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wuzhiyun.tglj.CalendarUtil;
import com.example.wuzhiyun.tglj.R;
import com.example.wuzhiyun.tglj.TGLJApplication;
import com.example.wuzhiyun.tglj.db.ShareCodeName;
import com.example.wuzhiyun.tglj.db.ShareRealm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.simple.eventbus.EventBus;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ShareFragment extends Fragment {
    View mView;
    @BindView(R.id.solar_today_day)
    TextView solarTodayDayTxt;
    @BindView(R.id.solar_today_num)
    TextView solarTodayNumTxt;
    @BindView(R.id.solar_today_rise)
    TextView solarTodayRiseTxt;
    @BindView(R.id.solar_today_fall)
    TextView solarTodayFallTxt;
    @BindView(R.id.solar_today_yang)
    TextView solarTodayYangTxt;
    @BindView(R.id.solar_today_yin)
    TextView solarTodayYinTxt;
    @BindView(R.id.lunar_today_day)
    TextView lunarTodayDayTxt;
    @BindView(R.id.lunar_today_num)
    TextView lunarTodayNumTxt;
    @BindView(R.id.lunar_today_rise)
    TextView lunarTodayRiseTxt;
    @BindView(R.id.lunar_today_fall)
    TextView lunarTodayFallTxt;
    @BindView(R.id.lunar_today_yang)
    TextView lunarTodayYangTxt;
    @BindView(R.id.lunar_today_yin)
    TextView lunarTodayYinTxt;
    Unbinder unbinder;
    @BindView(R.id.solar_high_open)
    TextView solarHighOpenTxt;
    @BindView(R.id.solar_low_open)
    TextView solarLowOpenTxt;
    @BindView(R.id.lunar_high_open)
    TextView lunarHighOpenTxt;
    @BindView(R.id.lunar_low_open)
    TextView lunarLowOpenTxt;
    @BindView(R.id.yin_yang_5)
    TextView yinYang5Txt;
    @BindView(R.id.yin_yang_10)
    TextView yinYang10Txt;
    @BindView(R.id.yin_yang_20)
    TextView yinYang20Txt;
    private String code;//股票代码
    private SparseArray<ShareRealm> arrayData;//k线数据
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_share, null);
            code = getArguments().getString("CODE");
            arrayData = new SparseArray<>();
            getData();
        }
        unbinder = ButterKnife.bind(this, mView);
        return mView;
    }


    private void getData() {
        initData();
        new Thread() {
            @Override
            public void run() {
                super.run();
                Document doc = null;
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                String today = sdf.format(calendar.getTime());
                Log.e("wuzhiyun" + code, "今日：" + today);
                int jidu = month / 3 + (month % 3 == 0 ? 0 : 1);
                Realm realm = TGLJApplication.getInstance().getRealm();
                RealmResults<ShareRealm> realmRealmResults = realm.where(ShareRealm.class)
                        .equalTo("code", code)
                        .findAllSorted("id", Sort.DESCENDING);
                String date = "";
                if (realmRealmResults != null && realmRealmResults.size() > 0) {
                    date = realmRealmResults.get(0).getDateYear() + realmRealmResults.get(0).getDate();
                    for (int i = 0; i < realmRealmResults.size(); i++) {
                        String dateStr = realmRealmResults.get(i).getDateYear() + realmRealmResults.get(i).getDate();
                        arrayData.append(Integer.valueOf(dateStr), realmRealmResults.get(i));
                    }
                }
                calendar.add(Calendar.DATE, -1);// 日期减1
                if (calendar.get(Calendar.DAY_OF_WEEK) == 7) {
                    calendar.add(Calendar.DATE, -1);// 日期减1
                } else if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
                    calendar.add(Calendar.DATE, -2);// 日期减2
                }
                String yestoday = sdf.format(calendar.getTime());//上一个交易日
                Log.e("wuzhiyun" + code, "下一个交易日：" + yestoday);
                calendar.add(Calendar.DATE, 2);
                if (calendar.get(Calendar.DAY_OF_WEEK) == 7) {
                    calendar.add(Calendar.DATE, 2);
                } else if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
                    calendar.add(Calendar.DATE, 1);
                }
                String tomorrow = sdf.format(calendar.getTime());//下一个交易日
                Log.e("wuzhiyun" + code, "下一个交易日：" + tomorrow);
                //已取到昨天数据。今天还未收盘
                if (!TextUtils.isEmpty(date) && date.equals(yestoday) && calendar.get(Calendar.HOUR_OF_DAY) < 15) {
                    analyze(today);
                    realm.close();
                    return;
                }
                boolean isOver = false;
                for (int k = 0; k < yearNum; k++, year--) {
                    if (isOver) {
                        break;
                    }
                    int j = 4;
                    if (year == calendar.get(Calendar.YEAR)) {
                        j = jidu;
                    }
                    for (; j > 0; j--) {
                        if (isOver) {
                            break;
                        }
                        try {
                            String url = "http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/" + code + ".phtml?year=" + year + "&jidu=" + j;
                            doc = Jsoup.connect(url).get();
                            if (doc == null) {
                                isOver = true;
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (doc == null) {
                            isOver = true;
                            break;
                        }
                        Element table = doc.getElementById("FundHoldSharesTable");
                        Log.e("wuzhiyun" + code, code + ":" + year + " " + j);
                        if (table == null) {
                            continue;
                        }
                        Elements trs = table.select("tr");
                        ShareCodeName shareCodeName = realm.where(ShareCodeName.class).equalTo("code", code).findFirst();
                        String text = trs.get(0).select("th").get(0).text();
                        String shareName = text.substring(0, text.indexOf("("));
                        if (shareCodeName == null || !shareName.equals(shareCodeName.getName())) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    ShareCodeName shareCodeName1 = new ShareCodeName();
                                    shareCodeName1.setCode(code);
                                    shareCodeName1.setName(shareName);
                                    realm.copyToRealmOrUpdate(shareCodeName1);
                                    EventBus.getDefault().post(shareCodeName1);

                                }
                            });
                        }
                        for (int i = 2; i < trs.size(); i++) {
                            Elements tds = trs.get(i).select("td");
                            String dateStr = tds.get(0).text().replace("-", "");
                            if (date.equals(dateStr)) {
                                isOver = true;
                                break;
                            }
                            try {
                                final ShareRealm shareK = new ShareRealm();
                                shareK.setId(code + dateStr);
                                shareK.setCode(code);
                                shareK.setDateYear(dateStr.substring(0, 4));
                                shareK.setDate(dateStr.substring(4));
                                shareK.setOpenPrice(Double.valueOf(tds.get(1).text()));
                                shareK.setMaxPrice(Double.valueOf(tds.get(2).text()));
                                shareK.setClosingPrice(Double.valueOf(tds.get(3).text()));
                                shareK.setMinPrice(Double.valueOf(tds.get(4).text()));
                                shareK.setVolume(Long.valueOf(tds.get(5).text()));
                                shareK.setTurnover(Long.valueOf(tds.get(6).text()));
                                String dateLunar = CalendarUtil.solarToLunar(dateStr);
                                shareK.setLeap(dateLunar.contains("闰"));
                                String lunarTemp = dateLunar.replace("闰", "");
                                shareK.setLunarYear(lunarTemp.substring(0, 4));
                                shareK.setLunar(lunarTemp.substring(4));
                                arrayData.append(Integer.valueOf(dateStr), shareK);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        //防爬虫检测
                        try {
                            Thread.sleep(new Random().nextInt(5) * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                }
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        for (int i = 0; i < arrayData.size(); i++) {
                            realm.copyToRealmOrUpdate(arrayData.valueAt(i));
                        }

                    }
                });
                realm.close();
                if (calendar.get(Calendar.HOUR_OF_DAY) < 15) {
                    analyze(today);
                } else {
                    analyze(tomorrow);
                }
                analyzeK();
            }
        }.start();
    }

    //相似阴阳线；true为阳
    private boolean[] yinYang5 = new boolean[5];
    private boolean[] yinYang10 = new boolean[10];
    private boolean[] yinYang20 = new boolean[20];
    private boolean[] yinYang5Tem = new boolean[5];
    private boolean[] yinYang10Tem = new boolean[10];
    private boolean[] yinYang20Tem = new boolean[20];
    private int yinYang5Index = 0;
    private int yinYang10Index = 0;
    private int yinYang20Index = 0;

    //寻找相似K线
    private void analyzeK() {
        initYinYang();
        for (int i = arrayData.size() - 1; i > 4; i--) {
            ShareRealm shareRealmI = arrayData.valueAt(i);
            compareYinYang(shareRealmI);
        }
    }

    //连续交易日阴阳线相同
    private void compareYinYang(ShareRealm shareRealm) {
        yinYang5Tem[yinYang5Index] = shareRealm.getClosingPrice() > shareRealm.getOpenPrice();
        yinYang10Tem[yinYang10Index] = shareRealm.getClosingPrice() > shareRealm.getOpenPrice();
        yinYang20Tem[yinYang20Index] = shareRealm.getClosingPrice() > shareRealm.getOpenPrice();
        if (yinYang5Index == 4) {
            yinYang5Index = 0;
        } else {
            yinYang5Index++;
        }
        if (yinYang10Index == 9) {
            yinYang10Index = 0;
        } else {
            yinYang10Index++;
        }
        if (yinYang20Index == 19) {
            yinYang20Index = 0;
        } else {
            yinYang20Index++;
        }
        //连续5个交易日阴阳线相同
        if (yinYang5[0] == yinYang5Tem[yinYang5Index]
                && yinYang5[1] == yinYang5Tem[yinYang5Index + 1 > 4 ? yinYang5Index - 4 : yinYang5Index + 1]
                && yinYang5[2] == yinYang5Tem[yinYang5Index + 2 > 4 ? yinYang5Index - 3 : yinYang5Index + 2]
                && yinYang5[3] == yinYang5Tem[yinYang5Index + 3 > 4 ? yinYang5Index - 2 : yinYang5Index + 3]
                && yinYang5[4] == yinYang5Tem[yinYang5Index + 4 > 4 ? yinYang5Index - 1 : yinYang5Index + 4]) {
            if (TextUtils.isEmpty(yinYang5Txt.getText())) {
                yinYang5Txt.setText("连续5个交易日阴阳线相同：" + shareRealm.getDateYear() + shareRealm.getDate());
            } else {
                yinYang5Txt.setText(yinYang5Txt.getText() + "、" + shareRealm.getDateYear() + shareRealm.getDate());
            }
        }
        //连续10个交易日阴阳线相同
        if (yinYang10[0] == yinYang10Tem[yinYang10Index]
                && yinYang10[1] == yinYang10Tem[yinYang10Index + 1 > 9 ? yinYang10Index - 9 : yinYang10Index + 1]
                && yinYang10[2] == yinYang10Tem[yinYang10Index + 2 > 9 ? yinYang10Index - 8 : yinYang10Index + 2]
                && yinYang10[3] == yinYang10Tem[yinYang10Index + 3 > 9 ? yinYang10Index - 7 : yinYang10Index + 3]
                && yinYang10[4] == yinYang10Tem[yinYang10Index + 4 > 9 ? yinYang10Index - 6 : yinYang10Index + 4]
                && yinYang10[5] == yinYang10Tem[yinYang10Index + 5 > 9 ? yinYang10Index - 5 : yinYang10Index + 5]
                && yinYang10[6] == yinYang10Tem[yinYang10Index + 6 > 9 ? yinYang10Index - 4 : yinYang10Index + 6]
                && yinYang10[7] == yinYang10Tem[yinYang10Index + 7 > 9 ? yinYang10Index - 3 : yinYang10Index + 7]
                && yinYang10[8] == yinYang10Tem[yinYang10Index + 8 > 9 ? yinYang10Index - 2 : yinYang10Index + 8]
                && yinYang10[9] == yinYang10Tem[yinYang10Index + 9 > 9 ? yinYang10Index - 1 : yinYang10Index + 9]) {
            if (TextUtils.isEmpty(yinYang10Txt.getText())) {
                yinYang10Txt.setText("连续10个交易日阴阳线相同：" + shareRealm.getDateYear() + shareRealm.getDate());
            } else {
                yinYang10Txt.setText(yinYang10Txt.getText() + "、" + shareRealm.getDateYear() + shareRealm.getDate());
            }

        }
        //连续20个交易日阴阳线中16个相同
        int num = 0;
        if (yinYang20[0] == yinYang20Tem[yinYang20Index]) {
            num++;
        }
        if (yinYang20[1] == yinYang20Tem[yinYang20Index + 1 > 19 ? yinYang20Index - 19 : yinYang20Index + 1]) {
            num++;
        }
        if (yinYang20[2] == yinYang20Tem[yinYang20Index + 2 > 19 ? yinYang20Index - 18 : yinYang20Index + 2]) {
            num++;
        }
        if (yinYang20[3] == yinYang20Tem[yinYang20Index + 3 > 19 ? yinYang20Index - 17 : yinYang20Index + 3]) {
            num++;
        }
        if (yinYang20[4] == yinYang20Tem[yinYang20Index + 4 > 19 ? yinYang20Index - 16 : yinYang20Index + 4]) {
            num++;
        }
        if (yinYang20[5] == yinYang20Tem[yinYang20Index + 5 > 19 ? yinYang20Index - 15 : yinYang20Index + 5]) {
            num++;
        }
        if (yinYang20[6] == yinYang20Tem[yinYang20Index + 6 > 19 ? yinYang20Index - 14 : yinYang20Index + 6]) {
            num++;
        }
        if (yinYang20[7] == yinYang20Tem[yinYang20Index + 7 > 19 ? yinYang20Index - 13 : yinYang20Index + 7]) {
            num++;
        }
        if (yinYang20[8] == yinYang20Tem[yinYang20Index + 8 > 19 ? yinYang20Index - 12 : yinYang20Index + 8]) {
            num++;
        }
        if (yinYang20[9] == yinYang20Tem[yinYang20Index + 9 > 19 ? yinYang20Index - 11 : yinYang20Index + 9]) {
            num++;
        }
        if (yinYang20[10] == yinYang20Tem[yinYang20Index + 10 > 19 ? yinYang20Index - 10 : yinYang20Index + 10]) {
            num++;
        }
        if (yinYang20[1] == yinYang20Tem[yinYang20Index + 11 > 19 ? yinYang20Index - 9 : yinYang20Index + 11]) {
            num++;
        }
        if (yinYang20[2] == yinYang20Tem[yinYang20Index + 12 > 19 ? yinYang20Index - 8 : yinYang20Index + 12]) {
            num++;
        }
        if (yinYang20[3] == yinYang20Tem[yinYang20Index + 13 > 19 ? yinYang20Index - 7 : yinYang20Index + 13]) {
            num++;
        }
        if (yinYang20[4] == yinYang20Tem[yinYang20Index + 14 > 19 ? yinYang20Index - 6 : yinYang20Index + 14]) {
            num++;
        }
        if (yinYang20[5] == yinYang20Tem[yinYang20Index + 15 > 19 ? yinYang20Index - 5 : yinYang20Index + 15]) {
            num++;
        }
        if (yinYang20[6] == yinYang20Tem[yinYang20Index + 16 > 19 ? yinYang20Index - 4 : yinYang20Index + 16]) {
            num++;
        }
        if (yinYang20[7] == yinYang20Tem[yinYang20Index + 17 > 19 ? yinYang20Index - 3 : yinYang20Index + 17]) {
            num++;
        }
        if (yinYang20[8] == yinYang20Tem[yinYang20Index + 18 > 19 ? yinYang20Index - 2 : yinYang20Index + 18]) {
            num++;
        }
        if (yinYang20[9] == yinYang20Tem[yinYang20Index + 19 > 19 ? yinYang20Index - 1 : yinYang20Index + 19]) {
            num++;
        }
        if (num >= 16 ) {
            if (TextUtils.isEmpty(yinYang20Txt.getText())) {
                yinYang20Txt.setText("连续20个交易日阴阳线16个相同：" + shareRealm.getDateYear() + shareRealm.getDate());
            } else {
                yinYang20Txt.setText(yinYang20Txt.getText() + "、" + shareRealm.getDateYear() + shareRealm.getDate());
            }
        }
    }

    private void initYinYang() {
        if (arrayData.size() >= 5) {
            yinYang5[0] = arrayData.valueAt(0).getClosingPrice() > arrayData.valueAt(0).getOpenPrice();
            yinYang5[1] = arrayData.valueAt(1).getClosingPrice() > arrayData.valueAt(1).getOpenPrice();
            yinYang5[2] = arrayData.valueAt(2).getClosingPrice() > arrayData.valueAt(2).getOpenPrice();
            yinYang5[3] = arrayData.valueAt(3).getClosingPrice() > arrayData.valueAt(3).getOpenPrice();
            yinYang5[4] = arrayData.valueAt(4).getClosingPrice() > arrayData.valueAt(4).getOpenPrice();
        }
        if (arrayData.size() >= 10) {
            yinYang10[0] = arrayData.valueAt(0).getClosingPrice() > arrayData.valueAt(0).getOpenPrice();
            yinYang10[1] = arrayData.valueAt(1).getClosingPrice() > arrayData.valueAt(1).getOpenPrice();
            yinYang10[2] = arrayData.valueAt(2).getClosingPrice() > arrayData.valueAt(2).getOpenPrice();
            yinYang10[3] = arrayData.valueAt(3).getClosingPrice() > arrayData.valueAt(3).getOpenPrice();
            yinYang10[4] = arrayData.valueAt(4).getClosingPrice() > arrayData.valueAt(4).getOpenPrice();
            yinYang10[5] = arrayData.valueAt(5).getClosingPrice() > arrayData.valueAt(5).getOpenPrice();
            yinYang10[6] = arrayData.valueAt(6).getClosingPrice() > arrayData.valueAt(6).getOpenPrice();
            yinYang10[7] = arrayData.valueAt(7).getClosingPrice() > arrayData.valueAt(7).getOpenPrice();
            yinYang10[8] = arrayData.valueAt(8).getClosingPrice() > arrayData.valueAt(8).getOpenPrice();
            yinYang10[9] = arrayData.valueAt(9).getClosingPrice() > arrayData.valueAt(9).getOpenPrice();
        }
        if (arrayData.size() >= 20) {
            yinYang20[0] = arrayData.valueAt(0).getClosingPrice() > arrayData.valueAt(0).getOpenPrice();
            yinYang20[1] = arrayData.valueAt(1).getClosingPrice() > arrayData.valueAt(1).getOpenPrice();
            yinYang20[2] = arrayData.valueAt(2).getClosingPrice() > arrayData.valueAt(2).getOpenPrice();
            yinYang20[3] = arrayData.valueAt(3).getClosingPrice() > arrayData.valueAt(3).getOpenPrice();
            yinYang20[4] = arrayData.valueAt(4).getClosingPrice() > arrayData.valueAt(4).getOpenPrice();
            yinYang20[5] = arrayData.valueAt(5).getClosingPrice() > arrayData.valueAt(5).getOpenPrice();
            yinYang20[6] = arrayData.valueAt(6).getClosingPrice() > arrayData.valueAt(6).getOpenPrice();
            yinYang20[7] = arrayData.valueAt(7).getClosingPrice() > arrayData.valueAt(7).getOpenPrice();
            yinYang20[8] = arrayData.valueAt(8).getClosingPrice() > arrayData.valueAt(8).getOpenPrice();
            yinYang20[9] = arrayData.valueAt(9).getClosingPrice() > arrayData.valueAt(9).getOpenPrice();
            yinYang20[10] = arrayData.valueAt(10).getClosingPrice() > arrayData.valueAt(10).getOpenPrice();
            yinYang20[11] = arrayData.valueAt(11).getClosingPrice() > arrayData.valueAt(11).getOpenPrice();
            yinYang20[12] = arrayData.valueAt(12).getClosingPrice() > arrayData.valueAt(12).getOpenPrice();
            yinYang20[13] = arrayData.valueAt(13).getClosingPrice() > arrayData.valueAt(13).getOpenPrice();
            yinYang20[14] = arrayData.valueAt(14).getClosingPrice() > arrayData.valueAt(14).getOpenPrice();
            yinYang20[15] = arrayData.valueAt(15).getClosingPrice() > arrayData.valueAt(15).getOpenPrice();
            yinYang20[16] = arrayData.valueAt(16).getClosingPrice() > arrayData.valueAt(16).getOpenPrice();
            yinYang20[17] = arrayData.valueAt(17).getClosingPrice() > arrayData.valueAt(17).getOpenPrice();
            yinYang20[18] = arrayData.valueAt(18).getClosingPrice() > arrayData.valueAt(18).getOpenPrice();
            yinYang20[19] = arrayData.valueAt(19).getClosingPrice() > arrayData.valueAt(19).getOpenPrice();
        }
    }

    //统计次数
    private void analyze(String day) {
        Log.e("wuzhiyun" + code, "统计日期：" + day);
        solarTodayDayTxt.setText("统计日期(阳历)：" + day);
        int todayKey = Integer.valueOf(day);//yyyyMMdd
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(day));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            for (int i = 0; i < yearNum; i++) {
                int key = todayKey - i * 10000;
                ShareRealm shareRealmToday = arrayData.get(key);
                if (shareRealmToday != null) {
                    solarNum++;
                    if (Math.abs(shareRealmToday.getClosingPrice() - shareRealmToday.getOpenPrice()) / shareRealmToday.getOpenPrice() > rate) {
                        if (shareRealmToday.getClosingPrice() > shareRealmToday.getOpenPrice()) {
                            solarYang++;
                        } else {
                            solarYin++;
                        }
                    }
                    for (int j = 1; j < 15; j++) {
                        int yestodayKey = getDayKey(key, j);
                        ShareRealm shareRealmYestoday = arrayData.get(yestodayKey);
                        if (shareRealmYestoday != null) {
                            if (Math.abs(shareRealmToday.getClosingPrice() - shareRealmYestoday.getClosingPrice()) / shareRealmYestoday.getClosingPrice() > rate) {
                                if (shareRealmToday.getClosingPrice() - shareRealmYestoday.getClosingPrice() > 0) {
                                    solarRise++;
                                } else {
                                    solarFall++;
                                }
                            }
                            if (Math.abs(shareRealmToday.getOpenPrice() - shareRealmYestoday.getClosingPrice()) / shareRealmYestoday.getClosingPrice() > rate) {
                                if (shareRealmToday.getOpenPrice() - shareRealmYestoday.getClosingPrice() > 0) {
                                    solarHighOpen++;
                                } else {
                                    solarLowOpen++;
                                }
                            }
                            break;
                        }
                    }
                }


            }

            String lunarTodayStr = CalendarUtil.solarToLunar(day);
            lunarTodayDayTxt.setText("统计日期(阴历)：" + lunarTodayStr);
            Log.e("wuzhiyun" + code, "阴历：" + lunarTodayStr);
            int lunarTodayYear = Integer.valueOf(lunarTodayStr.substring(0, 4));
            String lunarTodayDate = lunarTodayStr.substring(4);
            boolean isLeap = lunarTodayDate.contains("闰");
            if (isLeap) {
                lunarTodayDate.replace("闰", "");
            }
            for (int i = 0; i < yearNum; i++) {
                int key = Integer.valueOf(CalendarUtil.lunarToSolar((lunarTodayYear - i) + lunarTodayDate, isLeap));
                Log.e("wuzhiyun" + code, "key:" + key);
                ShareRealm shareRealmToday = arrayData.get(key);
                if (shareRealmToday != null) {
                    lunarNum++;
                    if (Math.abs(shareRealmToday.getClosingPrice() - shareRealmToday.getOpenPrice()) / shareRealmToday.getOpenPrice() > rate) {
                        if (shareRealmToday.getClosingPrice() > shareRealmToday.getOpenPrice()) {
                            lunarYang++;
                        } else {
                            lunarYin++;
                        }
                    }
                    for (int j = 1; j < 15; j++) {
                        int yestodayKey = getDayKey(key, j);
                        ShareRealm shareRealmYestoday = arrayData.get(yestodayKey);
                        if (shareRealmYestoday != null) {
                            if (Math.abs(shareRealmToday.getClosingPrice() - shareRealmYestoday.getClosingPrice()) / shareRealmYestoday.getClosingPrice() > rate) {
                                if (shareRealmToday.getClosingPrice() - shareRealmYestoday.getClosingPrice() > 0) {
                                    lunarRise++;
                                } else {
                                    lunarFall++;
                                }
                            }
                            if (Math.abs(shareRealmToday.getOpenPrice() - shareRealmYestoday.getClosingPrice()) / shareRealmYestoday.getClosingPrice() > rate) {
                                if (shareRealmToday.getOpenPrice() - shareRealmYestoday.getClosingPrice() > 0) {
                                    lunarHighOpen++;
                                } else {
                                    lunarLowOpen++;
                                }
                            }
                            break;
                        }
                    }
                }

            }
            showData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getDayKey(Integer today, int beforNum) {
        try {
            Date date = sdf.parse(today.toString());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, -beforNum);
            return Integer.valueOf(sdf.format(calendar.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private float rate = 0.01f;//上涨下跌判断标准
    private float rateT = 0.02f;//做T需要浮动比例
    private int yearNum = 13;

    private int solarNum = 0;//阳历统计样本数
    private int solarRise = 0;//阳历上涨次数
    private int solarFall = 0;//阳历下跌次数
    private int solarYang = 0;//阳历阳线次数
    private int solarYin = 0;//阳历阴线次数
    private int solarHighOpen = 0;//阳历高开次数
    private int solarLowOpen = 0;//阳历低开次数

    private int lunarNum = 0;//阴历统计样本数
    private int lunarRise = 0;//阴历上涨次数
    private int lunarFall = 0;//阴历下跌次数
    private int lunarYang = 0;//阴历阳线次数
    private int lunarYin = 0;//阴历阴线次数
    private int lunarHighOpen = 0;//阴历历高开次数
    private int lunarLowOpen = 0;//阴历低开次数


    private void initData() {
        arrayData.clear();
        solarNum = 0;//阳历今天统计样本数
        solarRise = 0;//阳历今天上涨次数
        solarFall = 0;//阳历今天下跌次数
        solarYang = 0;//阳历今天阳线次数
        solarYin = 0;//阳历今天阴线次数
        solarHighOpen = 0;//阳历高开次数
        solarLowOpen = 0;//阳历低开次数

        lunarNum = 0;//阴历今天统计样本数
        lunarRise = 0;//阴历今天上涨次数
        lunarFall = 0;//阴历今天下跌次数
        lunarYang = 0;//阴历今天阳线次数
        lunarYin = 0;//阴历今天阴线次数
        lunarHighOpen = 0;//阴历历高开次数
        lunarLowOpen = 0;//阴历低开次数
    }

    private void showData() {
        solarTodayNumTxt.post(new Runnable() {
            @Override
            public void run() {
                solarTodayNumTxt.setText(getString(R.string.solarTodayNum, solarNum));
                solarTodayRiseTxt.setText(getString(R.string.solarTodayRise, solarRise));
                solarTodayFallTxt.setText(getString(R.string.solarTodayFall, solarFall));
                solarTodayYangTxt.setText(getString(R.string.solarTodayYang, solarYang));
                solarTodayYinTxt.setText(getString(R.string.solarTodayYin, solarYin));
                solarHighOpenTxt.setText(getString(R.string.solarHighOpen, solarHighOpen));
                solarLowOpenTxt.setText(getString(R.string.solarLowOpen, solarLowOpen));


                lunarTodayNumTxt.setText(getString(R.string.lunarTodayNum, lunarNum));
                lunarTodayRiseTxt.setText(getString(R.string.lunarTodayRise, lunarRise));
                lunarTodayFallTxt.setText(getString(R.string.lunarTodayFall, lunarFall));
                lunarTodayYangTxt.setText(getString(R.string.lunarTodayYang, lunarYang));
                lunarTodayYinTxt.setText(getString(R.string.lunarTodayYin, lunarYin));
                lunarHighOpenTxt.setText(getString(R.string.lunarHighOpen, lunarHighOpen));
                lunarLowOpenTxt.setText(getString(R.string.lunarLowOpen, lunarLowOpen));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
