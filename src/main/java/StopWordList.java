import java.io.*;
import java.text.*;
import java.util.Hashtable;

public class StopWordList
{
    private Hashtable   fNoiseWordTable;

    /*******************************************************************************
        StopWordList::StopWordList
    *******************************************************************************/
    public StopWordList( String fileName)
    {
        fNoiseWordTable = new Hashtable(300);

        try
        {
            BufferedReader      reader = new BufferedReader( new FileReader(fileName));

            try
            {
                String      lineStr;

                while (( lineStr = reader.readLine()) != null)
                {
                    // neither a comment nor a blank line...
                    if (( lineStr.length() > 0) && !lineStr.startsWith("//"))
                    {
                        AddStringToNoiseList(lineStr);
                    }
                }
            }
            catch ( IOException e)
            {
            }

            try
            {
                reader.close();
            }
            catch ( IOException e)
            {
            }

            try     // now add days and months to whatever we've managed to read
            {
                DateFormatSymbols   symbols = new DateFormatSymbols();
                String              m[] = symbols.getMonths(), d[] = symbols.getWeekdays();
                int i;

                for ( i = 0; i <= m.length - 1; ++i)    AddStringToNoiseList(m[i]);
                for ( i = 0; i <= d.length - 1; ++i)    AddStringToNoiseList(d[i]);
            }
            catch ( java.lang.NoClassDefFoundError e)
            {
                // The Apple MRJ threw this at me in Internet Explorer 4.0; 14/4/98 @ 0255
                // If we get it, just don't bother with the month & day names
            }
        }
        catch ( FileNotFoundException e)
        {
            // No entries in the Stop Word table if a bad filename is given
        }
    }

    /*******************************************************************************
        StopWordList::AddStringToNoiseList
    *******************************************************************************/
    private void AddStringToNoiseList( String s)
    {
        fNoiseWordTable.put( new Integer(s.hashCode()), s);
    }

    /*******************************************************************************
        StopWordList::IsPhraseInStopList
    *******************************************************************************/
    boolean IsPhraseInStopList( String s)
    {
        return fNoiseWordTable.containsKey( new Integer(s.hashCode()));  // 19/7/98 at 16:58
    }
}