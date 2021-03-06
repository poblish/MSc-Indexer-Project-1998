/*
    Util.java

    A variety of utility functions generally performing the kind of string
    operations required for interpreting and analysing read-in data and tags.
*/

import java.awt.Color;
import java.text.NumberFormat;
import java.util.*;

public class Util implements Constants
{
    static char GetLastCharacter( StringBuffer b)   { return b.charAt( b.length() - 1); }
    static String   TrimFilename( String s)         { return s.trim(); }

    /*******************************************************************************
        Util::DoubleToString()
    *******************************************************************************/
    static String DoubleToString( double d, int dp)
    {
        NumberFormat    nf = NumberFormat.getInstance();

        nf.setMaximumFractionDigits(dp);

        return nf.format(d);
    }

    /*******************************************************************************
        Util::PickOutNumericChar
    *******************************************************************************/
    static int PickOutNumericChar( String s, int location)
    {
        return Character.getNumericValue(s.charAt(location));
    }

    /*******************************************************************************
        Util::ReplaceStringWithSpaces
    *******************************************************************************/
    static String ReplaceStringWithSpaces( String s, String removeThis)
    {
        String      startOfString = "";
        int     pos, oldLength = removeThis.length();

        // We use this to replace "%20" with " ".

        while (( pos = s.indexOf(removeThis)) != -1)
        {
            if ( pos > 0)   startOfString = s.substring( 0, pos);
            else            startOfString = "";

            s = startOfString + kSpace + s.substring( pos + oldLength);
        }

        return s;
    }

    /*******************************************************************************
        Util::RemoveAnchorFromURL
    *******************************************************************************/
    static String RemoveAnchorFromURL( String s)
    {
        int anchorPosition = s.lastIndexOf(kHashSign);

        if ( anchorPosition >= 0)
        {
            s = s.substring( 0, anchorPosition);
        }

        return s;
    }

    /*******************************************************************************
        Util::IsAWordBreak
    *******************************************************************************/
    static boolean IsAWordBreak( char c)
    {
        boolean     result;

        result = ( Character.isWhitespace(c)
                    || ( c == ';') || ( c == ',') || ( c == '*') || ( c == '=')
                    || ( c == '!') || ( c == ':') || ( c == '?') || ( c == kFullStopChar)
                    || ( c == '<') || ( c == '>') || ( c == '(') || ( c == ')')
                    || ( c == '{') || ( c == '}') || ( c == '[') || ( c == ']')
                    || ( c == '|') || ( c == '#') || ( c == '@') || ( c == '/')
                    || ( c == kSingleQuoteChar) || ( c == kDoubleQuoteChar));

        // 15/4/98: Returns are not breaks in IE 4.0 !
        if (( c == '\r') || ( c == '\n'))       result = false;

        return result;
    }

    /*******************************************************************************
        Util::EnsureWordStartsWithCapital
    *******************************************************************************/
    static void EnsureWordStartsWithCapital( StringBuffer s)
    {
        if ( s.length() > 0)
        {
            s.setCharAt( 0, Character.toUpperCase(s.charAt(0)));
        }
    }

    /*******************************************************************************
        Util::EnsurePhraseStartsWithCapital     13/8/98
    *******************************************************************************/
    static String EnsurePhraseStartsWithCapital( String s)
    {
        // Ugly way of changing characters in 'immutable' strings.

        if ( s.length() > 0)
        {
            return new Character( Character.toUpperCase(s.charAt(0))).toString() +
                                    s.substring( 1, s.length());
        }
        else    return "";
    }

    /*******************************************************************************
        Util::IsCapitalised     13/8/98
    *******************************************************************************/
    static boolean IsCapitalised( String s)
    {
        return s.equals(s.toUpperCase());
    }

    /*******************************************************************************
        Util::ConvertCapitalisedPhrases     13/8/98
    *******************************************************************************/
    static String ConvertCapitalisedPhrases( String s)
    {
        if (IsCapitalised(s))                   // WOMEN AND POLITICS
        {
            s = s.toLowerCase();                // women and politics
            s = EnsurePhraseStartsWithCapital(s);       // Women and politics, correct
        }
        return s;
    }

    /*******************************************************************************
        Util::StringToInteger
    *******************************************************************************/
    static int StringToInteger( String s)
    {
        return Integer.valueOf(s).intValue();
    }

