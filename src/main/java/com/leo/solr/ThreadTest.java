package com.leo.solr;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by LX on 2017/9/27.
 */
public class ThreadTest extends Thread {

    private String name;

    public ThreadTest(String name){
        this.name = name;
    }

    public void run(){

        try {
            int i = 10;
            while(i > 0){
                System.out.println(this.name+": 我来了"+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                Thread.sleep(2000);
                i -- ;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
