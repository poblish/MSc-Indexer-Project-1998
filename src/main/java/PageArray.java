import java.util.Vector;

public class PageArray implements Constants
{
    private Vector  fListOfPageReferences;      // of PageReference

    void    AddPageReference( PageReference ref)     { fListOfPageReferences.addElement(ref); }
    String  GetAdjPageLocationAt( int i)         { return GetReferenceAt(i).GetAdjLocation(); }
    int     GetLevelOfRef( int i)            { return GetReferenceAt(i).GetLevelOfReference(); }
    int     GetListLength()              { return fListOfPageReferences.size(); }

    /*******************************************************************************
        PageArray::PageArray
    *******************************************************************************/
    public PageArray()
    {
        fListOfPageReferences = new Vector();
    }

    /*******************************************************************************
        PageArray::GetReferenceAt
    *******************************************************************************/
    PageReference GetReferenceAt( int i)
    {
        return (PageReference) fListOfPageReferences.elementAt(i);
    }

    /*******************************************************************************
        PageArray::AddPageReference
    *******************************************************************************/
    void AddPageReference( HTMLFile fileObj, int position, int level)
    {
        AddPageReference( new PageReference( fileObj.GetFilename(),
                            position,
                            fileObj.CalculateCurrentPriority(),
                            level,
                            fileObj.GetTitleFlag()));
    }

    /*******************************************************************************
        PageArray::SortByCategory
    *******************************************************************************/
    void SortByCategory( int startIndex, int endIndex, int how)
    {
        if ( endIndex > startIndex)
        {
            // page arrays always ascending 14/4/98
            Sorter.QuicksortAscending( fListOfPageReferences, null, startIndex, endIndex, how);
        }
    }

    /*******************************************************************************
        PageArray::SortArray
    *******************************************************************************/
    void SortArray()
    {
        SortArrayByLevel( 0, GetListLength() - 1, GetListLength() - 1);
    }

    /*******************************************************************************
        PageArray::SortArrayByLevel
    *******************************************************************************/
    void SortArrayByLevel( int startIndex, int endIndex, int last)
    {
        SortByCategory( startIndex, endIndex, kSortLevels);

        int si = 0, k;

        for ( k = 1; k <= last + 1; ++k)
        {
            boolean     done = false;

            if ( k == last + 1)
            {
                done = true;
            }
            else
            {
                if ( GetLevelOfRef(k) != GetLevelOfRef( k - 1))
                {
                    done = true;
                }
            }

            if (done)
            {
                SortArrayByName( si, k - 1, last);
                si = k;
            }
        }
    }

    /*******************************************************************************
        PageArray::SortArrayByName
    *******************************************************************************/
    void SortArrayByName( int startIndex, int endIndex, int last)
    {
        SortByCategory( startIndex, endIndex, kSortFileNames);

        int si = 0, k;

        for ( k = 1; k <= last + 1; ++k)
        {
            boolean     done = false;

            if ( k == last + 1)
            {
                done = true;
            }
            else
            {
                if ( GetAdjPageLocationAt(k).compareTo(GetAdjPageLocationAt( k - 1)) != 0)
                {
                    done = true;
                }
            }
            if (done)
            {
                SortArrayByPageNo( si, k - 1);
                si = k;
            }
        }
    }

    /*******************************************************************************
        PageArray::SortArrayByPageNo
    *******************************************************************************/
    void SortArrayByPageNo( int startIndex, int endIndex)
    {
        SortByCategory( startIndex, endIndex, kSortPageNums);
    }
}