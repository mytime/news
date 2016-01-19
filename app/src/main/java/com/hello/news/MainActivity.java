package com.hello.news;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hello.news.models.News;
import com.loopj.android.image.SmartImageView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    List<News> newsList;

    //更新主线程Ui
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            ListView lv = (ListView) findViewById(R.id.lv);
            //设置adapter
            //要保障在设置adapter时，新闻xml文件已经解析完毕
            lv.setAdapter(new MyAdapter());
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getNewsInfo();

        //获取ListView
//        ListView lv = (ListView) findViewById(R.id.lv);
//        //设置adapter
//        //要保障在设置adapter时，新闻xml文件已经解析完毕
//        lv.setAdapter(new MyAdapter());





    }

    //自定义Adapter
    class MyAdapter extends BaseAdapter{

        //得到模型层的数量，用来确定ListView需要有多少个条目
        @Override
        public int getCount() {
            return newsList.size();
        }

        //返回一个View对象，作为listview的条目显示至界面
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            News news = newsList.get(position);
            View v = View.inflate(MainActivity.this, R.layout.item_listview, null);

            //给三个文本框设置内容
            TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
            tvTitle.setText(news.getTitle());

            TextView tvDetail = (TextView) v.findViewById(R.id.tvDetail);
            tvDetail.setText(news.getDetail());

            TextView tvComment = (TextView) v.findViewById(R.id.tvComment);
            tvComment.setText(news.getComment()+"条评论");

            //给新闻图片设置内容 ,item_listview.xml -> ImageView 改成 SmartImageView
            SmartImageView siv = (SmartImageView) v.findViewById(R.id.iv);
            siv.setImageUrl(news.getImageUrl());



            return v;
        }
        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }


    }

    private void getNewsInfo() {

        Thread t = new Thread(){

            @Override
            public void run() {

                String path = "http://10.0.2.2:63343/news/news.xml";

                try {


                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    //发送http GET请求，获取返回码
                    if (conn.getResponseCode() ==200 ){

                        //获得数据流
                        InputStream is = conn.getInputStream();

                        //使用pull解析器，解析这个流
                        parseNewsXml(is);
                    }

                    //测试输出
//                    for(News n : newsList){
//                        System.out.println(n.toString());
//                    }


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        t.start();
    }


    private void parseNewsXml(InputStream is){
        //
        XmlPullParser xp = Xml.newPullParser();

        try {

            xp.setInput(is,"utf-8");

            //对节点的事件类型进行判断，
            int type = xp.getEventType();

            //初始化
            News news = null;

            //如果不是末尾就继续
            while (type != XmlPullParser.END_DOCUMENT){

                switch (type){
                    //开始节点
                    case XmlPullParser.START_TAG:
                        if("newslist".equals(xp.getName())){
                            newsList = new ArrayList<News>();
                        }else if("news".equals(xp.getName())){
                            news = new News();
                        }else if("title".equals(xp.getName())){
                            String title = xp.nextText();
                            news.setTitle(title);
                        }else if("detail".equals(xp.getName())){
                            String detail = xp.nextText();
                            news.setDetail(detail);
                        }else if("comment".equals(xp.getName())){
                            String comment = xp.nextText();
                            news.setComment(comment);
                        }else if("image".equals(xp.getName())){
                            String image = xp.nextText();
                            news.setImageUrl(image);
                        }

                        break;
                    //结束节点
                    case XmlPullParser.END_TAG:
                        if("news".equals(xp.getName())){
                            newsList.add(news);
                        }
                        break;

                    default:
                        break;
                }

                //解析完一个节点后再继续下一个节点，并返回它的类型
                type = xp.next();
            }


            //发消息，让主线程设置ListView的适配器
            //由于newsList是个全局变量，所以不需要Message携带消息，因此发送一个空消息即可
            handler.sendEmptyMessage(1);

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