    /*******************************************************************************
        Util::StripQuotesFromName
    *******************************************************************************/
    static String StripQuotesFromName( String s)
    {
        if ( s.startsWith(kDoubleQuotes) &&
                s.endsWith(kDoubleQuotes))      // Remove quotes if at both ends.
        {
            if ( s.length() > 2)    s = s.substring( 1, s.length() - 1);
            else                    s = "";     // 2/8/98 at 19:49 hrs

        }
        return s;
    }

    /*******************************************************************************
        Util::MakeNoteIconLinkString
    *******************************************************************************/
    static String MakeNoteIconLinkString( String fileName)
    {
        String      b = new String();

        if (( fileName != null) &&
            ( fileName.length() > 0) &&
            ( !fileName.equals("null")))
        {
            b = kLeftBracket + kImageStart + kDoubleQuotes + fileName + kDoubleQuotes + kRightBracket;
        }
        return b;
    }

    /*******************************************************************************
        Util::RemoveBadPrefix
    *******************************************************************************/
    static String RemoveBadPrefix( String s, String prefix) throws BadPhraseException
    {
        if ( s.equalsIgnoreCase(prefix))
        {
            throw new BadPhraseException();
        }
        else
        {
            prefix += kSpace;
            if ( s.startsWith(prefix))
            {
                s = s.substring( prefix.length(), s.length());
            }
        }
        return s;
    }

    /*******************************************************************************
    *******************************************************************************/
    static String RemoveAllBadPrefixesFrom( String s) throws BadPhraseException
    {
        // Adjust phrases which start with "Of". 14/4/98 at 23:13
        // These are NOT stopwords, just ones that shouldn't appear at the start
        // of a phrase. Added number strings. Should do all the numbers but it's too
        // much typing. 18/7/98 at 22:39

        s = RemoveBadPrefix( s, "Of");
        s = RemoveBadPrefix( RemoveBadPrefix( RemoveBadPrefix( RemoveBadPrefix(
            RemoveBadPrefix( RemoveBadPrefix( RemoveBadPrefix( RemoveBadPrefix(
            RemoveBadPrefix( RemoveBadPrefix( s, "One"), "Two"), "Three"), "Four"),
            "Five"), "Six"), "Seven"), "Eight"), "Nine"), "Ten");

        // 13/8/98

        s = RemoveBadPrefix( RemoveBadPrefix(
                                RemoveBadPrefix( s, "Page"), "Chapter"), "Section");

        return s;
    }

    /*******************************************************************************
    *******************************************************************************/
    static String AdjustAnyFileName( String s)
    {
        // Remove quotes if at both ends.
        s = StripQuotesFromName(s);

        // Replace %20 with an actual space. 23/7/98
        s = ReplaceStringWithSpaces( s, "%20");

        // We don't bother with anchors here, so remove them. 23/7/98
        s = RemoveAnchorFromURL(s);

        return TrimFilename(s);
    }

    /*******************************************************************************
    *******************************************************************************/
    static String TrimApostrophes( String s)
    {
        int         lastIndex = s.lastIndexOf(kSingleQuotes);

        if ( lastIndex >= 0)
        {
            int     charsToDelete = s.length() - lastIndex;

            if ( charsToDelete <= 3)    s = s.substring( 0, lastIndex);

            // You're  ->  You
            // O'Brien ->  no change
        }
        return s;
    }

    /*******************************************************************************
    *******************************************************************************/
    static String AdjustFilenameString( String s)
    {
        int         loc = 0;
        boolean     done = false;

        // Here, we remove directories, leaving just the filename:
        // www.apple.com/aa/b/cc/f/g/ourFile.htm  ->  ourFile.htm

        while (!done)
        {
            loc = s.indexOf( kSlash, loc);
            if ( loc < 0)   done = true;
            else
            {
                s = s.substring( loc + 1);
                loc = 0;
            }
        }
        return s;
    }

    /*******************************************************************************
    *******************************************************************************/
    static String GetWebColourString( Color c)
    {
        StringBuffer    b = new StringBuffer(kDoubleQuotes);

        b.append(kHashSign);

        // AND'ing with 0x01ffffff ensures leading zeros will get displayed
        // then convert it to a String, and remove the extra leading digit

        int     rgbValue = c.getRGB() & 0x01ffffff;
        String  rgbHexString = new String( Integer.toString( rgbValue, 16)).substring(1);

        b.append(rgbHexString);
        b.append(kDoubleQuotes);

        return b.toString();
    }

    /*******************************************************************************
    *******************************************************************************/
    static String MyMakeDateString( Date d) // to centralise MW JIT Dates bug
    {
        try
        {
            return d.toString();
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return "<<< JIT can't do Date.toString() >>>";
        }
    }
}