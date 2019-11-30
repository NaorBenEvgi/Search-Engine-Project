public class Article {

    private String docId;
    private String date;
    private String title;
    private String content;

    public Article(String docId, String content) {
        this.docId = docId;
        this.date = null;
        this.title = null;
        this.content = content;
    }

    public String getDocId() {
        return docId;
    }

    public String getDate() {
        return date == null ? "No date": this.date;
    }

    public String getTitle() {
        return title == null ? "No title": this.title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content){
        this.content = content;
    }
    public void setDocId(String docId){
        this.docId = docId;
    }
    public void setDate(String date){
        this.date = date;
    }
    public void setTitle(String title){
        this.title = title;
    }


    @Override
    public String toString() {
        return "docID:\n" + this.getDocId() + "\n" +
                "date:\n" + this.getDate() + "\n" +
                "title:\n" + this.getTitle() + "\n" +
                "content:\n" + this.getContent() + "\n";
    }
}
