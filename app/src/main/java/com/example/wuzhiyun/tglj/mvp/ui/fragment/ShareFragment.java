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
import com.example.wuzhiyun.tglj.mvp.model.entity.ShareK;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;

public class ShareFragment extends Fragment {
    View mView;
    private String name;//股票名称
    private String code;//股票代码
    private SparseArray arrayData;//k线数据

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_share, null);
            code = getArguments().getString("CODE");
            getData();
        }
        ((TextView) mView.findViewById(R.id.name)).setText("聊天界面");
        return mView;
    }

    public String getShareName() {
        return name;
    }

    private void getData() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Document doc = null;
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int jidu = month / 3 + (month % 3 == 0 ? 0 : 1);
                String date = "10-24";
                String lundar = "";
                try {
                    lundar = CalendarUtil.solarToLunar("20181024");
                    if (!TextUtils.isEmpty(lundar)) {
                        lundar = lundar.substring(8);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (int k = 0; k < 13; k++, year--) {
                    for (int j = 4; j > 0; j--) {
                        try {
                            String url = "http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/" + code + ".phtml?year=" + year + "&jidu=" + j;
                            doc = Jsoup.connect(url).get();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Element table = doc.getElementById("FundHoldSharesTable");
                        if (table == null) {
                            continue;
                        }
                        Elements trs = table.select("tr");
                        for (int i = 2; i < trs.size(); i++) {
                            Elements tds = trs.get(i).select("td");
                            try {
                                ShareK shareK = new ShareK();
                                shareK.setDate(tds.get(0).text());
                                shareK.setOpenPrice(Double.valueOf(tds.get(1).text()));
                                shareK.setMaxPrice(Double.valueOf(tds.get(2).text()));
                                shareK.setClosingPrice(Double.valueOf(tds.get(3).text()));
                                shareK.setMinPrice(Double.valueOf(tds.get(4).text()));
                                shareK.setVolume(Long.valueOf(tds.get(5).text()));
                                shareK.setTurnover(Long.valueOf(tds.get(6).text()));
                                String dateTemp = shareK.getDate().replace("-", "");
                                shareK.setLunar(CalendarUtil.solarToLunar(dateTemp));
                                arrayData.append(Integer.valueOf(dateTemp), shareK);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
        }.start();
    }

}
