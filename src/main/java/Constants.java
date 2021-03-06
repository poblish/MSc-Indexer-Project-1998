public interface Constants
{
    static final String     kSpace = " ", kSlash = "/", kHashSign = "#", kAmpersand = "&";
    static final String     kDoubleQuotes = "\"", kSingleQuotes = "'", kEquals = "=";
    static final String     kLeftBracket = "<", kRightBracket = ">", kSemiColon = ";";
    static final String     kComma = ",";

    static final String     kUniversalLinkSmall = "A HREF", kAnchorLinkSmall = "A NAME";
    static final String     kUniversalLinkCode = kLeftBracket + kUniversalLinkSmall + kEquals;
    static final String     kLinkEndSmall = "/A", kColourSuffix = " COLOR=";
    static final String     kLargerSuffix = " SIZE=+1", kSmallerSuffix = " SIZE=-1";
    static final String     kMailtoPrefix = "MAILTO:", kImageStart = "IMG src" + kEquals;
    static final String     kLinkEnd = kLeftBracket + kLinkEndSmall + kRightBracket;

    static final String     kDefaultInputFile = "main.html";
    static final String     kDefaultOutputFile = "output.html";

    static final String     kMyComments = "Indexer &copy; Andrew Regan, 1998";
    static final String     kDocumentTitle = "Index to ";
    static final String     kAppearsInTitleString = "title";
    static final String     kIndexLabel = "Index: ";

    static final char       kSingleQuoteChar = '\'', kDoubleQuoteChar = '"', kSpaceChar = ' ';
    static final char       kFullStopChar = '.';

    static final int        kEarlierThan = -1, kSameAs = 0, kLaterThan = 1;
    static final int        kSortAscending = kLaterThan, kSortDescending = kEarlierThan;

    static final int        kFoundLinkResult = 109, kFoundAnchorResult = 104;
    static final int        kFoundLinkEndResult = 50, kMaxHeaderLevel = 6;

    static final int        kStandardPriority = 0, kMaxPriority = 7;
    static final int        kPriorityForEmphasisedPhrases = 1, kBonusForLinkPhrases = 4;
    static final int        kPriorityForStrongPhrases = 2, kPriorityForTitledPhrases = 5;
    static final int        kNoteIconThreshold = 5;

    static final int        kStartOfFile = 0, kMinimumEntryLength = 2;
    static final int        kSortLevels = 11, kSortFileNames = 21, kSortPageNums = 31;

    static final boolean    kPerformFullCheck = true, kPerformLimitedCheck = !kPerformFullCheck;
}