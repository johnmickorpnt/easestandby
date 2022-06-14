package com.test.easestandby;

//  This object class shows the leaderboard's row which are rank, score, username
public class LeaderBoardScores {
    public LeaderBoardScores(int rank, String grade, int score, String username) {
        this.rank = rank;
        this.grade = grade;
        this.score = score;
        this.username = username;
    }

    // The constructor of the leaderboadscores along with the setters and getters
    public LeaderBoardScores(){}

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
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
        return grade;
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
    private String grade;
    private int score;
    private String username;

}
