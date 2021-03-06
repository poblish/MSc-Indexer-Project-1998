public class Options implements Constants
{
    boolean     fTryToRemoveIngs, fKeepWithOriginalHost, fOutputHTMLFormat;
    boolean     fFindFirstWordInQuotes, fUseAStopWordList, fSortEntries;
    boolean     fMoreDetailedResults, fShouldAccessNet, fIndividualWordsOnly;
    boolean     fUseFrequencyMethod, fSmartNamePrefixes;
    int         fSortDirection, fOutputOptions, fDeepestWebFileAllowed;

    boolean     DoWeOutputHTML()            { return fOutputHTMLFormat; }
    boolean     UsingStopWordList()         { return fUseAStopWordList; }

    /*******************************************************************************
        Options::Options
    *******************************************************************************/
    public Options()
    {
        RestoreDefaults();
    }

    /*******************************************************************************
        Options::RestoreDefaults
    *******************************************************************************/
    void RestoreDefaults()
    {
        fShouldAccessNet = true;        // 21/4/98
        fKeepWithOriginalHost = true;   // 22/7/98
        fDeepestWebFileAllowed = 3;     // default is 3, I guess - 23/4/98

        fUseAStopWordList = true;

        fTryToRemoveIngs = true;        // 22/7/98 - be tougher!
        fFindFirstWordInQuotes = false;

        fSortEntries = true;
        fSortDirection = kSortAscending;
        fOutputOptions = 0;             // output ALL entries!

        fOutputHTMLFormat = true;
        fMoreDetailedResults = false;

        fIndividualWordsOnly = false;   // 3/8/98 at 17:19 hrs
        fUseFrequencyMethod = false;    // 3/8/98 at 17:19 hrs
        fSmartNamePrefixes = true;      // 24/8/98 at 21:58 hrs
    }
}