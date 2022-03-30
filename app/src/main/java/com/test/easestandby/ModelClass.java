package com.test.easestandby;

public class ModelClass {



    private  int imageview1;
    private String textView;
    private String textView2;
    private String textView3;
    private String divider;



    ModelClass(int imageview1,String textView, String textView2, String textView3, String divider) {

        this.imageview1 = imageview1;
        this.textView = textView;
        this.textView2 = textView2;
        this.textView3 = textView3;
        this.divider = divider;





    }

    public int getImageview1() {
        return imageview1;
    }

    public String getTextView1() {
        return textView;
    }

    public String getTextView2() {
        return textView2;
    }

    public String getTextView3() {
        return textView3;
    }

    public String getDivider() {
        return divider;
    }
}
