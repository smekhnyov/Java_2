public class ParserException extends Exception
{
    private static final long serialVersionUID = 1L;
    private String errorString;

    public ParserException(String errorString)
    {
        super();
        this.errorString = errorString;
    }

    public String toString()
    {
        return errorString;
    }
}