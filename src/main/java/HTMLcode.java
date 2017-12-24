/*
    HTMLcode.java

    A simple object designed to represent an HTML tag, storing its start tag,
    its end tag, and a reference number for each so their presence can be
    detected by the parser.
*/

public class HTMLcode implements Constants
{
    private String      fStart, fEnd;
    private int         fStartReference, fEndReference;

    /*******************************************************************************
        HTMLcode::HTMLcode
    *******************************************************************************/
    public HTMLcode( String s, String e, int sRef, int eRef)
    {
        fStart = new String(s);
        fEnd = new String(e);
        fStartReference = sRef;
        fEndReference = eRef;
    }

    /*******************************************************************************
        HTMLcode::HTMLcode
    *******************************************************************************/
    public HTMLcode( String s, int sRef, int eRef)
    {
        fStart = new String(s);
        fEnd = kSlash + fStart;
        fStartReference = sRef;
        fEndReference = eRef;
    }

    /*******************************************************************************
        HTMLcode::HTMLcode      for codes that aren't interpreted
    *******************************************************************************/
    public HTMLcode( String s, String e)
    {
        this( s, e, 0, 0);
    }

    /*******************************************************************************
        HTMLcode::HTMLcode      for codes that aren't interpreted
    *******************************************************************************/
    public HTMLcode( String s)
    {
        this( s, 0, 0);
    }

    /*******************************************************************************
        HTMLcode::GetStartRef
    *******************************************************************************/
    int GetStartRef()
    {
        return fStartReference;
    }

    /*******************************************************************************
        HTMLcode::GetEndRef
    *******************************************************************************/
    int GetEndRef()
    {
        return fEndReference;
    }

    /*******************************************************************************
        HTMLcode::GetStart
    *******************************************************************************/
    String GetStart()
    {
        return fStart;
    }

    /*******************************************************************************
        HTMLcode::GetStartWithBrackets
    *******************************************************************************/
    String GetStartWithBrackets()
    {
        return new String( kLeftBracket + GetStart() + kRightBracket);
    }

    /*******************************************************************************
        HTMLcode::GetEnd
    *******************************************************************************/
    String GetEnd()
    {
        return fEnd;
    }

    /*******************************************************************************
        HTMLcode::GetEndWithBrackets
    *******************************************************************************/
    String GetEndWithBrackets()
    {
        return new String( kLeftBracket + GetEnd() + kRightBracket);
    }

    /*******************************************************************************
        HTMLcode::Append
    *******************************************************************************/
    HTMLcode Append( String s, String e)
    {
        // for the moment - keep refs the same. They won't be used anyway.
        return new HTMLcode( GetStart() + s, GetEnd() + e, GetStartRef(), GetEndRef());
    }


    /*******************************************************************************
        HTMLcode::Append
    *******************************************************************************/
    HTMLcode Append( String s)
    {
        return Append( s, "");
    }
}