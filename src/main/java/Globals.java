import java.awt.*;
import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;

public class Globals implements Constants
{
    URL                             fOriginalURL;
    String                          fRetrievedArgument;
    static HTMLcode                 gEmphasis, gStrong, gBold, gTitle, gTable, gFont, gHTML;
    static HTMLcode                 gUnderline, gTableRow, gTableCol, gHorizontalRule, gBreak;
    static HTMLcode                 gCenter, gHead, gBody, gHeaderOne, gParagraph;
    long                            mDataBytesRead;

    private final static HTMLcode   fListOfImportantCodes[] =
                                    {
                                        gStrong = new HTMLcode( "STRONG", 2100, 2101),
                                        gEmphasis = new HTMLcode( "EM", 1100, 1101),
                                        gBold = new HTMLcode( "B", 70, 71),
                                        gUnderline = new HTMLcode( "U", 30, 31),
                                        gTitle = new HTMLcode( "TITLE", 60, 61)
                                    };

    // The special chars list - 26/7/98 at 23:05 - from...
    // HTMLParser.java
    // By Ned Etcode
    // Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

    final static String         fSpecialCharsList[] =
                                {
                                    "lt", "<",
                                    "gt",   ">",
                                    "amp", "&",
                                    "quot", "\"" ,
                                    "nbsp","\u00a0",
                                    "iexcl","\u00a1",
                                    "cent","\u00a2",
                                    "pound","\u00a3",
                                    "curren","\u00a4",
                                    "yen","\u00a5",
                                    "brvbar","\u00a6",
                                    "sect","\u00a7",
                                    "uml","\u00a8",
                                    "copy","\u00a9",
                                    "ordf","\u00aa",
                                    "laquo","\u00ab",
                                    "not","\u00ac",
                                    "shy","\u00ad",
                                    "reg","\u00ae",
                                    "macr","\u00af",
                                    "deg","\u00b0",
                                    "plusmn","\u00b1",
                                    "sup2","\u00b2",
                                    "sup3","\u00b3",
                                    "acute","\u00b4",
                                    "micro","\u00b5",
                                    "para","\u00b6",
                                    "middot","\u00b7",
                                    "cedil","\u00b8",
                                    "sup1","\u00b9",
                                    "ordm","\u00ba",
                                    "raquo","\u00bb",
                                    "frac14","\u00bc",
                                    "frac12","\u00bd",
                                    "frac34","\u00be",
                                    "iquest","\u00bf",
                                    "Agrave","\u00c0",
                                    "Aacute","\u00c1",
                                    "Acirc","\u00c2",
                                    "Atilde","\u00c3",
                                    "Auml","\u00c4",
                                    "Aring","\u00c5",
                                    "AElig","\u00c6",
                                    "Ccedil","\u00c7",
                                    "Egrave","\u00c8",
                                    "Eacute","\u00c9",
                                    "Ecirc","\u00ca",
                                    "Euml","\u00cb",
                                    "Igrave","\u00cc",
                                    "Iacute","\u00cd",
                                    "Icirc","\u00ce",
                                    "Iuml","\u00cf",
                                    "ETH","\u00d0",
                                    "Ntilde","\u00d1",
                                    "Ograve","\u00d2",
                                    "Oacute","\u00d3",
                                    "Ocirc","\u00d4",
                                    "Otilde","\u00d5",
                                    "Ouml","\u00d6",
                                    "times","\u00d7",
                                    "Oslash","\u00d8",
                                    "Ugrave","\u00d9",
                                    "Uacute","\u00da",
                                    "Ucirc","\u00db",
                                    "Uuml","\u00dc",
                                    "Yacute","\u00dd",
                                    "THORN","\u00de",
                                    "szlig","\u00df",
                                    "agrave","\u00e0",
                                    "aacute","\u00e1",
                                    "acirc","\u00e2",
                                    "atilde","\u00e3",
                                    "auml","\u00e4",
                                    "aring","\u00e5",
                                    "aelig","\u00e6",
                                    "ccedil","\u00e7",
                                    "egrave","\u00e8",
                                    "eacute","\u00e9",
                                    "ecirc","\u00ea",
                                    "euml","\u00eb",
                                    "igrave","\u00ec",
                                    "iacute","\u00ed",
                                    "icirc","\u00ee",
                                    "iuml","\u00ef",
                                    "eth","\u00f0",
                                    "ntilde","\u00f1",
                                    "ograve","\u00f2",
                                    "oacute","\u00f3",
                                    "ocirc","\u00f4",
                                    "otilde","\u00f5",
                                    "ouml","\u00f6",
                                    "divide","\u00f7",
                                    "oslash","\u00f8",
                                    "ugrave","\u00f9",
                                    "uacute","\u00fa",
                                    "ucirc","\u00fb",
                                    "uuml","\u00fc",
                                    "yacute","\u00fd",
                                    "thorn","\u00fe",
                                    "yuml","\u00ff",
                                    "ensp"," ",
                                    "emsp"," ",
                                    "endash","-",
                                    "emdash","-",
                                    };


    int         GetNumberOfCodes()      { return fListOfImportantCodes.length; }
    String      GetCodeArgument()       { return fRetrievedArgument; }
    HTMLcode    GetStoredCode( int i)   { return fListOfImportantCodes[i]; }

