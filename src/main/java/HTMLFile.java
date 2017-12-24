import java.io.*;
import java.util.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.net.MalformedURLException;
import java.net.URL;

public class HTMLFile implements Constants
{
    private static IndexerApp       fApp;
    private static Globals          g;
    private static Options          fOptions;
    private static StopWordList     fOurStopWordList;
    private SubIndexTable           fThisDocumentsIndex;
    private HTMLFile                fParent;
    private URL                     fThisFilesLocation;
    private BufferedReader          fFileReader;
    private StringCharacterIterator fIterator;
    private StringBuffer            fCurrentPhrase, fCurrentWord, fCurrentCode;
    private String                  fHTMLData, fNextLink, fStoredFileName;
    private int[]                   fLastHeaderLevelArray;
    private int                     fDocumentsLevelInHierarchy;
    private int                     fPosition, fCurrentHeaderLevel;
    private int                     fCharsIntoTheSpecialCode, fStartPosForSpecialCode;
    private boolean                 fInWord, fStartingNewWord, fFoundWordBreak, fWithinQuotes;
    private boolean                 fCarryOnAsNormal, fBuildUpPhrase, fOnlyJustStartedFile;
    private boolean                 fEverythingOK, fFoundAnotherLink, fInCode, fInSpecialCharCode;
    private boolean                 fDontAddThisCharacter;
    private boolean                 fEmphasisFlag, fStrongFlag, fLinkPriorityFlag, fTitleFlag;
    private boolean                 fBoldFlag, fUnderlineFlag;

    int             GetAnalysisPosition()           { return fIterator.getIndex(); }
    SubIndexTable   GetDocumentsIndex()             { return fThisDocumentsIndex; }
    String          GetFilename()                   { return fStoredFileName; }
    int             GetLevelInHierarchy()           { return fDocumentsLevelInHierarchy; }
    char            GetNextCharacter()              { return GetNextPrevCharacter(1); }
    String          GetNextLink()                   { return fNextLink; }
    HTMLFile        GetParent()                     { return fParent; }
    boolean         GetTitleFlag()                  { return fTitleFlag; }
    void            InitialiseCurrentPhrase()       { fCurrentPhrase = new StringBuffer(); }
    void            InitialiseCurrentWord()         { fCurrentWord = new StringBuffer(); }
    void            SetFilenameTo( String s)        { fStoredFileName = s; }
    void            SetFileObjectsURL( URL loc)     { fThisFilesLocation = loc; }
    void            SetLevelInHierarchy( int level) { fDocumentsLevelInHierarchy = level; }


    /*******************************************************************************
        HTMLFile::HTMLFile
    *******************************************************************************/
    public HTMLFile( IndexerApp app, String fileName, StopWordList wl)
    {
        // We use this constructor when indexing the Key Word file
        this( app, fileName, null, null, 0, wl);
    }

    /*******************************************************************************
        HTMLFile::HTMLFile
    *******************************************************************************/
    public HTMLFile( IndexerApp app, String fileName, HTMLFile parent, SubIndexTable wordListIndex, int docsInList, StopWordList wl)
    {
        fApp = app;
        g = fApp.GetApplicationGlobals();           // use the global one
        fOptions = fApp.GetApplicationOptions();    // use the global options

        SetFilenameTo(fileName);
        fOurStopWordList = wl;
        fParent = parent;
        SetFileObjectsURL(null);
        fFileReader = null;
        fEverythingOK = false;

        // If the number of active documents is zero, our level must be 1, i.e. the top!
        SetLevelInHierarchy( docsInList + 1);

        // Create an empty index structure for each document in the presentation
        fThisDocumentsIndex = new SubIndexTable(fApp);

        if ( wordListIndex != null)
        {
            // Make our index the same as the word list index. 25/7/98 at 16:40
            fThisDocumentsIndex.MergeWith(wordListIndex);
        }
    }

