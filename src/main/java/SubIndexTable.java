import java.util.Vector;

public class SubIndexTable implements Constants
{
    protected IndexerApp    fApp;
    protected Vector        fWordTable;         // of String
    protected Vector        fDataTable;         // of PageArray

    PageArray   GetPageArrayOfIndexEntry( int i)    { return (PageArray) fDataTable.elementAt(i); }
    int         GetTableSize()                      { return fWordTable.size(); }
    String      GetTheWordOfIndexEntry( int i)      { return (String) fWordTable.elementAt(i); }
    void        SetPageArrayOfIndexEntry( PageArray p, int i)   { fDataTable.setElementAt( p, i); }


    /*******************************************************************************
        SubIndexTable::SubIndexTable
    *******************************************************************************/
    public SubIndexTable( IndexerApp app)
    {
        fApp = app;
        fWordTable = new Vector();
        fDataTable = new Vector();
    }

    /*******************************************************************************
        SubIndexTable::AddEntry
    *******************************************************************************/
    void AddEntry( HTMLFile fileObj, String entry, int levelInHierarchy)
    {
        PageArray       pageArray;
        int             pos = fWordTable.indexOf(entry);    // find the word in our table
        int             phraseStartPosition;


        // Skipping a char (such as when we handle close quotes) causes the below to be 1
        // too low afterwards, each time we do it. Still, it's quite accurate.
        phraseStartPosition = fileObj.GetAnalysisPosition() + 1 - entry.length();

        if ( pos == -1)     // the entry wasn't found, so prepare to add it
        {
            if (!fApp.IsAlreadyUsingWordList())             // 20/4/98
            {
                if (fApp.IsBuildingWordList())              // 20/4/98
                {
                    pageArray = null;                       // 20/4/98
                }
                else
                {
                    pageArray = new PageArray();
                    pageArray.AddPageReference( fileObj, phraseStartPosition, levelInHierarchy);
                }
                fWordTable.addElement(entry);
                fDataTable.addElement(pageArray);
            }
        }
        else            // the entry's already there, so add the new page reference for it
        {
            pageArray = GetPageArrayOfIndexEntry(pos);

            if ( fApp.IsAlreadyUsingWordList() && ( pageArray == null))  // 20/4/98
            {
                pageArray = new PageArray();                             // 20/4/98
                SetPageArrayOfIndexEntry( pageArray, pos);               // replace dummy null
            }

            pageArray.AddPageReference( fileObj, phraseStartPosition, levelInHierarchy);
        }
    }

    /*******************************************************************************
        SubIndexTable::MergeWith        10/7/98
    *******************************************************************************/
    void MergeWith( SubIndexTable other)
    {
        if ( other != null)
        {
            for ( int i = 0; i <= other.GetTableSize() - 1; ++i)
            {
                String          otherEntry = other.GetTheWordOfIndexEntry(i);
                PageArray       otherArray = other.GetPageArrayOfIndexEntry(i);
                PageArray       ourArray;
                int             posInUs = fWordTable.indexOf(otherEntry);

                if ( posInUs == -1)
                {
                    fWordTable.addElement(otherEntry);
                    fDataTable.addElement(otherArray);
                }
                else
                {
                    ourArray = GetPageArrayOfIndexEntry(posInUs);

                    for ( int j = 0; j <= otherArray.GetListLength() - 1; ++j)
                    {
                        ourArray.AddPageReference(otherArray.GetReferenceAt(j));
                    }
                }
            }
        }
    }

    /*******************************************************************************
        SubIndexTable::RemoveIndexEntry     3/8/98
    *******************************************************************************/
    void RemoveIndexEntry( int i)
    {
        fWordTable.removeElementAt(i);
        fDataTable.removeElementAt(i);
    }

    /*******************************************************************************
        SubIndexTable::AdjustForFrequencies     3/8/98
    *******************************************************************************/
    void AdjustForFrequencies()
    {
        Vector      frequencyList = new Vector();
        Integer     temp;
        int         laxity = 4; // 4 (ie. take the middle half) is VERY tight!
        int         i, n = 0, lowerIndex, upperIndex, topLimit, bottomLimit;


        for ( i = 0; i <= GetTableSize() - 1; ++i)
        {
            temp = new Integer(GetPageArrayOfIndexEntry(i).GetListLength());
            if ( !frequencyList.contains(temp))
            {
                frequencyList.addElement(temp);
                ++n;
            }
        }

        // sort this
        Sorter.QuicksortAscending( frequencyList, null, 0, n - 1, Sorter.SortIntegers);

        lowerIndex  = (( n + 1) / laxity) + 1;
        upperIndex  = n + 1 - lowerIndex;
        topLimit    = ((Integer) frequencyList.elementAt(upperIndex-1)).intValue();
        bottomLimit = ((Integer) frequencyList.elementAt(lowerIndex-1)).intValue();

        i = 0;
        while ( i < GetTableSize())
        {
            int     len = GetPageArrayOfIndexEntry(i).GetListLength();

            if (( len < bottomLimit) ||
                ( len > topLimit))      RemoveIndexEntry(i);
            else                        ++i;
        }
    }
}