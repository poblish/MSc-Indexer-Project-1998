public class PageReference
{
    private String  fLocation, fAdjLocation;
    private int     fPageNumber, fImportance, fLevel;
    private boolean fIsTitle;

    String      GetPageLocation()           { return fLocation; }
    String      GetAdjLocation()            { return fAdjLocation; }
    int         GetPageNumber()             { return fPageNumber; }
    int         GetImportanceOfReference()  { return fImportance; }
    int         GetLevelOfReference()       { return fLevel; }
    boolean     IsPartOfTitle()             { return fIsTitle; }

    /*******************************************************************************
        PageReference::PageReference
    *******************************************************************************/
    public PageReference( String location, int pageNum, int importance, int level, boolean title)
    {
        fLocation = location;
        fPageNumber = pageNum;
        fImportance = importance;
        fLevel = level;
        fIsTitle = title;
        fAdjLocation = Util.AdjustFilenameString(fLocation);
    }
}