    /*******************************************************************************
        HTMLFile::ReadInDocumentData
    *******************************************************************************/
    void ReadInDocumentData() throws java.io.IOException
    {
        StringBuffer    buf = new StringBuffer();
        String          lineStr;
        char            charAtEndOfLastLine = '\0';


        while (( lineStr = fFileReader.readLine()) != null)
        {
            // General cleansing 18/8/98: if there's whitespace (like a tab) at the end of
            // the last line AND the start of this line, we should lose it from both ends
            // because whitespace runs count as one in HTML (I think). A single space will
            // be added later, if required.

            lineStr = lineStr.trim();

            if ( lineStr.length() > 0)
            {
                if ( charAtEndOfLastLine != '\0')
                {
                    // If we get "New" as one line and "Zealand" as the next, ie. the author
                    // has forgotten to put a space after the first word or before the
                    // second word, we put one in ourselves, like browsers do:
                    lineStr = kSpace + lineStr;
                }
                buf.append(lineStr);
                if ( lineStr.length() >= 1)         // This test 22/8/98 at 17:23
                        charAtEndOfLastLine = lineStr.charAt( lineStr.length() - 1);
            }
        }

        fFileReader.close();                        // we've finished with the file, close it
        fFileReader = null;

        g.mDataBytesRead += (long) buf.length();    // monitor the number of bytes read

        fHTMLData = new String(buf);
        fHTMLData.trim();               // one last check to remove trailing/leading spaces/tabs

        if ( fHTMLData.length() > 0)    // make sure we've got SOME data!
        {
            InitAnalysisVariables();
            fEverythingOK = true;
        }
    }

    /*******************************************************************************
        HTMLFile::InitAnalysisVariables
    *******************************************************************************/
    void InitAnalysisVariables()
    {
        fIterator = new StringCharacterIterator(fHTMLData);
        fCurrentCode = new StringBuffer();
        InitialiseCurrentWord();
        InitialiseCurrentPhrase();
        fFoundWordBreak = fInWord = fInCode = fWithinQuotes = fFoundAnotherLink = false;
        fInSpecialCharCode = fDontAddThisCharacter = false;
        fStartingNewWord = fBuildUpPhrase = fOnlyJustStartedFile = true;
        fPosition = kStartOfFile;
        InitialiseHTMLCodeFlags();
    }

    /*******************************************************************************
        Globals::InitialiseHTMLCodeFlags
    *******************************************************************************/
    void InitialiseHTMLCodeFlags()
    {
        fCurrentHeaderLevel = 0;
        fLastHeaderLevelArray = new int[ kMaxHeaderLevel + 1];
        fEmphasisFlag = fStrongFlag = fLinkPriorityFlag = fTitleFlag = false;
        fUnderlineFlag = fBoldFlag = false;
    }

