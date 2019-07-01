package cn.sheep.cms.utils;

/**
 * Created by ThinkPad on 2017/8/26.
 */
public class test {

    public static void main(String[] args) {

        Thread thread = new Thread(new Runnable() {
            public void run() {
                System.out.println("======");
            }
        });


        thread.run();


    }
}
