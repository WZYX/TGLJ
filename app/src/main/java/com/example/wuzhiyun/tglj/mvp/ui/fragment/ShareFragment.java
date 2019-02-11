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

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ShareFragment extends Fragment {
    View mView;
    @BindView(R.id.average)
    TextView averageTxt;
    @BindView(R.id.median)
    TextView medianTxt;
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
    @BindView(R.id.raise_fall_5)
    TextView raiseFall5Txt;
    @BindView(R.id.raise_fall_10)
    TextView raiseFall10Txt;
    @BindView(R.id.raise_fall_20)
    TextView raiseFall20Txt;
    @BindView(R.id.raise_fall_range_5)
    TextView raiseFallRange5Txt;
    @BindView(R.id.raise_fall_range_10)
    TextView raiseFallRange10Txt;
    @BindView(R.id.raise_fall_range_20)
    TextView raiseFallRange20Txt;
    @BindView(R.id.amplitude_range_5)
    TextView amplitudeRange5Txt;
    @BindView(R.id.amplitude_range_10)
    TextView amplitudeRange10Txt;
    @BindView(R.id.amplitude_range_20)
    TextView amplitudeRange20Txt;
    @BindView(R.id.change_rate_5)
    TextView changeRate5Txt;
    @BindView(R.id.change_rate_10)
    TextView changeRate10Txt;
    @BindView(R.id.change_rate_20)
    TextView changeRate20Txt;
    private String code;//股票代码
    private SparseArray<ShareRealm> arrayData;//k线数据
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_share, null);
            unbinder = ButterKnife.bind(this, mView);
            code = getArguments().getString("CODE");
            arrayData = new SparseArray<>();
            getData();
        }
        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void getData() {
        initData();
        if (!isZhiShu()) {
            //分价表。获取中位数、均价
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        Document doc = null;
                        String url = "http://quotes.money.163.com/trade/fjb_" + code + ".html";
                        doc = Jsoup.connect(url).get();
                        //中位数
                        Element table = doc.getElementsByClass("table_bg001 border_box table_sortable2").first();
                        if (table == null) {
                            return;
                        }
                        Elements trs = table.select("tbody").first().select("tr");
                        double turnover = 0;
                        long volume = 0;
                        long volumeIndex = 0;
                        for (int i = 0; i < trs.size(); i++) {
                            Elements tds = trs.get(i).select("td");
                            long volumeTem = Long.valueOf(tds.get(1).text().replaceAll(",", ""));
                            volume += volumeTem;
                            turnover += volumeTem * Double.valueOf(tds.get(0).text());
                        }
                        //均价
                        double averagePrice = turnover / volume;
                        mView.post(new Runnable() {
                            @Override
                            public void run() {
                                averageTxt.setText("均价：" + averagePrice);
                            }
                        });
                        //中位数
                        for (int i = 0; i < trs.size(); i++) {
                            Elements tds = trs.get(i).select("td");
                            volumeIndex += Long.valueOf(tds.get(1).text().replaceAll(",", ""));
                            if (volumeIndex >= volume / 2) {
                                double median = Double.valueOf(tds.get(0).text());
                                mView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        medianTxt.setText("中位数：" + median);
                                    }
                                });
                                break;
                            }
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        //k线数据
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
                Log.e("wuzhiyun" + code, "上一个交易日：" + yestoday);
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
                    analyzeK();
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
                            //网易个股
                            String url = (isZhiShu() ? "http://quotes.money.163.com/trade/lsjysj_zhishu_" :
                                    "http://quotes.money.163.com/trade/lsjysj_") + code + ".html?year=" + year + "&season=" + j;
                            //网易指数
//                            String url = "http://quotes.money.163.com/trade/lsjysj_zhishu_" + code + ".html?year=" + year + "&season=" + j;
                            //新浪
//                            String url = "http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/" + code + ".phtml?year=" + year + "&jidu=" + j;
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
//                        Element table = doc.getElementById("FundHoldSharesTable");//新浪id
                        //网易
                        Element table = doc.getElementsByClass("table_bg001 border_box limit_sale").first();
                        Log.e("wuzhiyun" + code, code + ":" + year + " " + j);
                        if (table == null) {
                            continue;
                        }
                        //新浪
