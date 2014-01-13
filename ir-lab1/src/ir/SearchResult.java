package ir;

public class SearchResult {

    public PostingsList list;
    public int currentIndex;

    public SearchResult(PostingsList input) {
        this.list = input;
        this.currentIndex = 0;
    }

    public PostingsEntry getNext() {
        this.currentIndex++;
        return getCurrent();
    }

    public PostingsEntry getCurrent() {
        if (this.currentIndex > list.size()) {
            return null;
        } else {
            return this.list.get(this.currentIndex);
        }
    }
}
