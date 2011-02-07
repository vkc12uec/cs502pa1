/*
http://www.seasite.niu.edu/cs580java/testexception.htm

each operation failure will have different 'cause'

just print the object to see error
*/
class RingException extends Exception {
	private String what;

	public RingException(String cause) {
		super(cause);
		what = cause;
	}

	public String getError () {
		return what;
	}
}
