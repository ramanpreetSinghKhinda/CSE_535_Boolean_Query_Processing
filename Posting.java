/**
 * @author Ramanpreet Singh Khinda
 * 
 * @code This is a helper class for creating Postings
 *
 */
public class Posting {
	int documentId, termFrequency;

	Posting(){
		
	}
	
	Posting(int documentId, int termFrequency){
		this.documentId = documentId;
		this.termFrequency = termFrequency;
	}
	
	@SuppressWarnings("javadoc")
	public int getDocumentId() {
		return documentId;
	}

	@SuppressWarnings("javadoc")
	public int getTermFrequency() {
		return termFrequency;
	}

	@SuppressWarnings("javadoc")
	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}

	@SuppressWarnings("javadoc")
	public void setTermFrequency(int termFrequency) {
		this.termFrequency = termFrequency;
	}
}