    /*******************************************************************************
        HTMLFile::SetupDocument
    *******************************************************************************/
    void SetupDocument() throws BadHTMLFileException
    {
        try
        {
            AdjustCurrentFileName();
            if ( IsFileNameOK())
            {
                HTMLFile            mum;

                ////////// 21/4/98 //////////

                boolean             wantRemoteFile = fOptions.fShouldAccessNet;
                boolean             isAnAbsoluteURL = false;

                if (wantRemoteFile)
                {
                    try
                    {
                        // If we successfully create the URL object, that means we've got
                        // a *genuine* absolute URL. Try this first of all.

                        SetFileObjectsURL( new URL(GetFilename()));
                        isAnAbsoluteURL = true;     // it IS an absolute URL, even if we can't connect!
                        TryToConnectToLocation();
                    }
                    catch ( MalformedURLException e)
                    {}
                    catch ( java.io.IOException e)
                    {}

                    if (!isAnAbsoluteURL)           // either a relative URL or a local file
                    {
                        wantRemoteFile = false;     // open local file if we fail down here

                        try
                        {
                            if (( mum = GetParent()) != null)   // document has a parent
                            {
                                URL         mumsLocation;

                                if (( mumsLocation = mum.fThisFilesLocation) != null)
                                {
                                    // 22/4/98 - If 'mum' is 'www.apple.com/doc.html'
                                    // and this filename is 'MAIN.HTM' then surely what we want
                                    // is not 'MAIN.HTM' on my computer but the following:
                                    // 'www.apple.com/MAIN.HTM'. So replace filename of parent
                                    // with the one here, and connect to that url!!

                                    String      mumsFile;
                                    String      prefix = new String();

                                    if (( mumsFile = mumsLocation.getFile()) != null)
                                    {
                                        int     lastSlashPos = mumsFile.lastIndexOf(kSlash);

                                        if ( lastSlashPos != -1)
                                        {
                                            prefix = mumsFile.substring( 0, lastSlashPos);
                                        }
                                    }

                                    SetFileObjectsURL( new URL( mumsLocation.getProtocol(),
                                                                mumsLocation.getHost(),
                                                                GetFilenameWithSlash(prefix)));
                                    wantRemoteFile = true;  // it WAS a relative url.

                                    // Now adjust the file name so it appears as an absolute url
                                    // rather than something that looks like it may be a local file.
                                    SetFilenameTo(fThisFilesLocation.toString());

                                    TryToConnectToLocation();
                                }
                            }
                        }
                        catch ( MalformedURLException e)    // have to open file normally instead!
                        {}
                        catch ( java.io.IOException e)
                        {}
                    }
                }

                /////////////////////////////

                if (!wantRemoteFile)
                {
                    // Try opening the LOCAL file! Either because trying to open a remote
                    // file failed, or because remote mode is off and everything's regarded
                    // as local.

                    try
                    {
                        fFileReader = new BufferedReader( new FileReader(GetFilename()));
                        ReadInDocumentData();
                    }
                    catch ( Exception e)
                    {
                        // May be a *relative* local reference. 23/7/98 at 13:58

                        if (( mum = GetParent()) != null)
                        {
                            String      prefix = "", mumsFile = mum.GetFilename();
                            int         lastSlashPos = mumsFile.lastIndexOf(kSlash);

                            if ( lastSlashPos != -1)
                                        prefix = mumsFile.substring( 0, lastSlashPos);

                            SetFilenameTo(GetFilenameWithSlash(prefix));

                            fFileReader = new BufferedReader( new FileReader(GetFilename()));
                            ReadInDocumentData();
                        }
                    }
                }
            }
        }
        catch (java.io.IOException e)
        {
            fEverythingOK = false;
        }

        if (!fEverythingOK)         throw new BadHTMLFileException();
    }

    /*******************************************************************************
        HTMLFile::TryToConnectToLocation
    *******************************************************************************/
    void TryToConnectToLocation() throws java.io.IOException
    {
        if ( GetLevelInHierarchy() > fOptions.fDeepestWebFileAllowed)
        {
            // We do NOT read REMOTE files deeper than that!
            throw new IOException();
        }

        if (fOptions.fKeepWithOriginalHost)         // 22/7/98 at 14:34
        {
            if ( g.fOriginalURL == null)            // store the name of the first host we use
                    g.fOriginalURL = new URL(fThisFilesLocation.toString());
            else
            {
                String      originalHost = g.fOriginalURL.getHost();

                // see if this host is the same as the first one we used

                if ( !originalHost.equalsIgnoreCase(fThisFilesLocation.getHost()))
                {
                    System.out.println( fApp.GetString("outside_a") + fThisFilesLocation +
                                        fApp.GetString("outside_b") + originalHost +
                                        kRightBracket);
                    throw new IOException();
                }
            }
        }

        // Show user if we're connecting to a url
        System.out.println( fApp.GetString("connecting") + fThisFilesLocation + kRightBracket);

        // Try to read from it
        fFileReader = new BufferedReader( new InputStreamReader(fThisFilesLocation.openStream()));
        ReadInDocumentData();
    }

