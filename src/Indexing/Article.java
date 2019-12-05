package Indexing;

/**
 * This class represents a document. A document has an ID, title, publication date and a content.
 */
public class Article {

    private String docId;
    private String date;
    private String title;
    private String content;

    /**
     * A constructor that initializes the document using only its ID and content, and assigns null to the other fields.
     * @param docId the ID of the document
     * @param content the content of the document
     */
    public Article(String docId, String content) {
        this.docId = docId;
        this.date = null;
        this.title = null;
        this.content = content;
    }

    /**
     * A constructor that initializes all of the fields of the document with the given parameters.
     * @param docID the ID of the document
     * @param title the title of the document
     * @param date the publication date of the document
     * @param content the content of the document
     */
    public Article(String docID, String title, String date, String content){
        this.docId = docID;
        this.title = title;
        this.date = date;
        this.content = content;
    }

    //getters
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


    //setters
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
