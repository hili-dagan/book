package com.example.title;

public class Score {
    public int top1, top2, top3;
    // שדה חדש לשמירת ה-ID של המשתמש במקום הראשון
    public String top1UserUID;

    // קונסטרקטור ריק חובה עבור Firebase
    public Score() {}

    // קונסטרקטור מעודכן שמתאים ללוגיקה החדשה
    public Score(int top1, int top2, int top3, String top1UserUID) {
        this.top1 = top1;
        this.top2 = top2;
        this.top3 = top3;
        this.top1UserUID = top1UserUID;
    }
}