    /*******************************************************************************
        HTMLFile::AnalyseDocument
    *******************************************************************************/
    boolean AnalyseDocument()
    {
        boolean         checkedChar;

        fFoundAnotherLink = false;

        for ( char c = fIterator.setIndex(fPosition);
                    c != CharacterIterator.DONE && (!fFoundAnotherLink);
                    c = SkipToNextCharacter())
        {
            checkedChar = false;

            if ( c == '<')          // Start reading html code now
            {
                // Do this, as a bracket is also a word break!
                checkedChar = HandleCharacterMinimal(c);
                StartReadingHTMLCode();
            }
            else
            {
                if ( c == '&')      // Start reading special html character now
                {
                    // Do this, as a bracket is also a word break!
                    fInSpecialCharCode = fDontAddThisCharacter = true;
                    checkedChar = HandleCharacterMinimal(c);
                    fDontAddThisCharacter = false;
                    fCharsIntoTheSpecialCode = 0;
                    fStartPosForSpecialCode = GetAnalysisPosition() + 1;
                }
                else
                {
                    if (fInSpecialCharCode)
                    {
                        boolean     success = LookForSpecialCharacter();

                        if (!success)
                        {
                            fInSpecialCharCode = false;
                            checkedChar = HandleCharacter(c);
                        }
                    }
                    else
                    {
                        if ( fInCode && ( c == '>'))    FinishReadingHTMLCode();
                        else
                        {
                            if (fInCode)                fCurrentCode.append(c);
                            else
                            {
                                if (!checkedChar)       checkedChar = HandleCharacter(c);
                            }
                        }
                    }
                }
            }
        }

        return fFoundAnotherLink;
    }

    /*******************************************************************************
        HTMLFile::LookForSpecialCharacter
    *******************************************************************************/
    boolean LookForSpecialCharacter()
    {
        String      each;
        char        replacementChar;
        int         eachLen;
        boolean     success = false;


        ++fCharsIntoTheSpecialCode;

        // If any of the special chars in the list match what we've read so far (since the
        // & char) then we call that a WIN. Otherwise, let's wait until another char's been
        // read and try again then. This allows us to match special chars even when they've
        // not been properly terminated (with a ;), just like web browsers do. If we'd used
        // a word break iterator we'd never be able to handle that eventuality.

        for ( int count = 0; count <= g.fSpecialCharsList.length - 2; count += 2)
        {
            each = (String) g.fSpecialCharsList[count];
            eachLen = Math.min( each.length(), fCharsIntoTheSpecialCode);

            if ( fHTMLData.regionMatches( fStartPosForSpecialCode, each, 0, eachLen))
            {
                if ( fCharsIntoTheSpecialCode >= each.length())
                {
                    // If there is a closing semi-colon, skip over it.
                    if ( SkipToNextCharacter() == kSemiColon.charAt(0))     SkipToNextCharacter();

                    // Handle the new character that we've put in to replace
                    // that code. Skip back, then continue adding after that.
                    replacementChar = g.fSpecialCharsList[count+1].charAt(0);

                    HandleSpecialCharacter( replacementChar, kAmpersand + each + kSemiColon);
                    SkipBackACharacter();

                    fInSpecialCharCode = false;
                }
                success = true;
                break;
            }
        }

        return success;
    }

    /*******************************************************************************
        HTMLFile::StartReadingHTMLCode
    *******************************************************************************/
    void StartReadingHTMLCode()
    {
        fInCode = true;
        fCurrentCode = new StringBuffer();  // we'll add the code's characters to this
    }

    /*******************************************************************************
        HTMLFile::FinishReadingHTMLCode
    *******************************************************************************/
    void FinishReadingHTMLCode()
    {
        fInCode = false;
        // Pass the string to these functions to verify and act upon it.
        HandleThisHTMLCode(g.IsRecognisedCode(fCurrentCode.toString()));
    }

