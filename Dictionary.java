/**
 * @author Ramanpreet Singh Khinda
 * 
 * @code This is a helper class which for creating Dictionary
 *
 */
public class Dictionary {
	String term;
	int sizeOfPosting;

	Dictionary() {

	}

	Dictionary(String term, int sizeOfPosting) {
		this.term = term;
		this.sizeOfPosting = sizeOfPosting;
	}

	// Since we are using custom comparator for sorting the HashMaps we have to
	// implement our own hashing algorithm. We want only the hash
	// of term because otherwise we will not be able to search the query term
	// from the HashMap
	public int hashCode() {
		return term.hashCode();
	}

	// Extended functionality of generic equals method to do comparison of
	// Dictionary only on the basis of terms
	public boolean equals(Object obj) {
		if (obj instanceof Dictionary) {
			Dictionary dic = (Dictionary) obj;
			return (dic.term.equals(this.term));
		} else {
			return false;
		}
	}

	@SuppressWarnings("javadoc")
	public String getTerm() {
		return term;
	}

	@SuppressWarnings("javadoc")
	public int getSizeOfPosting() {
		return sizeOfPosting;
	}

	@SuppressWarnings("javadoc")
	public void setTerm(String term) {
		this.term = term;
	}

	@SuppressWarnings("javadoc")
	public void setSizeOfPosting(int sizeOfPosting) {
		this.sizeOfPosting = sizeOfPosting;
	}
}