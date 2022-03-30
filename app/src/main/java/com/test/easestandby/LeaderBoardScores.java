package com.test.easestandby;

public class LeaderBoardScores {
    public LeaderBoardScores(int rank, int grade, int score, String username) {
        this.rank = rank;
        this.grade = grade;
        this.score = score;
        this.username = username;
    }
    public LeaderBoardScores(){}

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStringGrade(){
        return Integer.toString(grade);
    }

    public String getStringScore(){
        return Integer.toString(score);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String msg(){
        return Integer.toString(this.getScore());
    }

    private int rank;
    private int grade;
    private int score;
    private String username;

}