    /*******************************************************************************
        HTMLFile::HandleThisHTMLCode
    *******************************************************************************/
    void HandleThisHTMLCode( int which)
    {
        if (( which >= 1) && ( which <= kMaxHeaderLevel))
        {
            // IE 4.0 doesn't do this test, hence: <H1><H2><H2>  =>  last = 2, should be 1
            if ( which != fCurrentHeaderLevel)
            {
                // This seems to be exactly what IE 4.0 does.
                fLastHeaderLevelArray[which] = fCurrentHeaderLevel;
                fCurrentHeaderLevel = which;
            }
        }
        if (( which >= -kMaxHeaderLevel) && ( which <= -1))
        {
            // InternetExplorer 4.0 does just this - not very clever though!
            fCurrentHeaderLevel = fLastHeaderLevelArray[fCurrentHeaderLevel];
        }


        // Set the flags that influence prioritites

        for ( int i = 0; i <= g.GetNumberOfCodes() - 1; ++i)
        {
            HTMLcode    h = g.GetStoredCode(i);
            boolean     isOn = ( which == h.GetStartRef());

            if ( isOn || ( which == h.GetEndRef()))
            {
                if ( h == g.gStrong)
                {
                    fStrongFlag = isOn;         return;     // <STRONG>
                }
                if ( h == g.gEmphasis)
                {
                    fEmphasisFlag = isOn;       return;     // <EM>
                }
                if ( h == g.gTitle)
                {
                    fTitleFlag = isOn;          return;     // <TITLE>
                }
                if ( h == g.gBold)
                {
                    fBoldFlag = isOn;           return;     // <B>
                }
                if ( h == g.gUnderline)
                {
                    fUnderlineFlag = isOn;      return;     // <U>
                }
            }
        }

        switch (which)
        {
            case kFoundLinkResult:
                fNextLink = Util.AdjustAnyFileName(g.GetCodeArgument());    // 23/7/98 at 19:07
                // being in a link increments priority level
                fLinkPriorityFlag = fFoundAnotherLink = true;
                break;

            case kFoundAnchorResult:
                // Anchor names we will add to the index, unlike with URLs, so set the current
                // phrase to the code's argument (the name), then add it.
                fCurrentPhrase = new StringBuffer(g.GetCodeArgument());
                HandleFoundPhrase();
                // being in a link increments priority level
                fLinkPriorityFlag = true;
                break;

            case kFoundLinkEndResult:
                // we're outside the link now, so decrement priority level
                fLinkPriorityFlag = false;
                break;
        }
    }

    /*******************************************************************************
        HTMLFile::AreAtLastCharacter
    *******************************************************************************/
    boolean AreAtLastCharacter()
    {
        return ( GetAnalysisPosition() == ( fHTMLData.length() - 1));
    }

    /*******************************************************************************
        HTMLFile::HandleSpaceChar
    *******************************************************************************/
    void HandleSpaceChar( boolean isLastChar)
    {
        if (!isLastChar)
        {
            char        nextChar = GetNextCharacter();

            // if the char after the space is a capital, we should keep building the phrase
            // unless we only want individual words.

            if ( !fOptions.fIndividualWordsOnly &&
                ( Character.isUpperCase(nextChar) || fWithinQuotes))
            {
                // don't break up the phrase
                fCarryOnAsNormal = false;
                fBuildUpPhrase = true;
                fFoundWordBreak = false;
            }
        }
    }

    /*******************************************************************************
        HTMLFile::HandleSpecialCharacter
    *******************************************************************************/
    boolean HandleSpecialCharacter( char c, String fullVersion)
    {
        return HandleCharacter( c, true, fullVersion, kPerformFullCheck);
    }

    /*******************************************************************************
        HTMLFile::HandleCharacterMinimal
    *******************************************************************************/
    boolean HandleCharacterMinimal( char c)
    {
        return HandleCharacter( c, false, "", kPerformLimitedCheck);
    }