//                        Elements trs = table.select("tr");
//                        ShareCodeName shareCodeName = realm.where(ShareCodeName.class).equalTo("code", code).findFirst();
//                        String text = trs.get(0).select("th").get(0).text();
//                        String shareName = text.substring(0, text.indexOf("("));
//                        if (shareCodeName == null || !shareName.equals(shareCodeName.getName())) {
//                            realm.executeTransaction(new Realm.Transaction() {
//                                @Override
//                                public void execute(Realm realm) {
//                                    ShareCodeName shareCodeName1 = new ShareCodeName();
//                                    shareCodeName1.setCode(code);
//                                    shareCodeName1.setName(shareName);
//                                    realm.copyToRealmOrUpdate(shareCodeName1);
//                                    EventBus.getDefault().post(shareCodeName1);
//
//                                }
//                            });
//                        }

                        ShareCodeName shareCodeName = realm.where(ShareCodeName.class).equalTo("code", code).findFirst();
                        String text = doc.getElementsByClass("title_01").first().text();
                        String shareName = text.substring(0, isZhiShu() ? text.indexOf(" ") : text.indexOf("("));
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
                        Element tbody = table.select("tbody").first();
                        if (tbody == null) {
                            break;
                        }
                        Elements trs = tbody.select("tr");

                        for (int i = 0; i < trs.size(); i++) {
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
                                shareK.setOpenPrice(Double.valueOf(tds.get(1).text().replaceAll(",", "")));
                                shareK.setMaxPrice(Double.valueOf(tds.get(2).text().replaceAll(",", "")));
                                shareK.setMinPrice(Double.valueOf(tds.get(3).text().replaceAll(",", "")));
                                shareK.setClosingPrice(Double.valueOf(tds.get(4).text().replaceAll(",", "")));
                                shareK.setVolume(Long.valueOf(tds.get(7).text().replaceAll(",", "").replace(".00", "")));
                                shareK.setTurnover(Double.valueOf(tds.get(8).text().replaceAll(",", "")));
                                if (!isZhiShu()) {
                                    shareK.setChangeRate(Double.valueOf(tds.get(10).text().replaceAll(",", "")));
                                }
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
//                        try {
//                            Thread.sleep(new Random().nextInt(5) * 1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
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

                if (calendar.get(Calendar.HOUR_OF_DAY) < 15) {
                    analyze(today);
                } else {
                    analyze(tomorrow);
                }
                analyzeK();
                realm.close();
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
    //相似涨跌；true为涨
    private boolean[] raiseFall5 = new boolean[5];
    private boolean[] raiseFall10 = new boolean[10];
    private boolean[] raiseFall20 = new boolean[20];
    private boolean[] raiseFall5Tem = new boolean[5];
    private boolean[] raiseFall10Tem = new boolean[10];
    private boolean[] raiseFall20Tem = new boolean[20];
    private int raiseFall5Index = 0;
    private int raiseFall10Index = 0;
    private int raiseFall20Index = 0;
    //相似涨跌幅；正为涨，负为跌；
    private int[] raiseFallRange5 = new int[5];
    private int[] raiseFallRange10 = new int[10];
    private int[] raiseFallRange20 = new int[20];
    private int[] raiseFallRange5Tem = new int[5];
    private int[] raiseFallRange10Tem = new int[10];
    private int[] raiseFallRange20Tem = new int[20];
    private int raiseFallRange5Index = 0;
    private int raiseFallRange10Index = 0;
    private int raiseFallRange20Index = 0;
    //相似振幅等级；
    private int[] amplitudeRange5 = new int[5];
    private int[] amplitudeRange10 = new int[10];
    private int[] amplitudeRange20 = new int[20];
    private int[] amplitudeRange5Tem = new int[5];
    private int[] amplitudeRange10Tem = new int[10];
    private int[] amplitudeRange20Tem = new int[20];
    private int amplitudeRange5Index = 0;
    private int amplitudeRange10Index = 0;
    private int amplitudeRange20Index = 0;
    //相似换手等级；
    private int[] changeRate5 = new int[5];
    private int[] changeRate10 = new int[10];
    private int[] changeRate20 = new int[20];
    private int[] changeRate5Tem = new int[5];
    private int[] changeRate10Tem = new int[10];
    private int[] changeRate20Tem = new int[20];
    private int changeRate5Index = 0;
    private int changeRate10Index = 0;
    private int changeRate20Index = 0;

    //寻找相似K线
    private void analyzeK() {
        initYinYang();
        initRaiseFall();
        initRaiseRangeFall();
        initAmplitude();
        if (!isZhiShu()) {
            initChangeRate();
        }

        for (int i = arrayData.size() - 1; i > 4; i--) {
            ShareRealm shareRealmI = arrayData.valueAt(i);
            ShareRealm shareRealmI1 = arrayData.valueAt(i - 1);
            compareYinYang(shareRealmI);
            compareRaiseFall(shareRealmI, shareRealmI1);
            compareRaiseFallRange(shareRealmI, shareRealmI1);
            compareAmplitudeRange(shareRealmI);
            if (!isZhiShu()) {
                compareChangeRateRange(shareRealmI);
            }

        }
    }

    //是否是指数
    private boolean isZhiShu() {
        return "000001".equals(code) || "399006".equals(code) || "399001".equals(code);
    }

    //振幅等级
    private int amplitudeRange(double range) {
        int result = 0;
        double leve1 = 3;
        double leve2 = 6;
        double leve3 = 9;
        double leve4 = 12;
        double leve_1 = -3;
        double leve_2 = -6;
        double leve_3 = -9;
        double leve_4 = -12;
        if (isZhiShu()) {
            leve1 = 1.5;
            leve2 = 3;
            leve3 = 4.5;
            leve4 = 6;
            leve_1 = -1.5;
            leve_2 = -3;
            leve_3 = -4.5;
            leve_4 = -6;
        }
        if (range > 0) {
            if (range < leve1) {
                result = 1;
            } else if (range < leve2) {
                result = 2;
            } else if (range < leve3) {
                result = 3;
            } else if (range < leve4) {
                result = 4;
            } else {
                result = 5;
            }
        } else {
            if (range > leve_1) {
                result = -1;
            } else if (range > leve_2) {
                result = -2;
            } else if (range > leve_3) {
                result = -3;
            } else if (range > leve_4) {
                result = -4;
            } else {
                result = -5;
            }

        }
        return result;
    }

    //换手率等级
    private int changeRateRange(double range) {
        int result = 0;
        double leve1 = 4;
        double leve2 = 8;
        double leve3 = 16;
        double leve4 = 32;
        double leve_1 = 2;
        double leve_2 = 1;
        double leve_3 = 0.5;
        double leve_4 = 0.25;
        if (range > 2) {
            if (range < leve1) {
                result = 1;
            } else if (range < leve2) {
                result = 2;
            } else if (range < leve3) {
                result = 3;
            } else if (range < leve4) {
                result = 4;
            } else {
                result = 5;
            }
        } else {
            if (range > leve_1) {
                result = -1;
            } else if (range > leve_2) {
                result = -2;
            } else if (range > leve_3) {
                result = -3;
            } else if (range > leve_4) {
                result = -4;
            } else {
                result = -5;
            }

        }
        return result;
    }


    //涨跌等级
    private int raiseFallRange(double range) {
        int result = 0;
        double leve1 = 2;
        double leve2 = 4;
        double leve3 = 6;
        double leve4 = 8;
        double leve_1 = -2;
        double leve_2 = -4;
        double leve_3 = -6;
        double leve_4 = -8;
        if (isZhiShu()) {
            leve1 = 0.5;
            leve2 = 1.5;
            leve3 = 2.5;
            leve4 = 3.5;
            leve_1 = -0.5;
            leve_2 = -1.5;
            leve_3 = -2.5;
            leve_4 = -3.5;
        }
        if (range > 0) {
            if (range < leve1) {
                result = 1;
            } else if (range < leve2) {
                result = 2;
            } else if (range < leve3) {
                result = 3;
            } else if (range < leve4) {
                result = 4;
            } else {
                result = 5;
            }
        } else {
            if (range > leve_1) {
                result = -1;
            } else if (range > leve_2) {
                result = -2;
            } else if (range > leve_3) {
                result = -3;
            } else if (range > leve_4) {
                result = -4;
            } else {
                result = -5;
            }

        }
        return result;
    }

    private void initAmplitude() {
        if (arrayData.size() >= 5) {
            changeRate5[4] = changeRateRange(arrayData.valueAt(0).getChangeRate());
            changeRate5[3] = changeRateRange(arrayData.valueAt(1).getChangeRate());
            changeRate5[2] = changeRateRange(arrayData.valueAt(2).getChangeRate());
            changeRate5[1] = changeRateRange(arrayData.valueAt(3).getChangeRate());
            changeRate5[0] = changeRateRange(arrayData.valueAt(4).getChangeRate());
        }
        if (arrayData.size() >= 10) {
            changeRate10[9] = changeRateRange(arrayData.valueAt(0).getChangeRate());
            changeRate10[8] = changeRateRange(arrayData.valueAt(1).getChangeRate());
            changeRate10[7] = changeRateRange(arrayData.valueAt(2).getChangeRate());
            changeRate10[6] = changeRateRange(arrayData.valueAt(3).getChangeRate());
            changeRate10[5] = changeRateRange(arrayData.valueAt(4).getChangeRate());
            changeRate10[4] = changeRateRange(arrayData.valueAt(5).getChangeRate());
            changeRate10[3] = changeRateRange(arrayData.valueAt(6).getChangeRate());
            changeRate10[2] = changeRateRange(arrayData.valueAt(7).getChangeRate());
            changeRate10[1] = changeRateRange(arrayData.valueAt(8).getChangeRate());
            changeRate10[0] = changeRateRange(arrayData.valueAt(9).getChangeRate());
        }
        if (arrayData.size() >= 20) {
            changeRate20[19] = changeRateRange(arrayData.valueAt(0).getChangeRate());
            changeRate20[18] = changeRateRange(arrayData.valueAt(1).getChangeRate());
            changeRate20[17] = changeRateRange(arrayData.valueAt(2).getChangeRate());
            changeRate20[16] = changeRateRange(arrayData.valueAt(3).getChangeRate());
            changeRate20[15] = changeRateRange(arrayData.valueAt(4).getChangeRate());
            changeRate20[14] = changeRateRange(arrayData.valueAt(5).getChangeRate());
            changeRate20[13] = changeRateRange(arrayData.valueAt(6).getChangeRate());
            changeRate20[12] = changeRateRange(arrayData.valueAt(7).getChangeRate());
            changeRate20[11] = changeRateRange(arrayData.valueAt(8).getChangeRate());
            changeRate20[10] = changeRateRange(arrayData.valueAt(9).getChangeRate());
            changeRate20[9] = changeRateRange(arrayData.valueAt(10).getChangeRate());
            changeRate20[8] = changeRateRange(arrayData.valueAt(11).getChangeRate());
            changeRate20[7] = changeRateRange(arrayData.valueAt(12).getChangeRate());
            changeRate20[6] = changeRateRange(arrayData.valueAt(13).getChangeRate());
            changeRate20[5] = changeRateRange(arrayData.valueAt(14).getChangeRate());
            changeRate20[4] = changeRateRange(arrayData.valueAt(15).getChangeRate());
            changeRate20[3] = changeRateRange(arrayData.valueAt(16).getChangeRate());
            changeRate20[2] = changeRateRange(arrayData.valueAt(17).getChangeRate());
            changeRate20[1] = changeRateRange(arrayData.valueAt(18).getChangeRate());
            changeRate20[0] = changeRateRange(arrayData.valueAt(19).getChangeRate());
        }
    }

    //连续振幅相同、相似
    private void compareAmplitudeRange(ShareRealm shareRealm) {
        amplitudeRange5Tem[amplitudeRange5Index] = amplitudeRange((shareRealm.getMaxPrice() - shareRealm.getMinPrice()) / shareRealm.getOpenPrice());
        amplitudeRange10Tem[amplitudeRange10Index] = amplitudeRange((shareRealm.getMaxPrice() - shareRealm.getMinPrice()) / shareRealm.getOpenPrice());
        amplitudeRange20Tem[amplitudeRange20Index] = amplitudeRange((shareRealm.getMaxPrice() - shareRealm.getMinPrice()) / shareRealm.getOpenPrice());
        if (amplitudeRange5Index == 4) {
            amplitudeRange5Index = 0;
        } else {
            amplitudeRange5Index++;
        }
        if (amplitudeRange10Index == 9) {
            amplitudeRange10Index = 0;
        } else {
            amplitudeRange10Index++;
        }
        if (amplitudeRange20Index == 19) {
            amplitudeRange20Index = 0;
        } else {
            amplitudeRange20Index++;
        }
        //连续5个交易日涨跌幅相同
        if (amplitudeRange5[0] == amplitudeRange5Tem[amplitudeRange5Index]
                && amplitudeRange5[1] == amplitudeRange5Tem[amplitudeRange5Index + 1 > 4 ? amplitudeRange5Index - 4 : amplitudeRange5Index + 1]
                && amplitudeRange5[2] == amplitudeRange5Tem[amplitudeRange5Index + 2 > 4 ? amplitudeRange5Index - 3 : amplitudeRange5Index + 2]
                && amplitudeRange5[3] == amplitudeRange5Tem[amplitudeRange5Index + 3 > 4 ? amplitudeRange5Index - 2 : amplitudeRange5Index + 3]
                && amplitudeRange5[4] == amplitudeRange5Tem[amplitudeRange5Index + 4 > 4 ? amplitudeRange5Index - 1 : amplitudeRange5Index + 4]) {
            String text = shareRealm.getDateYear() + shareRealm.getDate();
            if (TextUtils.isEmpty(amplitudeRange5Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        amplitudeRange5Txt.setText("连续5个交易日振幅相似：" + text);
                    }
                });

            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        amplitudeRange5Txt.setText(amplitudeRange5Txt.getText() + "、" + text);
                    }
                });

            }
        }
        //连续10个交易日涨跌幅相同
        if (amplitudeRange10[0] == amplitudeRange10Tem[amplitudeRange10Index]
                && amplitudeRange10[1] == amplitudeRange10Tem[amplitudeRange10Index + 1 > 9 ? amplitudeRange10Index - 9 : amplitudeRange10Index + 1]
                && amplitudeRange10[2] == amplitudeRange10Tem[amplitudeRange10Index + 2 > 9 ? amplitudeRange10Index - 8 : amplitudeRange10Index + 2]
                && amplitudeRange10[3] == amplitudeRange10Tem[amplitudeRange10Index + 3 > 9 ? amplitudeRange10Index - 7 : amplitudeRange10Index + 3]
                && amplitudeRange10[4] == amplitudeRange10Tem[amplitudeRange10Index + 4 > 9 ? amplitudeRange10Index - 6 : amplitudeRange10Index + 4]
                && amplitudeRange10[5] == amplitudeRange10Tem[amplitudeRange10Index + 5 > 9 ? amplitudeRange10Index - 5 : amplitudeRange10Index + 5]
                && amplitudeRange10[6] == amplitudeRange10Tem[amplitudeRange10Index + 6 > 9 ? amplitudeRange10Index - 4 : amplitudeRange10Index + 6]
                && amplitudeRange10[7] == amplitudeRange10Tem[amplitudeRange10Index + 7 > 9 ? amplitudeRange10Index - 3 : amplitudeRange10Index + 7]
                && amplitudeRange10[8] == amplitudeRange10Tem[amplitudeRange10Index + 8 > 9 ? amplitudeRange10Index - 2 : amplitudeRange10Index + 8]
                && amplitudeRange10[9] == amplitudeRange10Tem[amplitudeRange10Index + 9 > 9 ? amplitudeRange10Index - 1 : amplitudeRange10Index + 9]) {
            String text = shareRealm.getDateYear() + shareRealm.getDate();
            if (TextUtils.isEmpty(amplitudeRange10Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        amplitudeRange10Txt.setText("连续10个交易日振幅相似：" + text);
                    }
                });

            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        amplitudeRange10Txt.setText(amplitudeRange10Txt.getText() + "、" + text);
                    }
                });

            }

        }
        //连续20个交易日涨跌幅中16个相同
        int num = 0;
        if (amplitudeRange20[0] == amplitudeRange20Tem[amplitudeRange20Index]) {
            num++;
        }
        if (amplitudeRange20[1] == amplitudeRange20Tem[amplitudeRange20Index + 1 > 19 ? amplitudeRange20Index - 19 : amplitudeRange20Index + 1]) {
            num++;
        }
        if (amplitudeRange20[2] == amplitudeRange20Tem[amplitudeRange20Index + 2 > 19 ? amplitudeRange20Index - 18 : amplitudeRange20Index + 2]) {
            num++;
        }
        if (amplitudeRange20[3] == amplitudeRange20Tem[amplitudeRange20Index + 3 > 19 ? amplitudeRange20Index - 17 : amplitudeRange20Index + 3]) {
            num++;
        }
        if (amplitudeRange20[4] == amplitudeRange20Tem[amplitudeRange20Index + 4 > 19 ? amplitudeRange20Index - 16 : amplitudeRange20Index + 4]) {
            num++;
        }
        if (amplitudeRange20[5] == amplitudeRange20Tem[amplitudeRange20Index + 5 > 19 ? amplitudeRange20Index - 15 : amplitudeRange20Index + 5]) {
            num++;
        }
        if (amplitudeRange20[6] == amplitudeRange20Tem[amplitudeRange20Index + 6 > 19 ? amplitudeRange20Index - 14 : amplitudeRange20Index + 6]) {
            num++;
        }
        if (amplitudeRange20[7] == amplitudeRange20Tem[amplitudeRange20Index + 7 > 19 ? amplitudeRange20Index - 13 : amplitudeRange20Index + 7]) {
            num++;
        }
        if (amplitudeRange20[8] == amplitudeRange20Tem[amplitudeRange20Index + 8 > 19 ? amplitudeRange20Index - 12 : amplitudeRange20Index + 8]) {
            num++;
        }
        if (amplitudeRange20[9] == amplitudeRange20Tem[amplitudeRange20Index + 9 > 19 ? amplitudeRange20Index - 11 : amplitudeRange20Index + 9]) {
            num++;
        }
        if (amplitudeRange20[10] == amplitudeRange20Tem[amplitudeRange20Index + 10 > 19 ? amplitudeRange20Index - 10 : amplitudeRange20Index + 10]) {
            num++;
        }
        if (amplitudeRange20[11] == amplitudeRange20Tem[amplitudeRange20Index + 11 > 19 ? amplitudeRange20Index - 9 : amplitudeRange20Index + 11]) {
            num++;
        }
        if (amplitudeRange20[12] == amplitudeRange20Tem[amplitudeRange20Index + 12 > 19 ? amplitudeRange20Index - 8 : amplitudeRange20Index + 12]) {
            num++;
        }
        if (amplitudeRange20[13] == amplitudeRange20Tem[amplitudeRange20Index + 13 > 19 ? amplitudeRange20Index - 7 : amplitudeRange20Index + 13]) {
            num++;
        }
        if (amplitudeRange20[14] == amplitudeRange20Tem[amplitudeRange20Index + 14 > 19 ? amplitudeRange20Index - 6 : amplitudeRange20Index + 14]) {
            num++;
        }
        if (amplitudeRange20[15] == amplitudeRange20Tem[amplitudeRange20Index + 15 > 19 ? amplitudeRange20Index - 5 : amplitudeRange20Index + 15]) {
            num++;
        }
        if (amplitudeRange20[16] == amplitudeRange20Tem[amplitudeRange20Index + 16 > 19 ? amplitudeRange20Index - 4 : amplitudeRange20Index + 16]) {
            num++;
        }
        if (amplitudeRange20[17] == amplitudeRange20Tem[amplitudeRange20Index + 17 > 19 ? amplitudeRange20Index - 3 : amplitudeRange20Index + 17]) {
            num++;
        }
        if (amplitudeRange20[18] == amplitudeRange20Tem[amplitudeRange20Index + 18 > 19 ? amplitudeRange20Index - 2 : amplitudeRange20Index + 18]) {
            num++;
        }
        if (amplitudeRange20[19] == amplitudeRange20Tem[amplitudeRange20Index + 19 > 19 ? amplitudeRange20Index - 1 : amplitudeRange20Index + 19]) {
            num++;
        }
        if (num >= 16) {
            String text = shareRealm.getDateYear() + shareRealm.getDate();
            if (TextUtils.isEmpty(amplitudeRange20Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        amplitudeRange20Txt.setText("连续20个交易日16个振幅相似：" + text);
                    }
                });
            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        amplitudeRange20Txt.setText(amplitudeRange20Txt.getText() + "、" + text);
                    }
                });
            }
        }
    }

    private void initChangeRate() {
        if (arrayData.size() >= 5) {
            amplitudeRange5[4] = changeRateRange(arrayData.valueAt(0).getChangeRate());
            amplitudeRange5[3] = changeRateRange(arrayData.valueAt(1).getChangeRate());
            amplitudeRange5[2] = changeRateRange(arrayData.valueAt(2).getChangeRate());
            amplitudeRange5[1] = changeRateRange(arrayData.valueAt(3).getChangeRate());
            amplitudeRange5[0] = changeRateRange(arrayData.valueAt(4).getChangeRate());
        }
        if (arrayData.size() >= 10) {
            amplitudeRange10[9] = changeRateRange(arrayData.valueAt(0).getChangeRate());
            amplitudeRange10[8] = changeRateRange(arrayData.valueAt(1).getChangeRate());
            amplitudeRange10[7] = changeRateRange(arrayData.valueAt(2).getChangeRate());
            amplitudeRange10[6] = changeRateRange(arrayData.valueAt(3).getChangeRate());
            amplitudeRange10[5] = changeRateRange(arrayData.valueAt(4).getChangeRate());
            amplitudeRange10[4] = changeRateRange(arrayData.valueAt(5).getChangeRate());
            amplitudeRange10[3] = changeRateRange(arrayData.valueAt(6).getChangeRate());
            amplitudeRange10[2] = changeRateRange(arrayData.valueAt(7).getChangeRate());
            amplitudeRange10[1] = changeRateRange(arrayData.valueAt(8).getChangeRate());
            amplitudeRange10[0] = changeRateRange(arrayData.valueAt(9).getChangeRate());
        }
        if (arrayData.size() >= 20) {
            amplitudeRange20[19] = changeRateRange(arrayData.valueAt(0).getChangeRate());
            amplitudeRange20[18] = changeRateRange(arrayData.valueAt(1).getChangeRate());
            amplitudeRange20[17] = changeRateRange(arrayData.valueAt(2).getChangeRate());
            amplitudeRange20[16] = changeRateRange(arrayData.valueAt(3).getChangeRate());
            amplitudeRange20[15] = changeRateRange(arrayData.valueAt(4).getChangeRate());
            amplitudeRange20[14] = changeRateRange(arrayData.valueAt(5).getChangeRate());
            amplitudeRange20[13] = changeRateRange(arrayData.valueAt(6).getChangeRate());
            amplitudeRange20[12] = changeRateRange(arrayData.valueAt(7).getChangeRate());
            amplitudeRange20[11] = changeRateRange(arrayData.valueAt(8).getChangeRate());
            amplitudeRange20[10] = changeRateRange(arrayData.valueAt(9).getChangeRate());
            amplitudeRange20[9] = changeRateRange(arrayData.valueAt(10).getChangeRate());
            amplitudeRange20[8] = changeRateRange(arrayData.valueAt(11).getChangeRate());
            amplitudeRange20[7] = changeRateRange(arrayData.valueAt(12).getChangeRate());
            amplitudeRange20[6] = changeRateRange(arrayData.valueAt(13).getChangeRate());
            amplitudeRange20[5] = changeRateRange(arrayData.valueAt(14).getChangeRate());
            amplitudeRange20[4] = changeRateRange(arrayData.valueAt(15).getChangeRate());
            amplitudeRange20[3] = changeRateRange(arrayData.valueAt(16).getChangeRate());
            amplitudeRange20[2] = changeRateRange(arrayData.valueAt(17).getChangeRate());
            amplitudeRange20[1] = changeRateRange(arrayData.valueAt(18).getChangeRate());
            amplitudeRange20[0] = changeRateRange(arrayData.valueAt(19).getChangeRate());
        }
    }

    //连续换手率相似
    private void compareChangeRateRange(ShareRealm shareRealm) {
        changeRate5Tem[changeRate5Index] = changeRateRange(shareRealm.getChangeRate());
        changeRate10Tem[changeRate10Index] = changeRateRange(shareRealm.getChangeRate());
        changeRate20Tem[changeRate20Index] = changeRateRange(shareRealm.getChangeRate());
        if (changeRate5Index == 4) {
            changeRate5Index = 0;
        } else {
            changeRate5Index++;
        }
        if (changeRate10Index == 9) {
            changeRate10Index = 0;
        } else {
            changeRate10Index++;
        }
        if (changeRate20Index == 19) {
            changeRate20Index = 0;
        } else {
            changeRate20Index++;
        }
        //连续5个交易日换手率相似
        if (changeRate5[0] == changeRate5Tem[changeRate5Index]
                && changeRate5[1] == changeRate5Tem[changeRate5Index + 1 > 4 ? changeRate5Index - 4 : changeRate5Index + 1]
                && changeRate5[2] == changeRate5Tem[changeRate5Index + 2 > 4 ? changeRate5Index - 3 : changeRate5Index + 2]
                && changeRate5[3] == changeRate5Tem[changeRate5Index + 3 > 4 ? changeRate5Index - 2 : changeRate5Index + 3]
                && changeRate5[4] == changeRate5Tem[changeRate5Index + 4 > 4 ? changeRate5Index - 1 : changeRate5Index + 4]) {
            String text = shareRealm.getDateYear() + shareRealm.getDate();
            if (TextUtils.isEmpty(changeRate5Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        changeRate5Txt.setText("连续5个交易日换手率相似：" + text);
                    }
                });

            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        changeRate5Txt.setText(changeRate5Txt.getText() + "、" + text);
                    }
                });

            }
        }
        //连续10个交易日换手率相似
        if (changeRate10[0] == changeRate10Tem[changeRate10Index]
                && changeRate10[1] == changeRate10Tem[changeRate10Index + 1 > 9 ? changeRate10Index - 9 : changeRate10Index + 1]
                && changeRate10[2] == changeRate10Tem[changeRate10Index + 2 > 9 ? changeRate10Index - 8 : changeRate10Index + 2]
                && changeRate10[3] == changeRate10Tem[changeRate10Index + 3 > 9 ? changeRate10Index - 7 : changeRate10Index + 3]
                && changeRate10[4] == changeRate10Tem[changeRate10Index + 4 > 9 ? changeRate10Index - 6 : changeRate10Index + 4]
                && changeRate10[5] == changeRate10Tem[changeRate10Index + 5 > 9 ? changeRate10Index - 5 : changeRate10Index + 5]
                && changeRate10[6] == changeRate10Tem[changeRate10Index + 6 > 9 ? changeRate10Index - 4 : changeRate10Index + 6]
                && changeRate10[7] == changeRate10Tem[changeRate10Index + 7 > 9 ? changeRate10Index - 3 : changeRate10Index + 7]
                && changeRate10[8] == changeRate10Tem[changeRate10Index + 8 > 9 ? changeRate10Index - 2 : changeRate10Index + 8]
                && changeRate10[9] == changeRate10Tem[changeRate10Index + 9 > 9 ? changeRate10Index - 1 : changeRate10Index + 9]) {
            String text = shareRealm.getDateYear() + shareRealm.getDate();
            if (TextUtils.isEmpty(changeRate10Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        changeRate10Txt.setText("连续10个交易日换手率相似：" + text);
                    }
                });

            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        changeRate10Txt.setText(changeRate10Txt.getText() + "、" + text);
                    }
                });

            }

        }
        //连续20个交易日中16个换手率相似
        int num = 0;
        if (changeRate20[0] == changeRate20Tem[changeRate20Index]) {
            num++;
        }
        if (changeRate20[1] == changeRate20Tem[changeRate20Index + 1 > 19 ? changeRate20Index - 19 : changeRate20Index + 1]) {
            num++;
        }
        if (changeRate20[2] == changeRate20Tem[changeRate20Index + 2 > 19 ? changeRate20Index - 18 : changeRate20Index + 2]) {
            num++;
        }
        if (changeRate20[3] == changeRate20Tem[changeRate20Index + 3 > 19 ? changeRate20Index - 17 : changeRate20Index + 3]) {
            num++;
        }
        if (changeRate20[4] == changeRate20Tem[changeRate20Index + 4 > 19 ? changeRate20Index - 16 : changeRate20Index + 4]) {
            num++;
        }
        if (changeRate20[5] == changeRate20Tem[changeRate20Index + 5 > 19 ? changeRate20Index - 15 : changeRate20Index + 5]) {
            num++;
        }
        if (changeRate20[6] == changeRate20Tem[changeRate20Index + 6 > 19 ? changeRate20Index - 14 : changeRate20Index + 6]) {
            num++;
        }
        if (changeRate20[7] == changeRate20Tem[changeRate20Index + 7 > 19 ? changeRate20Index - 13 : changeRate20Index + 7]) {
            num++;
        }
        if (changeRate20[8] == changeRate20Tem[changeRate20Index + 8 > 19 ? changeRate20Index - 12 : changeRate20Index + 8]) {
            num++;
        }
        if (changeRate20[9] == changeRate20Tem[changeRate20Index + 9 > 19 ? changeRate20Index - 11 : changeRate20Index + 9]) {
            num++;
        }
        if (changeRate20[10] == changeRate20Tem[changeRate20Index + 10 > 19 ? changeRate20Index - 10 : changeRate20Index + 10]) {
            num++;
        }
        if (changeRate20[11] == changeRate20Tem[changeRate20Index + 11 > 19 ? changeRate20Index - 9 : changeRate20Index + 11]) {
            num++;
        }
        if (changeRate20[12] == changeRate20Tem[changeRate20Index + 12 > 19 ? changeRate20Index - 8 : changeRate20Index + 12]) {
            num++;
        }
        if (changeRate20[13] == changeRate20Tem[changeRate20Index + 13 > 19 ? changeRate20Index - 7 : changeRate20Index + 13]) {
            num++;
        }
        if (changeRate20[14] == changeRate20Tem[changeRate20Index + 14 > 19 ? changeRate20Index - 6 : changeRate20Index + 14]) {
            num++;
        }
        if (changeRate20[15] == changeRate20Tem[changeRate20Index + 15 > 19 ? changeRate20Index - 5 : changeRate20Index + 15]) {
            num++;
        }
        if (changeRate20[16] == changeRate20Tem[changeRate20Index + 16 > 19 ? changeRate20Index - 4 : changeRate20Index + 16]) {
            num++;
        }
        if (changeRate20[17] == changeRate20Tem[changeRate20Index + 17 > 19 ? changeRate20Index - 3 : changeRate20Index + 17]) {
            num++;
        }
        if (changeRate20[18] == changeRate20Tem[changeRate20Index + 18 > 19 ? changeRate20Index - 2 : changeRate20Index + 18]) {
            num++;
        }
        if (changeRate20[19] == changeRate20Tem[changeRate20Index + 19 > 19 ? changeRate20Index - 1 : changeRate20Index + 19]) {
            num++;
        }
        if (num >= 16) {
            String text = shareRealm.getDateYear() + shareRealm.getDate();
            if (TextUtils.isEmpty(changeRate20Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        changeRate20Txt.setText("连续20个交易日16个换手率相似：" + text);
                    }
                });

            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        changeRate20Txt.setText(changeRate20Txt.getText() + "、" + text);
                    }
                });

            }
        }
    }

    private void initRaiseRangeFall() {
        if (arrayData.size() >= 6) {
            raiseFallRange5[4] = raiseFallRange((arrayData.valueAt(0).getClosingPrice() - arrayData.valueAt(1).getClosingPrice()) / arrayData.valueAt(1).getClosingPrice());
            raiseFallRange5[3] = raiseFallRange((arrayData.valueAt(1).getClosingPrice() - arrayData.valueAt(2).getClosingPrice()) / arrayData.valueAt(2).getClosingPrice());
            raiseFallRange5[2] = raiseFallRange((arrayData.valueAt(2).getClosingPrice() - arrayData.valueAt(3).getClosingPrice()) / arrayData.valueAt(3).getClosingPrice());
            raiseFallRange5[1] = raiseFallRange((arrayData.valueAt(3).getClosingPrice() - arrayData.valueAt(4).getClosingPrice()) / arrayData.valueAt(4).getClosingPrice());
            raiseFallRange5[0] = raiseFallRange((arrayData.valueAt(4).getClosingPrice() - arrayData.valueAt(5).getClosingPrice()) / arrayData.valueAt(5).getClosingPrice());
        }
        if (arrayData.size() >= 11) {
            raiseFallRange10[9] = raiseFallRange((arrayData.valueAt(0).getClosingPrice() - arrayData.valueAt(1).getClosingPrice()) / arrayData.valueAt(1).getClosingPrice());
            raiseFallRange10[8] = raiseFallRange((arrayData.valueAt(1).getClosingPrice() - arrayData.valueAt(2).getClosingPrice()) / arrayData.valueAt(2).getClosingPrice());
            raiseFallRange10[7] = raiseFallRange((arrayData.valueAt(2).getClosingPrice() - arrayData.valueAt(3).getClosingPrice()) / arrayData.valueAt(3).getClosingPrice());
            raiseFallRange10[6] = raiseFallRange((arrayData.valueAt(3).getClosingPrice() - arrayData.valueAt(4).getClosingPrice()) / arrayData.valueAt(4).getClosingPrice());
            raiseFallRange10[5] = raiseFallRange((arrayData.valueAt(4).getClosingPrice() - arrayData.valueAt(5).getClosingPrice()) / arrayData.valueAt(5).getClosingPrice());
            raiseFallRange10[4] = raiseFallRange((arrayData.valueAt(5).getClosingPrice() - arrayData.valueAt(6).getClosingPrice()) / arrayData.valueAt(6).getClosingPrice());
            raiseFallRange10[3] = raiseFallRange((arrayData.valueAt(6).getClosingPrice() - arrayData.valueAt(7).getClosingPrice()) / arrayData.valueAt(7).getClosingPrice());
            raiseFallRange10[2] = raiseFallRange((arrayData.valueAt(7).getClosingPrice() - arrayData.valueAt(8).getClosingPrice()) / arrayData.valueAt(8).getClosingPrice());
            raiseFallRange10[1] = raiseFallRange((arrayData.valueAt(8).getClosingPrice() - arrayData.valueAt(9).getClosingPrice()) / arrayData.valueAt(9).getClosingPrice());
            raiseFallRange10[0] = raiseFallRange((arrayData.valueAt(9).getClosingPrice() - arrayData.valueAt(10).getClosingPrice()) / arrayData.valueAt(10).getClosingPrice());
        }
        if (arrayData.size() >= 21) {
            raiseFallRange20[19] = raiseFallRange((arrayData.valueAt(0).getClosingPrice() - arrayData.valueAt(1).getClosingPrice()) / arrayData.valueAt(1).getClosingPrice());
            raiseFallRange20[18] = raiseFallRange((arrayData.valueAt(1).getClosingPrice() - arrayData.valueAt(2).getClosingPrice()) / arrayData.valueAt(2).getClosingPrice());
            raiseFallRange20[17] = raiseFallRange((arrayData.valueAt(2).getClosingPrice() - arrayData.valueAt(3).getClosingPrice()) / arrayData.valueAt(3).getClosingPrice());
            raiseFallRange20[16] = raiseFallRange((arrayData.valueAt(3).getClosingPrice() - arrayData.valueAt(4).getClosingPrice()) / arrayData.valueAt(4).getClosingPrice());
            raiseFallRange20[15] = raiseFallRange((arrayData.valueAt(4).getClosingPrice() - arrayData.valueAt(5).getClosingPrice()) / arrayData.valueAt(5).getClosingPrice());
            raiseFallRange20[14] = raiseFallRange((arrayData.valueAt(5).getClosingPrice() - arrayData.valueAt(6).getClosingPrice()) / arrayData.valueAt(6).getClosingPrice());
            raiseFallRange20[13] = raiseFallRange((arrayData.valueAt(6).getClosingPrice() - arrayData.valueAt(7).getClosingPrice()) / arrayData.valueAt(7).getClosingPrice());
            raiseFallRange20[12] = raiseFallRange((arrayData.valueAt(7).getClosingPrice() - arrayData.valueAt(8).getClosingPrice()) / arrayData.valueAt(8).getClosingPrice());
            raiseFallRange20[11] = raiseFallRange((arrayData.valueAt(8).getClosingPrice() - arrayData.valueAt(9).getClosingPrice()) / arrayData.valueAt(9).getClosingPrice());
            raiseFallRange20[10] = raiseFallRange((arrayData.valueAt(9).getClosingPrice() - arrayData.valueAt(10).getClosingPrice()) / arrayData.valueAt(10).getClosingPrice());
            raiseFallRange20[9] = raiseFallRange((arrayData.valueAt(10).getClosingPrice() - arrayData.valueAt(11).getClosingPrice()) / arrayData.valueAt(11).getClosingPrice());
            raiseFallRange20[8] = raiseFallRange((arrayData.valueAt(11).getClosingPrice() - arrayData.valueAt(12).getClosingPrice()) / arrayData.valueAt(12).getClosingPrice());
            raiseFallRange20[7] = raiseFallRange((arrayData.valueAt(12).getClosingPrice() - arrayData.valueAt(13).getClosingPrice()) / arrayData.valueAt(13).getClosingPrice());
            raiseFallRange20[6] = raiseFallRange((arrayData.valueAt(13).getClosingPrice() - arrayData.valueAt(14).getClosingPrice()) / arrayData.valueAt(14).getClosingPrice());
            raiseFallRange20[5] = raiseFallRange((arrayData.valueAt(14).getClosingPrice() - arrayData.valueAt(15).getClosingPrice()) / arrayData.valueAt(15).getClosingPrice());
            raiseFallRange20[4] = raiseFallRange((arrayData.valueAt(15).getClosingPrice() - arrayData.valueAt(16).getClosingPrice()) / arrayData.valueAt(16).getClosingPrice());
            raiseFallRange20[3] = raiseFallRange((arrayData.valueAt(16).getClosingPrice() - arrayData.valueAt(17).getClosingPrice()) / arrayData.valueAt(17).getClosingPrice());
            raiseFallRange20[2] = raiseFallRange((arrayData.valueAt(17).getClosingPrice() - arrayData.valueAt(18).getClosingPrice()) / arrayData.valueAt(18).getClosingPrice());
            raiseFallRange20[1] = raiseFallRange((arrayData.valueAt(18).getClosingPrice() - arrayData.valueAt(19).getClosingPrice()) / arrayData.valueAt(19).getClosingPrice());
            raiseFallRange20[0] = raiseFallRange((arrayData.valueAt(19).getClosingPrice() - arrayData.valueAt(20).getClosingPrice()) / arrayData.valueAt(20).getClosingPrice());
        }
    }

    //连续涨跌幅相同、相似
    private void compareRaiseFallRange(ShareRealm shareRealm, ShareRealm shareRealm1) {
        raiseFallRange5Tem[raiseFallRange5Index] = raiseFallRange((shareRealm1.getClosingPrice() - shareRealm.getClosingPrice()) / shareRealm.getClosingPrice());
        raiseFallRange10Tem[raiseFallRange10Index] = raiseFallRange((shareRealm1.getClosingPrice() - shareRealm.getClosingPrice()) / shareRealm.getClosingPrice());
        raiseFallRange20Tem[raiseFallRange20Index] = raiseFallRange((shareRealm1.getClosingPrice() - shareRealm.getClosingPrice()) / shareRealm.getClosingPrice());
        if (raiseFallRange5Index == 4) {
            raiseFallRange5Index = 0;
        } else {
            raiseFallRange5Index++;
        }
        if (raiseFallRange10Index == 9) {
            raiseFallRange10Index = 0;
        } else {
            raiseFallRange10Index++;
        }
        if (raiseFallRange20Index == 19) {
            raiseFallRange20Index = 0;
        } else {
            raiseFallRange20Index++;
        }
        //连续5个交易日涨跌幅相同
        if (raiseFallRange5[0] == raiseFallRange5Tem[raiseFallRange5Index]
                && raiseFallRange5[1] == raiseFallRange5Tem[raiseFallRange5Index + 1 > 4 ? raiseFallRange5Index - 4 : raiseFallRange5Index + 1]
                && raiseFallRange5[2] == raiseFallRange5Tem[raiseFallRange5Index + 2 > 4 ? raiseFallRange5Index - 3 : raiseFallRange5Index + 2]
                && raiseFallRange5[3] == raiseFallRange5Tem[raiseFallRange5Index + 3 > 4 ? raiseFallRange5Index - 2 : raiseFallRange5Index + 3]
                && raiseFallRange5[4] == raiseFallRange5Tem[raiseFallRange5Index + 4 > 4 ? raiseFallRange5Index - 1 : raiseFallRange5Index + 4]) {
            String text = shareRealm1.getDateYear() + shareRealm1.getDate();
            if (TextUtils.isEmpty(raiseFallRange5Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        raiseFallRange5Txt.setText("连续5个交易日涨跌幅相似：" + text);
                    }
                });

            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        raiseFallRange5Txt.setText(raiseFallRange5Txt.getText() + "、" + text);
                    }
                });

            }
        }
        //连续10个交易日涨跌幅相同
        if (raiseFallRange10[0] == raiseFallRange10Tem[raiseFallRange10Index]
                && raiseFallRange10[1] == raiseFallRange10Tem[raiseFallRange10Index + 1 > 9 ? raiseFallRange10Index - 9 : raiseFallRange10Index + 1]
                && raiseFallRange10[2] == raiseFallRange10Tem[raiseFallRange10Index + 2 > 9 ? raiseFallRange10Index - 8 : raiseFallRange10Index + 2]
                && raiseFallRange10[3] == raiseFallRange10Tem[raiseFallRange10Index + 3 > 9 ? raiseFallRange10Index - 7 : raiseFallRange10Index + 3]
                && raiseFallRange10[4] == raiseFallRange10Tem[raiseFallRange10Index + 4 > 9 ? raiseFallRange10Index - 6 : raiseFallRange10Index + 4]
                && raiseFallRange10[5] == raiseFallRange10Tem[raiseFallRange10Index + 5 > 9 ? raiseFallRange10Index - 5 : raiseFallRange10Index + 5]
                && raiseFallRange10[6] == raiseFallRange10Tem[raiseFallRange10Index + 6 > 9 ? raiseFallRange10Index - 4 : raiseFallRange10Index + 6]
                && raiseFallRange10[7] == raiseFallRange10Tem[raiseFallRange10Index + 7 > 9 ? raiseFallRange10Index - 3 : raiseFallRange10Index + 7]
                && raiseFallRange10[8] == raiseFallRange10Tem[raiseFallRange10Index + 8 > 9 ? raiseFallRange10Index - 2 : raiseFallRange10Index + 8]
                && raiseFallRange10[9] == raiseFallRange10Tem[raiseFallRange10Index + 9 > 9 ? raiseFallRange10Index - 1 : raiseFallRange10Index + 9]) {
            String text = shareRealm1.getDateYear() + shareRealm1.getDate();
            if (TextUtils.isEmpty(raiseFallRange10Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        raiseFallRange10Txt.setText("连续10个交易日涨跌幅相似：" + text);
                    }
                });

            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        raiseFallRange10Txt.setText(raiseFallRange10Txt.getText() + "、" + text);
                    }
                });

            }

        }
        //连续20个交易日涨跌幅中16个相同
        int num = 0;
        if (raiseFallRange20[0] == raiseFallRange20Tem[raiseFallRange20Index]) {
            num++;
        }
        if (raiseFallRange20[1] == raiseFallRange20Tem[raiseFallRange20Index + 1 > 19 ? raiseFallRange20Index - 19 : raiseFallRange20Index + 1]) {
            num++;
        }
        if (raiseFallRange20[2] == raiseFallRange20Tem[raiseFallRange20Index + 2 > 19 ? raiseFallRange20Index - 18 : raiseFallRange20Index + 2]) {
            num++;
        }
        if (raiseFallRange20[3] == raiseFallRange20Tem[raiseFallRange20Index + 3 > 19 ? raiseFallRange20Index - 17 : raiseFallRange20Index + 3]) {
            num++;
        }
        if (raiseFallRange20[4] == raiseFallRange20Tem[raiseFallRange20Index + 4 > 19 ? raiseFallRange20Index - 16 : raiseFallRange20Index + 4]) {
            num++;
        }
        if (raiseFallRange20[5] == raiseFallRange20Tem[raiseFallRange20Index + 5 > 19 ? raiseFallRange20Index - 15 : raiseFallRange20Index + 5]) {
            num++;
        }
        if (raiseFallRange20[6] == raiseFallRange20Tem[raiseFallRange20Index + 6 > 19 ? raiseFallRange20Index - 14 : raiseFallRange20Index + 6]) {
            num++;
        }
        if (raiseFallRange20[7] == raiseFallRange20Tem[raiseFallRange20Index + 7 > 19 ? raiseFallRange20Index - 13 : raiseFallRange20Index + 7]) {
            num++;
        }
        if (raiseFallRange20[8] == raiseFallRange20Tem[raiseFallRange20Index + 8 > 19 ? raiseFallRange20Index - 12 : raiseFallRange20Index + 8]) {
            num++;
        }
        if (raiseFallRange20[9] == raiseFallRange20Tem[raiseFallRange20Index + 9 > 19 ? raiseFallRange20Index - 11 : raiseFallRange20Index + 9]) {
            num++;
        }
        if (raiseFallRange20[10] == raiseFallRange20Tem[raiseFallRange20Index + 10 > 19 ? raiseFallRange20Index - 10 : raiseFallRange20Index + 10]) {
            num++;
        }
        if (raiseFallRange20[11] == raiseFallRange20Tem[raiseFallRange20Index + 11 > 19 ? raiseFallRange20Index - 9 : raiseFallRange20Index + 11]) {
            num++;
        }
        if (raiseFallRange20[12] == raiseFallRange20Tem[raiseFallRange20Index + 12 > 19 ? raiseFallRange20Index - 8 : raiseFallRange20Index + 12]) {
            num++;
        }
        if (raiseFallRange20[13] == raiseFallRange20Tem[raiseFallRange20Index + 13 > 19 ? raiseFallRange20Index - 7 : raiseFallRange20Index + 13]) {
            num++;
        }
        if (raiseFallRange20[14] == raiseFallRange20Tem[raiseFallRange20Index + 14 > 19 ? raiseFallRange20Index - 6 : raiseFallRange20Index + 14]) {
            num++;
        }
        if (raiseFallRange20[15] == raiseFallRange20Tem[raiseFallRange20Index + 15 > 19 ? raiseFallRange20Index - 5 : raiseFallRange20Index + 15]) {
            num++;
        }
        if (raiseFallRange20[16] == raiseFallRange20Tem[raiseFallRange20Index + 16 > 19 ? raiseFallRange20Index - 4 : raiseFallRange20Index + 16]) {
            num++;
        }
        if (raiseFallRange20[17] == raiseFallRange20Tem[raiseFallRange20Index + 17 > 19 ? raiseFallRange20Index - 3 : raiseFallRange20Index + 17]) {
            num++;
        }
        if (raiseFallRange20[18] == raiseFallRange20Tem[raiseFallRange20Index + 18 > 19 ? raiseFallRange20Index - 2 : raiseFallRange20Index + 18]) {
            num++;
        }
        if (raiseFallRange20[19] == raiseFallRange20Tem[raiseFallRange20Index + 19 > 19 ? raiseFallRange20Index - 1 : raiseFallRange20Index + 19]) {
            num++;
        }
        if (num >= 16) {
            String text = shareRealm1.getDateYear() + shareRealm1.getDate();
            if (TextUtils.isEmpty(raiseFallRange20Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        raiseFallRange20Txt.setText("连续20个交易日16个涨跌幅相似：" + text);
                    }
                });

            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        raiseFallRange20Txt.setText(raiseFallRange20Txt.getText() + "、" + text);
                    }
                });

            }
        }
    }

    private void initRaiseFall() {
        if (arrayData.size() >= 6) {
            raiseFall5[4] = arrayData.valueAt(0).getClosingPrice() > arrayData.valueAt(1).getClosingPrice();
            raiseFall5[3] = arrayData.valueAt(1).getClosingPrice() > arrayData.valueAt(2).getClosingPrice();
            raiseFall5[2] = arrayData.valueAt(2).getClosingPrice() > arrayData.valueAt(3).getClosingPrice();
            raiseFall5[1] = arrayData.valueAt(3).getClosingPrice() > arrayData.valueAt(4).getClosingPrice();
            raiseFall5[0] = arrayData.valueAt(4).getClosingPrice() > arrayData.valueAt(5).getClosingPrice();
        }
        if (arrayData.size() >= 11) {
            raiseFall10[9] = arrayData.valueAt(0).getClosingPrice() > arrayData.valueAt(1).getClosingPrice();
            raiseFall10[8] = arrayData.valueAt(1).getClosingPrice() > arrayData.valueAt(2).getClosingPrice();
            raiseFall10[7] = arrayData.valueAt(2).getClosingPrice() > arrayData.valueAt(3).getClosingPrice();
            raiseFall10[6] = arrayData.valueAt(3).getClosingPrice() > arrayData.valueAt(4).getClosingPrice();
            raiseFall10[5] = arrayData.valueAt(4).getClosingPrice() > arrayData.valueAt(5).getClosingPrice();
            raiseFall10[4] = arrayData.valueAt(5).getClosingPrice() > arrayData.valueAt(6).getClosingPrice();
            raiseFall10[3] = arrayData.valueAt(6).getClosingPrice() > arrayData.valueAt(7).getClosingPrice();
            raiseFall10[2] = arrayData.valueAt(7).getClosingPrice() > arrayData.valueAt(8).getClosingPrice();
            raiseFall10[1] = arrayData.valueAt(8).getClosingPrice() > arrayData.valueAt(9).getClosingPrice();
            raiseFall10[0] = arrayData.valueAt(9).getClosingPrice() > arrayData.valueAt(10).getClosingPrice();
        }
        if (arrayData.size() >= 21) {
            raiseFall20[19] = arrayData.valueAt(0).getClosingPrice() > arrayData.valueAt(1).getClosingPrice();
            raiseFall20[18] = arrayData.valueAt(1).getClosingPrice() > arrayData.valueAt(2).getClosingPrice();
            raiseFall20[17] = arrayData.valueAt(2).getClosingPrice() > arrayData.valueAt(3).getClosingPrice();
            raiseFall20[16] = arrayData.valueAt(3).getClosingPrice() > arrayData.valueAt(4).getClosingPrice();
            raiseFall20[15] = arrayData.valueAt(4).getClosingPrice() > arrayData.valueAt(5).getClosingPrice();
            raiseFall20[14] = arrayData.valueAt(5).getClosingPrice() > arrayData.valueAt(6).getClosingPrice();
            raiseFall20[13] = arrayData.valueAt(6).getClosingPrice() > arrayData.valueAt(7).getClosingPrice();
            raiseFall20[12] = arrayData.valueAt(7).getClosingPrice() > arrayData.valueAt(8).getClosingPrice();
            raiseFall20[11] = arrayData.valueAt(8).getClosingPrice() > arrayData.valueAt(9).getClosingPrice();
            raiseFall20[10] = arrayData.valueAt(9).getClosingPrice() > arrayData.valueAt(10).getClosingPrice();
            raiseFall20[9] = arrayData.valueAt(10).getClosingPrice() > arrayData.valueAt(11).getClosingPrice();
            raiseFall20[8] = arrayData.valueAt(11).getClosingPrice() > arrayData.valueAt(12).getClosingPrice();
            raiseFall20[7] = arrayData.valueAt(12).getClosingPrice() > arrayData.valueAt(13).getClosingPrice();
            raiseFall20[6] = arrayData.valueAt(13).getClosingPrice() > arrayData.valueAt(14).getClosingPrice();
            raiseFall20[5] = arrayData.valueAt(14).getClosingPrice() > arrayData.valueAt(15).getClosingPrice();
            raiseFall20[4] = arrayData.valueAt(15).getClosingPrice() > arrayData.valueAt(16).getClosingPrice();
            raiseFall20[3] = arrayData.valueAt(16).getClosingPrice() > arrayData.valueAt(17).getClosingPrice();
            raiseFall20[2] = arrayData.valueAt(17).getClosingPrice() > arrayData.valueAt(18).getClosingPrice();
            raiseFall20[1] = arrayData.valueAt(18).getClosingPrice() > arrayData.valueAt(19).getClosingPrice();
            raiseFall20[0] = arrayData.valueAt(19).getClosingPrice() > arrayData.valueAt(20).getClosingPrice();
        }
    }

    //连续涨跌相同、相似
    private void compareRaiseFall(ShareRealm shareRealm, ShareRealm shareRealm1) {
        raiseFall5Tem[raiseFall5Index] = shareRealm1.getClosingPrice() > shareRealm.getClosingPrice();
        raiseFall10Tem[raiseFall10Index] = shareRealm1.getClosingPrice() > shareRealm.getClosingPrice();
        raiseFall20Tem[raiseFall20Index] = shareRealm1.getClosingPrice() > shareRealm.getClosingPrice();
        if (raiseFall5Index == 4) {
            raiseFall5Index = 0;
        } else {
            raiseFall5Index++;
        }
        if (raiseFall10Index == 9) {
            raiseFall10Index = 0;
        } else {
            raiseFall10Index++;
        }
        if (raiseFall20Index == 19) {
            raiseFall20Index = 0;
        } else {
            raiseFall20Index++;
        }
        //连续5个交易日涨跌相同
        if (raiseFall5[0] == raiseFall5Tem[raiseFall5Index]
                && raiseFall5[1] == raiseFall5Tem[raiseFall5Index + 1 > 4 ? raiseFall5Index - 4 : raiseFall5Index + 1]
                && raiseFall5[2] == raiseFall5Tem[raiseFall5Index + 2 > 4 ? raiseFall5Index - 3 : raiseFall5Index + 2]
                && raiseFall5[3] == raiseFall5Tem[raiseFall5Index + 3 > 4 ? raiseFall5Index - 2 : raiseFall5Index + 3]
                && raiseFall5[4] == raiseFall5Tem[raiseFall5Index + 4 > 4 ? raiseFall5Index - 1 : raiseFall5Index + 4]) {
            String text = shareRealm1.getDateYear() + shareRealm1.getDate();
            if (TextUtils.isEmpty(raiseFall5Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        raiseFall5Txt.setText("连续5个交易日涨跌相同：" + text);
                    }
                });

            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        raiseFall5Txt.setText(raiseFall5Txt.getText() + "、" + text);
                    }
                });

            }
        }
        //连续10个交易日涨跌相同
        if (raiseFall10[0] == raiseFall10Tem[raiseFall10Index]
                && raiseFall10[1] == raiseFall10Tem[raiseFall10Index + 1 > 9 ? raiseFall10Index - 9 : raiseFall10Index + 1]
                && raiseFall10[2] == raiseFall10Tem[raiseFall10Index + 2 > 9 ? raiseFall10Index - 8 : raiseFall10Index + 2]
                && raiseFall10[3] == raiseFall10Tem[raiseFall10Index + 3 > 9 ? raiseFall10Index - 7 : raiseFall10Index + 3]
                && raiseFall10[4] == raiseFall10Tem[raiseFall10Index + 4 > 9 ? raiseFall10Index - 6 : raiseFall10Index + 4]
                && raiseFall10[5] == raiseFall10Tem[raiseFall10Index + 5 > 9 ? raiseFall10Index - 5 : raiseFall10Index + 5]
                && raiseFall10[6] == raiseFall10Tem[raiseFall10Index + 6 > 9 ? raiseFall10Index - 4 : raiseFall10Index + 6]
                && raiseFall10[7] == raiseFall10Tem[raiseFall10Index + 7 > 9 ? raiseFall10Index - 3 : raiseFall10Index + 7]
                && raiseFall10[8] == raiseFall10Tem[raiseFall10Index + 8 > 9 ? raiseFall10Index - 2 : raiseFall10Index + 8]
                && raiseFall10[9] == raiseFall10Tem[raiseFall10Index + 9 > 9 ? raiseFall10Index - 1 : raiseFall10Index + 9]) {
            String text = shareRealm1.getDateYear() + shareRealm1.getDate();
            if (TextUtils.isEmpty(raiseFall10Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        raiseFall10Txt.setText("连续10个交易日涨跌相同：" + text);
                    }
                });

            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        raiseFall10Txt.setText(raiseFall10Txt.getText() + "、" + text);
                    }
                });

            }

        }
        //连续20个交易日涨跌中16个相同
        int num = 0;
        if (raiseFall20[0] == raiseFall20Tem[raiseFall20Index]) {
            num++;
        }
        if (raiseFall20[1] == raiseFall20Tem[raiseFall20Index + 1 > 19 ? raiseFall20Index - 19 : raiseFall20Index + 1]) {
            num++;
        }
        if (raiseFall20[2] == raiseFall20Tem[raiseFall20Index + 2 > 19 ? raiseFall20Index - 18 : raiseFall20Index + 2]) {
            num++;
        }
        if (raiseFall20[3] == raiseFall20Tem[raiseFall20Index + 3 > 19 ? raiseFall20Index - 17 : raiseFall20Index + 3]) {
            num++;
        }
        if (raiseFall20[4] == raiseFall20Tem[raiseFall20Index + 4 > 19 ? raiseFall20Index - 16 : raiseFall20Index + 4]) {
            num++;
        }
        if (raiseFall20[5] == raiseFall20Tem[raiseFall20Index + 5 > 19 ? raiseFall20Index - 15 : raiseFall20Index + 5]) {
            num++;
        }
        if (raiseFall20[6] == raiseFall20Tem[raiseFall20Index + 6 > 19 ? raiseFall20Index - 14 : raiseFall20Index + 6]) {
            num++;
        }
        if (raiseFall20[7] == raiseFall20Tem[raiseFall20Index + 7 > 19 ? raiseFall20Index - 13 : raiseFall20Index + 7]) {
            num++;
        }
        if (raiseFall20[8] == raiseFall20Tem[raiseFall20Index + 8 > 19 ? raiseFall20Index - 12 : raiseFall20Index + 8]) {
            num++;
        }
        if (raiseFall20[9] == raiseFall20Tem[raiseFall20Index + 9 > 19 ? raiseFall20Index - 11 : raiseFall20Index + 9]) {
            num++;
        }
        if (raiseFall20[10] == raiseFall20Tem[raiseFall20Index + 10 > 19 ? raiseFall20Index - 10 : raiseFall20Index + 10]) {
            num++;
        }
        if (raiseFall20[11] == raiseFall20Tem[raiseFall20Index + 11 > 19 ? raiseFall20Index - 9 : raiseFall20Index + 11]) {
            num++;
        }
        if (raiseFall20[12] == raiseFall20Tem[raiseFall20Index + 12 > 19 ? raiseFall20Index - 8 : raiseFall20Index + 12]) {
            num++;
        }
        if (raiseFall20[13] == raiseFall20Tem[raiseFall20Index + 13 > 19 ? raiseFall20Index - 7 : raiseFall20Index + 13]) {
            num++;
        }
        if (raiseFall20[14] == raiseFall20Tem[raiseFall20Index + 14 > 19 ? raiseFall20Index - 6 : raiseFall20Index + 14]) {
            num++;
        }
        if (raiseFall20[15] == raiseFall20Tem[raiseFall20Index + 15 > 19 ? raiseFall20Index - 5 : raiseFall20Index + 15]) {
            num++;
        }
        if (raiseFall20[16] == raiseFall20Tem[raiseFall20Index + 16 > 19 ? raiseFall20Index - 4 : raiseFall20Index + 16]) {
            num++;
        }
        if (raiseFall20[17] == raiseFall20Tem[raiseFall20Index + 17 > 19 ? raiseFall20Index - 3 : raiseFall20Index + 17]) {
            num++;
        }
        if (raiseFall20[18] == raiseFall20Tem[raiseFall20Index + 18 > 19 ? raiseFall20Index - 2 : raiseFall20Index + 18]) {
            num++;
        }
        if (raiseFall20[19] == raiseFall20Tem[raiseFall20Index + 19 > 19 ? raiseFall20Index - 1 : raiseFall20Index + 19]) {
            num++;
        }
        if (num >= 16) {
            String text = shareRealm1.getDateYear() + shareRealm1.getDate();
            if (TextUtils.isEmpty(raiseFall20Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        raiseFall20Txt.setText("连续20个交易日涨跌16个相同：" + text);
                    }
                });

            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        raiseFall20Txt.setText(raiseFall20Txt.getText() + "、" + text);
                    }
                });

            }
        }
    }

    //连续交易日阴阳线相同、相似
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
            String text = shareRealm.getDateYear() + shareRealm.getDate();
            if (TextUtils.isEmpty(yinYang5Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        yinYang5Txt.setText("连续5个交易日阴阳线相同：" + text);
                    }
                });

            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        yinYang5Txt.setText(yinYang5Txt.getText() + "、" + text);
                    }
                });

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
            String text = shareRealm.getDateYear() + shareRealm.getDate();
            if (TextUtils.isEmpty(yinYang10Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        yinYang10Txt.setText("连续10个交易日阴阳线相同：" + text);
                    }
                });

            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        yinYang10Txt.setText(yinYang10Txt.getText() + "、" + text);
                    }
                });

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
        if (num >= 16) {
            String text = shareRealm.getDateYear() + shareRealm.getDate();
            if (TextUtils.isEmpty(yinYang20Txt.getText())) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        yinYang20Txt.setText("连续20个交易日阴阳线16个相同：" + text);
                    }
                });

            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        yinYang20Txt.setText(yinYang20Txt.getText() + "、" + text);
                    }
                });

            }
        }
    }

    private void initYinYang() {
        if (arrayData.size() >= 5) {
            yinYang5[4] = arrayData.valueAt(0).getClosingPrice() > arrayData.valueAt(0).getOpenPrice();
            yinYang5[3] = arrayData.valueAt(1).getClosingPrice() > arrayData.valueAt(1).getOpenPrice();
            yinYang5[2] = arrayData.valueAt(2).getClosingPrice() > arrayData.valueAt(2).getOpenPrice();
            yinYang5[1] = arrayData.valueAt(3).getClosingPrice() > arrayData.valueAt(3).getOpenPrice();
            yinYang5[0] = arrayData.valueAt(4).getClosingPrice() > arrayData.valueAt(4).getOpenPrice();
        }
        if (arrayData.size() >= 10) {
            yinYang10[9] = arrayData.valueAt(0).getClosingPrice() > arrayData.valueAt(0).getOpenPrice();
            yinYang10[8] = arrayData.valueAt(1).getClosingPrice() > arrayData.valueAt(1).getOpenPrice();
            yinYang10[7] = arrayData.valueAt(2).getClosingPrice() > arrayData.valueAt(2).getOpenPrice();
            yinYang10[6] = arrayData.valueAt(3).getClosingPrice() > arrayData.valueAt(3).getOpenPrice();
            yinYang10[5] = arrayData.valueAt(4).getClosingPrice() > arrayData.valueAt(4).getOpenPrice();
            yinYang10[4] = arrayData.valueAt(5).getClosingPrice() > arrayData.valueAt(5).getOpenPrice();
            yinYang10[3] = arrayData.valueAt(6).getClosingPrice() > arrayData.valueAt(6).getOpenPrice();
            yinYang10[2] = arrayData.valueAt(7).getClosingPrice() > arrayData.valueAt(7).getOpenPrice();
            yinYang10[1] = arrayData.valueAt(8).getClosingPrice() > arrayData.valueAt(8).getOpenPrice();
            yinYang10[0] = arrayData.valueAt(9).getClosingPrice() > arrayData.valueAt(9).getOpenPrice();
        }
        if (arrayData.size() >= 20) {
            yinYang20[19] = arrayData.valueAt(0).getClosingPrice() > arrayData.valueAt(0).getOpenPrice();
            yinYang20[18] = arrayData.valueAt(1).getClosingPrice() > arrayData.valueAt(1).getOpenPrice();
            yinYang20[17] = arrayData.valueAt(2).getClosingPrice() > arrayData.valueAt(2).getOpenPrice();
            yinYang20[16] = arrayData.valueAt(3).getClosingPrice() > arrayData.valueAt(3).getOpenPrice();
            yinYang20[15] = arrayData.valueAt(4).getClosingPrice() > arrayData.valueAt(4).getOpenPrice();
            yinYang20[14] = arrayData.valueAt(5).getClosingPrice() > arrayData.valueAt(5).getOpenPrice();
            yinYang20[13] = arrayData.valueAt(6).getClosingPrice() > arrayData.valueAt(6).getOpenPrice();
            yinYang20[12] = arrayData.valueAt(7).getClosingPrice() > arrayData.valueAt(7).getOpenPrice();
            yinYang20[11] = arrayData.valueAt(8).getClosingPrice() > arrayData.valueAt(8).getOpenPrice();
            yinYang20[10] = arrayData.valueAt(9).getClosingPrice() > arrayData.valueAt(9).getOpenPrice();
            yinYang20[9] = arrayData.valueAt(10).getClosingPrice() > arrayData.valueAt(10).getOpenPrice();
            yinYang20[8] = arrayData.valueAt(11).getClosingPrice() > arrayData.valueAt(11).getOpenPrice();
            yinYang20[7] = arrayData.valueAt(12).getClosingPrice() > arrayData.valueAt(12).getOpenPrice();
            yinYang20[6] = arrayData.valueAt(13).getClosingPrice() > arrayData.valueAt(13).getOpenPrice();
            yinYang20[5] = arrayData.valueAt(14).getClosingPrice() > arrayData.valueAt(14).getOpenPrice();
            yinYang20[4] = arrayData.valueAt(15).getClosingPrice() > arrayData.valueAt(15).getOpenPrice();
            yinYang20[3] = arrayData.valueAt(16).getClosingPrice() > arrayData.valueAt(16).getOpenPrice();
            yinYang20[2] = arrayData.valueAt(17).getClosingPrice() > arrayData.valueAt(17).getOpenPrice();
            yinYang20[1] = arrayData.valueAt(18).getClosingPrice() > arrayData.valueAt(18).getOpenPrice();
            yinYang20[0] = arrayData.valueAt(19).getClosingPrice() > arrayData.valueAt(19).getOpenPrice();
        }
    }

    //统计次数
    private void analyze(String day) {
        Log.e("wuzhiyun" + code, "统计日期：" + day);
        mView.post(new Runnable() {
            @Override
            public void run() {
                solarTodayDayTxt.setText("统计日期(阳历)：" + day);
            }
        });

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
            mView.post(new Runnable() {
                @Override
                public void run() {
                    lunarTodayDayTxt.setText("统计日期(阴历)：" + lunarTodayStr);
                }
            });

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
        mView.post(new Runnable() {
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
    }
}
