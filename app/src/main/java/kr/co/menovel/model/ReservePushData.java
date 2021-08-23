package kr.co.menovel.model;

public class ReservePushData implements DataModel{
    private String title;
    private String body;
    private String senddate;
    private String url;
    private String img_url;

    public String getTitle() { return title; }

    public String getBody() {
        return body;
    }

    public String getSenddate() {
        return senddate;
    }

    public String getUrl() {
        return url;
    }

    public String getImg_url() {
        return img_url;
    }
}