    /*******************************************************************************
        HTMLFile::HandleCharacter
    *******************************************************************************/
    boolean HandleCharacter( char c)
    {
        return HandleCharacter( c, false, "", kPerformFullCheck);
    }

    /*******************************************************************************
        HTMLFile::HandleCharacter
    *******************************************************************************/
    boolean HandleCharacter( char c, boolean hasFullVersion, String fullVersion, boolean fullCheck)
    {
        if ( fullCheck == kPerformFullCheck)        SeeIfWeMustStartNewPhrase(c);

        boolean             isLastChar = AreAtLastCharacter();
        boolean             killedInvalidWord = false;
        boolean             isBreak = Util.IsAWordBreak(c);

        if ( isLastChar || isBreak)
        {
            fStartingNewWord = fCarryOnAsNormal = true;
            fInWord = false;
            fWithinQuotes = false;      // we would only ever need the 1st word in quotes

            if ( c == kSpaceChar)
            {
                // if we have a space then another capital letter, this lets us continue
                // building the phrase
                HandleSpaceChar(isLastChar);
            }
            else
            {
                if ( c == kFullStopChar)
                {
                    // 19/8/98. When dealing with Americans and their middle initials,
                    // if there's a full stop after it, we would erroneously split
                    // "Harry S. Truman" into "Harry S" and "Truman". Now, when we get
                    // a full stop we look for a capital in front and a space (or
                    // another full stop for multiple initials!) in front of that.
                    // If so, we skip the space after the full stop, and tell the
                    // parser not to split the phrase up, so that when we get to the "T"
                    // we can join it on.

                    char    beforeThat = GetNextPrevCharacter(-2);

                    if ( Character.isUpperCase(GetNextPrevCharacter(-1)) &&
                            (( beforeThat == kSpaceChar) || ( beforeThat == kFullStopChar)))
                    {
                        if ( GetNextCharacter() == kSpaceChar)      SkipToNextCharacter();

                        // don't break up the phrase
                        fCarryOnAsNormal = false;
                        fBuildUpPhrase = true;
                        fFoundWordBreak = false;
                    }
                }
            }

            if (fOptions.fFindFirstWordInQuotes)    HandleStartOfQuotes( c, isLastChar);

            Util.EnsureWordStartsWithCapital(fCurrentWord);

            // Word must be one char or more. It will only be added if there's no stoplist,
            // or there is one and our word's not in it!  22/7/98 at 17:21

            if (( fCurrentWord.length() > 0) &&
                ( !fOptions.UsingStopWordList() ||
                ( fOptions.UsingStopWordList() &&
                        !fOurStopWordList.IsPhraseInStopList(fCurrentWord.toString()))))
                                                    fCurrentPhrase.append(fCurrentWord);
            else
            {
                killedInvalidWord = true;
                fBuildUpPhrase = false;
                fFoundWordBreak = true;
            }

            InitialiseCurrentWord();

            if (fCarryOnAsNormal)
            {
                fFoundWordBreak = true;             // carry on as normal
                if (!fOnlyJustStartedFile)          fBuildUpPhrase = false;
            }
        }
        else
        {
            if ( !Character.isWhitespace(c) && !fDontAddThisCharacter)
            {
                // Not a break - so its a valid character.
                HandleRealCharacter( c, hasFullVersion, fullVersion);
            }
        }

        if ( isLastChar && !killedInvalidWord && !isBreak)
                                                    fCurrentPhrase.append(c);

        if (fFoundWordBreak)                        HandleFoundPhrase();

        return true;
    }

