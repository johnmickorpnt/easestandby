package com.test.easestandby;

public class OptionClass {

    public OptionClass(String question, String oA, String oB, String oC, String oD, String oAns) {
        Question = question;
        this.oA = oA;
        this.oB = oB;
        this.oC = oC;
        this.oD = oD;
        this.oAns = oAns;
    }

    public String getQuestion() {
        return Question;
    }

    public void setQuestion(String question) {
        Question = question;
    }

    public String getoA() {
        return oA;
    }

    public void setoA(String oA) {
        this.oA = oA;
    }

    public String getoB() {
        return oB;
    }

    public void setoB(String oB) {
        this.oB = oB;
    }

    public String getoC() {
        return oC;
    }

    public void setoC(String oC) {
        this.oC = oC;
    }

    public String getoD() {
        return oD;
    }

    public void setoD(String oD) {
        this.oD = oD;
    }

    public String getoAns() {
        return oAns;
    }

    public void setoAns(String oAns) {
        this.oAns = oAns;
    }

    String Question;
    String oA;
    String oB;
    String oC;
    String oD;
    String oAns;

}


