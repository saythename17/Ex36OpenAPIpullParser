package com.icandothisallday2020.ex36openapipullparser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayAdapter adapter;
    ArrayList<String> items=new ArrayList<>();
    String apiKey="9b1aebc103922213fbc7ce498a50ae8c";//kobis.or.kr 에서 발급받은 키값
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=findViewById(R.id.listview);
        adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,items);
        listView.setAdapter(adapter);
    }
    public void clcik(View view) {
        //네트워크를 통해 읽어온 데이터 분석->리스트뷰에 set
        //인터넷 사용->permission(허가) 작성 [AndroidManifest.xml]
        //※네트워크 작업: 반드시 별도의 Thread 에서 진행
        new Thread(){
            @Override
            public void run() {
                Date date=new Date();//오늘 날짜
                date.setTime(date.getTime()-(1000*60*60*24));//박스오피스 검색을 위해 하루전 으로 세팅
                SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");//원하는 날짜 출력형태 포맷
                String date_=sdf.format(date);//검색할 날짜"20200526"(검색날짜기준하루전)
                //네트워크를 통해 서버에서 필요한 정보의 xml 읽어오기
                String address="http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.xml?"
                        +"key="+apiKey+"&targetDt="+date_;//주소
                /////////////////////////////////////////////////////////////////////////////
                //api 28버전 부터 인터넷 주소가 https 가 아닌 http 는 동작 불가
                // ->http 사용시 AndroidManifest.xml 에 추가작업을 해야함
                /////////////////////////////////////////////////////////////////////////////









                //완성된 네트워크 주소와 연결하여 데이터 읽어오기
                //무지개로드(Stream)를 만들어주는 해임달(URL) 객체 생성
                try {//해임달에게 무지개로드 열어달라고 요청
                    URL url=new URL(address);
                    InputStream is=url.openStream();//byte(숫자-해독어렵) 단위로 읽어오는 Stream
                    InputStreamReader isr=new InputStreamReader(is);//String 문자로 읽어오도록
                    //XML 문서를 isr 로 부터 받아와 분석해주는 분석가 객체를 만들어주는 공장생성
                    XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                    XmlPullParser xpp=factory.newPullParser();//PullParser(분석가 객체) 생성
                    xpp.setInput(isr);
                    //PullParser 는 문서를 읽어오는 순간(next()없이도) 커서가 이미 문서안에 들어와있음
                    int eventType=xpp.getEventType();
                    StringBuffer buffer=new StringBuffer();
                    //└String 결합해서 하나의 메모리 공간안에 쓰기-지속적으로 객체를 만드는 것이아닌
                    //문자열을 모아놓는 녀석이기 때문에 메모리사용에 효율적
                    while(eventType!=XmlPullParser.END_DOCUMENT){
                        switch (eventType){
                            case XmlPullParser.START_DOCUMENT:
                                runOnUiThread(new Runnable() {//run() method 안 Toast 를 바로 호출 불가
                                    @Override //Main Thread 아닌 곳에서 화면(UI)에 변환을 주려면위임장을 가진녀석 호출
                                    public void run() {
                                        Toast.makeText(MainActivity.this,"Start Parsing",Toast.LENGTH_SHORT).show();
                                    }
                                });//Runnable
                                break;
                            case XmlPullParser.START_TAG:
                                String tagName=xpp.getName();
                                if(tagName.equals("dailyBoxOffice")) buffer=new StringBuffer();
                                else if(tagName.equals("rank")){
                                    buffer.append("\n순위 : ");
                                    xpp.next(); //커서 단위 이동
                                    buffer.append(xpp.getText()+"\n");/*text 값 append &줄바꿈 문자*/
                                }
                                else if(tagName.equals("movieNm")) {
                                    buffer.append("제목 : ");     xpp.next();
                                    buffer.append(xpp.getText()+"\n"); }
                                else if(tagName.equals("openDt")) {
                                    buffer.append("개봉일 : ");   xpp.next();
                                    buffer.append(xpp.getText()+"\n"); }
                                else if(tagName.equals("audiCnt")) {
                                    buffer.append("일일 관객수 : ");     xpp.next();
                                    buffer.append(xpp.getText()+"\n"); }
                                else if(tagName.equals("audiAcc")) {
                                    buffer.append("누적 관객수 : ");     xpp.next();
                                    buffer.append(xpp.getText()+"\n"); }
                                break;
                            case XmlPullParser.END_TAG:
                                String tagName2=xpp.getName();
                                if(tagName2.equals("dailyBoxOffice")) {
                                    String s=buffer.toString();//누적된 StringBuffer->String 으로 변환
                                    items.add(s);//ArrayList 에 추가
                                    runOnUiThread(new Runnable() {
                                        @Override //화면 변화는 반드시 UI Thread 만 가능
                                        public void run() {
                                            adapter.notifyDataSetChanged();//리스트뷰(UI) 갱신
                                        }/*run()*/     });/*Runnable*/ }//if
                                break;
                        }//switch...
                        eventType=xpp.next();//커서 이동
                    }//while...
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"End Parsing",Toast.LENGTH_SHORT).show();
                        } });
                }catch (Exception e) {e.printStackTrace();}
            }        }.start();//Thread
        
    }
}