    /*******************************************************************************
        HTMLFile::SeeIfWeMustStartNewPhrase
    *******************************************************************************/
    void SeeIfWeMustStartNewPhrase( char c)
    {
        if ( fStartingNewWord &&
            ( Character.isUpperCase(c) || fWithinQuotes) &&
            ( fBuildUpPhrase || !fInWord))      // allows caps within words
        {
            fInWord = true;

            if (fBuildUpPhrase)
            {
                // don't add space after last word if we're just starting!
                if ( fCurrentPhrase.length() > 0)       fCurrentPhrase.append(kSpace);
            }
        }

        if (fStartingNewWord)       // whether it be a Capitalised word or not
        {
            if (!fBuildUpPhrase)    fCurrentPhrase = new StringBuffer(c);
        }
    }

    /*******************************************************************************
        HTMLFile::HandleStartOfQuotes
    *******************************************************************************/
    void HandleStartOfQuotes( char currentChar, boolean isLastChar)
    {
        if (!isLastChar)
        {
            char    nextChar = GetNextCharacter();

            if (( nextChar == kSingleQuoteChar) || ( nextChar == kDoubleQuoteChar))
            {
                if (Character.isWhitespace(currentChar))
                {
                    fWithinQuotes = true;
                    SkipToNextCharacter();
                }
            }
        }
    }

    /*******************************************************************************
        HTMLFile::HandleFoundPhrase
    *******************************************************************************/
    void HandleFoundPhrase()
    {
        try
        {
            if ( fCurrentPhrase.length() < kMinimumEntryLength)
                                                throw new BadPhraseException();

            // 23/7/98 at 19:44 hrs
            // Nothing that starts or ends with a number allowed, especially numbers themselves!
            if ( Character.isDigit(fCurrentPhrase.charAt(0)) ||
                    Character.isDigit(fCurrentPhrase.charAt( fCurrentPhrase.length() - 1)))
                                                throw new BadPhraseException();

            String      curPhraseString = fCurrentPhrase.toString();


            // TRIM any phrases ending in apostrophes or apostrophe-S's
            curPhraseString = Util.TrimApostrophes(curPhraseString);
            curPhraseString = Util.RemoveAllBadPrefixesFrom(curPhraseString);

            // We may have removed a "You" from the end of a phrase, but this may have left
            // a trailing space. This should sort them out!   15/4/98 at 01:43
            curPhraseString = curPhraseString.trim();

            if ( curPhraseString.length() < kMinimumEntryLength)
                                        throw new BadPhraseException();

            // Remove entries ending in "ing" (probably verbs), or "ally" (adverbs?)
            if (fOptions.fTryToRemoveIngs)
            {
                if (( curPhraseString.indexOf(kSpace) == -1) &&     // must be one word only
                        ( curPhraseString.endsWith(fApp.GetString("ing_suffix")) ||
                            curPhraseString.endsWith(fApp.GetString("ally_suffix"))))
                                        throw new BadPhraseException();
            }

            if ( !Character.isUpperCase(curPhraseString.charAt(0)))
            {
                curPhraseString = Util.EnsurePhraseStartsWithCapital(curPhraseString);
            }

            // Another rule is that we'll only allow two-letter words if they are all
            // in capitals, ie. a company name, perhaps, like BT.
            if ( curPhraseString.length() == 2)
            {
                if (!Util.IsCapitalised(curPhraseString))   throw new BadPhraseException();
            }

            // Phrase is only ok if there's no stoplist,
            // or there is one and our phrase isn't in it!  22/7/98 at 17:21

            if ( !fOptions.UsingStopWordList() ||
                ( fOptions.UsingStopWordList() &&
                        !fOurStopWordList.IsPhraseInStopList(curPhraseString)))
            {
                if ( curPhraseString.length() > 4)
                {
                    // 13/8/98. Make an effort to turn "WOMEN AND POLITICS" into
                    // "Women and politics". We must assume that anything under 4 letters
                    // should REALLY be capitalised, eg. an abbreviation.
                    curPhraseString = Util.ConvertCapitalisedPhrases(curPhraseString);
                }

                fThisDocumentsIndex.AddEntry( this, curPhraseString, GetLevelInHierarchy());

                // On 17/4/98 at 01:06 "Egg <H1>" produced "Egg (main.html) 0, 1."
                // because the phrase buffer wasn't inited when the left bracket was
                // tested as a word break (because the 'full' flag was off). !!!!
                InitialiseCurrentPhrase();      // initialise this
            }
        }
        catch ( BadPhraseException e)
        {}
    }

