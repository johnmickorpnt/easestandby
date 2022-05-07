package com.test.easestandby;

public class ModelClass {



    private String textView;
    private String textView2;
    private String textView3;
    private String divider;



    ModelClass(String textView, String textView2, String textView3, String divider) {


        this.textView = textView;
        this.textView2 = textView2;
        this.textView3 = textView3;
        this.divider = divider;


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