    /*******************************************************************************
    *******************************************************************************/
    public Globals()
    {
        gHead           = new HTMLcode("HEAD");
        gBody           = new HTMLcode("BODY");
        gCenter         = new HTMLcode("CENTER");
        gFont           = new HTMLcode("FONT");
        gHTML           = new HTMLcode("HTML");
        gTableRow       = new HTMLcode("TR");
        gTableCol       = new HTMLcode("TD");
        gBreak          = new HTMLcode("BR");
        gParagraph      = new HTMLcode("P");
        gHorizontalRule = new HTMLcode("HR");
        gHeaderOne      = new HTMLcode("H1");
        gTable          = new HTMLcode("TABLE BORDER=2","/TABLE");

        Reset();
    }

    /*******************************************************************************
    *******************************************************************************/
    void Reset()
    {
        fOriginalURL = null;
        mDataBytesRead = 0;
    }

    /*******************************************************************************
    *******************************************************************************/
    int IsRecognisedCode( String sOrig)
    {
        HTMLcode    h;
        String      s = sOrig.toUpperCase();


        for ( int i = 0; i <= GetNumberOfCodes() - 1; ++i)
        {
            h = GetStoredCode(i);
            if ( s.equals(h.GetStart()))            return h.GetStartRef();
            if ( s.equals(h.GetEnd()))              return h.GetEndRef();
        }

        if ( s.equals(kLinkEndSmall))               return kFoundLinkEndResult;


        int         headingLevel;

        // see if heading level code is set
        if (( s.length() == 2) && s.startsWith("H"))        // it's H?
        {
            headingLevel = Util.PickOutNumericChar( s, 1);
            if (( headingLevel >= 1) && ( headingLevel <= kMaxHeaderLevel))
            {
                return headingLevel;
            }
        }

        // see if heading level code is reset
        if (( s.length() == 3) && s.startsWith("/H"))       // it's /H?
        {
            headingLevel = Util.PickOutNumericChar( s, 2);
            if (( headingLevel >= 1) && ( headingLevel <= kMaxHeaderLevel))
            {
                return -headingLevel;
            }
        }


        // get the file name from the link code.
        if ( CheckForLinkStart( s, sOrig, kUniversalLinkSmall))     return kFoundLinkResult;

        // 19/7/98 at 20:14 .... This section handles internal anchors
        if ( CheckForLinkStart( s, sOrig, kAnchorLinkSmall))        return kFoundAnchorResult;

        return 0;
    }

    /*******************************************************************************
    *******************************************************************************/
    boolean CheckForLinkStart( String s, String s_orig, String linkStartString)
    {
        if ( s.startsWith(linkStartString))
        {
            // it goes: LINKSTART whitespace = whitespace ARGUMENT

            int     startPos = s.lastIndexOf(kEquals);

            if (( startPos > 0) &&
                ( startPos < ( s.length() - 1)))        // 2/8/98 at 19:58 hrs
            {
                while ( Character.isWhitespace(s.charAt(++startPos)));  // skip over whitespace

                // We DON'T want the file name to be forced into UPPERCASE !
                fRetrievedArgument = s_orig.substring( startPos, s.length());
                fRetrievedArgument = Util.StripQuotesFromName(fRetrievedArgument);  // might as well!
                return true;
            }
        }
        return false;
    }

    /*******************************************************************************
        Globals::GetCodeForPriorityLevel
    *******************************************************************************/
    HTMLcode GetCodeForPriorityLevel( int priority)
    {
        Color           c;

        if ( priority < 3)          c = Color.black;
        else if ( priority > 5)     c = Color.red;
        else                        c = new Color(0,102,0);     // green. now web-safe! (0x66)

        HTMLcode        h;
        String          colString = kColourSuffix + Util.GetWebColourString(c);

        switch (priority)
        {
            case 1:     // BLACK small, bold
                h = gFont.Append( kSmallerSuffix + colString + kRightBracket + kLeftBracket +
                                    gBold.GetStart(),
                                    "><" + gBold.GetEnd());
                break;
            case 2:     // BLACK small, bold, italic
                h = gFont.Append( kSmallerSuffix + colString + kRightBracket +
                                    gBold.GetStartWithBrackets() + kLeftBracket + "I",
                                    kRightBracket + gBold.GetEndWithBrackets() + kLeftBracket + "/I");
                break;
            case 3:     // GREEN medium, plain
                h = gFont.Append(colString);
                break;
            case 4:     // GREEN medium, bold
                h = gFont.Append( colString + kRightBracket + kLeftBracket + gBold.GetStart(),
                                    kRightBracket + kLeftBracket + gBold.GetEnd());
                break;
            case 5:     // GREEN medium, bold, italic
                h = gFont.Append( colString + kRightBracket + gBold.GetStartWithBrackets() +
                                    kLeftBracket + "I",
                                    kRightBracket + gBold.GetEndWithBrackets() + kLeftBracket +
                                    "/I");
                break;
            case 6:     // RED large, plain
                h = gFont.Append( kLargerSuffix + colString);
                break;
            case 7:     // RED large, bold
                h = gFont.Append( kLargerSuffix + colString + kRightBracket + kLeftBracket +
                                    gBold.GetStart(),
                                    kRightBracket + kLeftBracket + gBold.GetEnd());
                break;
            default:    // kStandardPriority - BLACK small, plain
                h = gFont.Append( kSmallerSuffix + colString);
                break;
        }

        return h;
    }
}