    /*******************************************************************************
        HTMLFile::HandleRealCharacter
    *******************************************************************************/
    void HandleRealCharacter( char c, boolean hasFullVers, String fullVersion)
    {
        fOnlyJustStartedFile = fFoundWordBreak = fStartingNewWord = false;

        if (fInWord)
        {
            if ( hasFullVers && fOptions.DoWeOutputHTML())
            {
                // In this case we are using the special character code's "char" value to
                // determine whether it's a capital/word break etc, and we'll output it in the
                // plain text version, but we MUST restore the original code for the HTML file
                // otherwise the "char" will just appear as asterisks. 28/7/98 4-5pm
                fCurrentWord.append(fullVersion);
            }
            else    fCurrentWord.append(c);
        }
    }

    /*******************************************************************************
        HTMLFile::CalculateCurrentPriority
    *******************************************************************************/
    int CalculateCurrentPriority()
    {
        int         i, priority = kStandardPriority;

        if (fEmphasisFlag)                      priority = kPriorityForEmphasisedPhrases;
        if (fStrongFlag)                        priority = kPriorityForStrongPhrases;
        if (GetTitleFlag())                     priority = kPriorityForTitledPhrases;

        // 20/4/98 - Why not use "+=" ?? Because it looks like there's a compiler bug.
        if (fLinkPriorityFlag)                  priority = priority + kBonusForLinkPhrases;

        // If (fCurrentHeaderLevel == 1) H1 is set, which corresponds to importance 7, so...
        if ( fCurrentHeaderLevel > 0)           priority = 8 - fCurrentHeaderLevel;

        if (fBoldFlag)                          ++priority;
        if (fUnderlineFlag)                     ++priority;

        if ( priority < kStandardPriority)      priority = kStandardPriority;
        if ( priority > kMaxPriority)           priority = kMaxPriority;

        return priority;
    }

    /*******************************************************************************
        HTMLFile::GetFilenameWithSlash
    *******************************************************************************/
    String GetFilenameWithSlash( String prefix)
    {
        String      fn = GetFilename();

        // Add a slash to start of filename, and possibly try to construct an absolute url.

        if ( !fn.startsWith(kSlash))        fn = kSlash + fn;
        return ( prefix + fn);
    }

    /*******************************************************************************
        HTMLFile::AdjustCurrentFileName
    *******************************************************************************/
    void AdjustCurrentFileName()
    {
        SetFilenameTo( Util.AdjustAnyFileName(GetFilename()));
    }

    /*******************************************************************************
        HTMLFile::IsFileNameOK
    *******************************************************************************/
    boolean IsFileNameOK()
    {
        String      upperVersion = GetFilename().toUpperCase();

        if ( upperVersion.startsWith(kMailtoPrefix))        return false;

        return ( upperVersion.endsWith(".HTM") || upperVersion.endsWith(".HTML"));
    }

    /*******************************************************************************
        HTMLFile::GetNextPrevCharacter
    *******************************************************************************/
    char GetNextPrevCharacter( int offset)
    {
        return fHTMLData.charAt( GetAnalysisPosition() + offset);
    }

    /*******************************************************************************
        HTMLFile::SkipToNextCharacter
    *******************************************************************************/
    char SkipToNextCharacter()
    {
        ++fPosition;                // we need to keep track, so we can re-enter later
        return fIterator.next();
    }

    /*******************************************************************************
        HTMLFile::SkipBackACharacter
    *******************************************************************************/
    void SkipBackACharacter()
    {
        --fPosition;                // we need to keep track, so we can re-enter later
        fIterator.previous();
    }
}