import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.String;
import java.util.Vector;

public class CompleteIndexTable extends SubIndexTable
{
    protected Globals               g;
    protected Options               fOptions;
    protected HTMLcode              fPriorityCodeList[];
    protected BufferedWriter        fWriter;
    protected Vector                fWeightForEachTerm_SJ, fWeightForEachTerm_Sa;
    protected Vector                fMinWeightForEachTerm_Sa;
    protected String                fFirstColumnStartCodes, fSecondColumnStartCodes;
    protected String                fNoteIconString;
    protected int                   fNumEntriesWritten, fNumberOfDocumentsInIndex;
    protected boolean[]             fThisEntryIsEligible;

    void    SetTheWordOfIndexEntry( String s, int i)    { fWordTable.setElementAt( s, i); }


    /*******************************************************************************
        CompleteIndexTable::CompleteIndexTable
    *******************************************************************************/
    public CompleteIndexTable( IndexerApp app)
    {
        super(app);
        g = app.GetApplicationGlobals();
        fOptions = app.GetApplicationOptions();

        fNumEntriesWritten = 0;
        fNumberOfDocumentsInIndex = 0;

        fWeightForEachTerm_SJ = null;
        fWeightForEachTerm_Sa = null;
        fMinWeightForEachTerm_Sa = null;
        fThisEntryIsEligible = null;

        // Each priority level (0-max) has a code linked with it.
        // Calculate each code in advance, store in array for easy access
        // during the writing of the output file.

        fPriorityCodeList = new HTMLcode[ kMaxPriority + 1];
        for ( int i = 0; i <= kMaxPriority; ++i)
                            fPriorityCodeList[i] = g.GetCodeForPriorityLevel(i);
    }

    /*******************************************************************************
        CompleteIndexTable::MergeWith   10/7/98
    *******************************************************************************/
    void MergeWith( SubIndexTable other)
    {
        if ( other != null)
        {
            super.MergeWith(other);
            ++fNumberOfDocumentsInIndex;    // just add this behaviour, for the final index.
        }
    }

    /*******************************************************************************
        CompleteIndexTable::WriteOut
    *******************************************************************************/
    boolean WriteOut( String picFileName, String destFilename)
    {
        try
        {
            fWriter = new BufferedWriter( new FileWriter(destFilename));

            //////////

            StringBuffer    letterStartsBuffer = new StringBuffer();
            StringBuffer    finalIndex = new StringBuffer(kIndexLabel);

            if (fOptions.DoWeOutputHTML())
            {
                String      base = kLeftBracket + g.gTableCol.GetStart() + " BGCOLOR" + kEquals;

                // Set these up now to save having to do it 'n' times.

                fFirstColumnStartCodes  = base +
                                            Util.GetWebColourString( new Color(153,153,153)) +
                                            kRightBracket + kSpace;
                fSecondColumnStartCodes = base +
                                            Util.GetWebColourString( new Color(204,204,204)) +
                                            kRightBracket + kSpace;
                //////////

                WriteOutHeader();
                fNoteIconString = Util.MakeNoteIconLinkString(picFileName);

                //////////

                letterStartsBuffer.append(fSecondColumnStartCodes);         // <TD BGCOLOR...
                letterStartsBuffer.append(g.gHeaderOne.GetStartWithBrackets());
                letterStartsBuffer.append(g.gBold.GetStartWithBrackets());
                letterStartsBuffer.append( kLeftBracket +
                                            g.gFont.GetStart() + kColourSuffix +
                                            Util.GetWebColourString(Color.blue) +
                                            kRightBracket);
            }

            //////////

            int     i;


            fThisEntryIsEligible = new boolean[GetTableSize()];

            // this isn't relevant for only one document
            if ( fNumberOfDocumentsInIndex > 1)
            {
                CalculateDocumentWeights();
                DetermineWhichEntriesToOutput();
            }
            else    // for one document only, ALL entries are output
            {
                for ( i = 0; i <= GetTableSize() - 1; ++i)      fThisEntryIsEligible[i] = true;
            }

            //

            PageArray       array;
            String          eachEntry, firstLetterString, lastFirstLetterString = "*";
            char            firstLetterChar = '*';

            for ( i = 0; i <= GetTableSize() - 1; ++i)
            {
                // If no page array, we're just an entry from the word list that didn't
                // get any matches at all. Pretend we don't exist.

                if ((( array = GetPageArrayOfIndexEntry(i)) != null) &&
                        fThisEntryIsEligible[i])
                {
                    eachEntry = GetTheWordOfIndexEntry(i);

                    if (fOptions.DoWeOutputHTML())
                    {
                        // 10/8/98 at 18:07
                        firstLetterString = "";

                        if ( eachEntry.charAt(0) == kAmpersand.charAt(0))
                        {
                            int     lastSemi = eachEntry.indexOf( kSemiColon, 0);

                            if ( lastSemi != -1)
                            {
                                String      codeToMatch = eachEntry.substring( 1, lastSemi);

                                for ( int count = 0; count <= g.fSpecialCharsList.length - 2; count += 2)
                                {
                                    if ( codeToMatch.equals(g.fSpecialCharsList[count]))
                                    {
                                        firstLetterChar     = g.fSpecialCharsList[count+1].charAt(0);
                                        firstLetterString   = kAmpersand +
                                                                g.fSpecialCharsList[count] +
                                                                kSemiColon;
                                        break;
                                    }
                                }
                            }
                        }

                        if ( firstLetterString.length() <= 0)
                        {
                            firstLetterString   = eachEntry.substring(0,1); // as string
                            firstLetterChar     = eachEntry.charAt(0);      // as char
                        }

                        if (( !firstLetterString.equals(lastFirstLetterString)) &&
                                Character.isUpperCase(firstLetterChar))
                        {
                            // Marker for each letter
                            AddLetterToFinalIndex( finalIndex, firstLetterString);
                            AddNewLetterToTable( letterStartsBuffer, firstLetterString);

                            lastFirstLetterString = firstLetterString;
                        }
                    }

                    StartWritingNewEntry(eachEntry);
                    WriteOutPageArray( array, i);
                    ++fNumEntriesWritten;

                    if ( i < GetTableSize() - 1)        WriteNewline();
                }
            }

            //////////

            if (fOptions.DoWeOutputHTML())      FinishWritingOutputFile(finalIndex);

            fWriter.close();
        }
        catch (java.io.IOException e)
        {
            return false;
        }
        catch (java.lang.ArrayIndexOutOfBoundsException e)
        {
            return false;
        }

        return true;
    }

    /*******************************************************************************
        CompleteIndexTable::CalculateDocumentWeights
    *******************************************************************************/
    void CalculateDocumentWeights()
    {
        fWeightForEachTerm_SJ       = new Vector();
        fWeightForEachTerm_Sa       = new Vector();
        fMinWeightForEachTerm_Sa    = new Vector();

        for ( int i = 0; i <= GetTableSize() - 1; ++i)
        {
            PageArray           arrayForThisTerm;

            if (( arrayForThisTerm = GetPageArrayOfIndexEntry(i)) != null)
            {
                Vector          weightForEachDoc_SJ = new Vector();
                Vector          weightForEachDoc_Sa = new Vector();
                String          lastLocation = null;
                int             freqInAllDocs = arrayForThisTerm.GetListLength();
                int             doc, j, numberOfMatchedDocuments = -1;

                fWeightForEachTerm_SJ.addElement(weightForEachDoc_SJ);
                fWeightForEachTerm_Sa.addElement(weightForEachDoc_Sa);

                int[]           matchesInEachDoc = new int [freqInAllDocs];

                for ( j = 0; j <= freqInAllDocs - 1; ++j)
                {
                    String      loc = arrayForThisTerm.GetReferenceAt(j).GetPageLocation();

                    if ( loc != lastLocation)
                    {
                        ++numberOfMatchedDocuments;
                        lastLocation = loc;
                    }
                    ++matchesInEachDoc[numberOfMatchedDocuments];
                }

                // Sparck Jones in Smith p20
                for ( doc = 0; doc <= numberOfMatchedDocuments; ++doc)
                {
                    weightForEachDoc_SJ.addElement( new WeightRec( doc,
                                                                (double) matchesInEachDoc[doc] /
                                                                (double) freqInAllDocs));
                }

                // Salton p280
                double      minSaltonWeight = 1E+30, saltonWeight;
                double      inverseDocFrequency = Math.log( (double) fNumberOfDocumentsInIndex /
                                                            (double)( numberOfMatchedDocuments + 1));

                for ( doc = 0; doc <= numberOfMatchedDocuments; ++doc)
                {
                    saltonWeight = (double) matchesInEachDoc[doc] * inverseDocFrequency;
                    if ( saltonWeight < minSaltonWeight)        minSaltonWeight = saltonWeight;

                    weightForEachDoc_Sa.addElement( new WeightRec( doc, saltonWeight));
                }

                fMinWeightForEachTerm_Sa.addElement( new WeightRec( i, minSaltonWeight));
            }
        }

        Sorter.QuicksortAscending( fMinWeightForEachTerm_Sa, null,
                                    0, GetTableSize() - 1, Sorter.SortWeights);
    }

    /*******************************************************************************
        CompleteIndexTable::DetermineWhichEntriesToOutput
    *******************************************************************************/
    void DetermineWhichEntriesToOutput()
    {
        WeightRec       wr;
        double          nextWeight;
        int             originalT = WorkOutHowManyToOutput(), endIndex, i, t;


        if ( originalT < GetTableSize())
        {
            t = Math.min( originalT - 1, GetTableSize() - 1);
            wr = GetMinWeightForThisTerm(t);
            nextWeight = GetMinWeightForThisTerm(t+1).GetWeight();

            if ( wr.GetWeight() == nextWeight)
            {
                while (( --t > 0) &&
                        (( wr = GetMinWeightForThisTerm(t)) != null) &&
                        ( wr.GetWeight() == nextWeight));

                if ( t < 0)
                {
                    t = Math.min( originalT - 1, GetTableSize() - 1);
                    while (( ++t > 0) &&
                            (( wr = GetMinWeightForThisTerm(t)) != null) &&
                            ( wr.GetWeight() == nextWeight));
                    endIndex = t - 1;
                }
                else    endIndex = t;
            }
            else        endIndex = t;
        }
        else            endIndex = Math.min( originalT - 1, GetTableSize() - 1);

        for ( i = 0; i <= endIndex; ++i)
        {
            wr = GetMinWeightForThisTerm(i);
            fThisEntryIsEligible[wr.GetIndex()] = true;
        }
    }

    /*******************************************************************************
        CompleteIndexTable::WorkOutHowManyToOutput
    *******************************************************************************/
    int WorkOutHowManyToOutput()
    {
        int     n;

        switch (fOptions.fOutputOptions)
        {
            case 1:     // top 50%
                n = (int) ( 0.5 * (double) GetTableSize());     break;
            case 2:     // top 25%
                n = (int) ( 0.25 * (double) GetTableSize());    break;
            case 3:     // top 10%
                n = (int) ( 0.1 * (double) GetTableSize());     break;
            case 4:     // top 1%
                n = (int) ( 0.01 * (double) GetTableSize());    break;
            case 5:             n = 100;                        break;      // top 100
            case 6:             n = 20;                         break;      // top 20
            default:            n = GetTableSize();             break;
        }

        if ( n < 1)             n = 1;      // let's have SOME results!

        return n;
    }

    /*******************************************************************************
        CompleteIndexTable::GetMinWeightForThisTerm
    *******************************************************************************/
    WeightRec GetMinWeightForThisTerm( int i)
    {
        return (WeightRec) fMinWeightForEachTerm_Sa.elementAt(i);
    }

    /*******************************************************************************
        CompleteIndexTable::StartWritingNewEntry
    *******************************************************************************/
    void StartWritingNewEntry( String entry) throws java.io.IOException
    {
        if (fOptions.DoWeOutputHTML())
        {
            WriteTab();
            WriteString(g.gTableRow.GetStartWithBrackets());
            WriteNewline();

            WriteTab();
            WriteTab();
            WriteString(fFirstColumnStartCodes);
        }

        WriteString(entry);

        if (fOptions.DoWeOutputHTML())
        {
            WriteString(kSpace);
            WriteString(g.gTableCol.GetEndWithBrackets());
            WriteNewline();

            WriteTab();
            WriteTab();
            WriteString(fSecondColumnStartCodes);
        }
        else        WriteTab();
    }

    /*******************************************************************************
        CompleteIndexTable::FinishWritingOutputFile
    *******************************************************************************/
    void FinishWritingOutputFile( StringBuffer finIndex) throws java.io.IOException
    {
        // Finish table
        WriteNewline();
        WriteString(g.gTable.GetEndWithBrackets());         WriteNewline();     // </TABLE>
        WriteNewline();                                     WriteNewline();

        // horizontal line
        WriteString(g.gBreak.GetStartWithBrackets());
        WriteString(g.gHorizontalRule.GetStartWithBrackets());
        WriteString(g.gBreak.GetStartWithBrackets());
        WriteNewline();                                     WriteNewline();

        // for anchors
        WriteStringBuffer(finIndex);                        WriteNewline();

        // horizontal line
        WriteString(g.gBreak.GetStartWithBrackets());
        WriteString(g.gHorizontalRule.GetStartWithBrackets());
        WriteNewline();

        // Comments...
        WriteNewline();
        WriteString(g.gCenter.GetStartWithBrackets());      WriteNewline();
        WriteString(g.gBold.GetStartWithBrackets());        WriteNewline();
        WriteString(kMyComments);                           WriteNewline();
        WriteString(g.gBold.GetEndWithBrackets());          WriteNewline();
        WriteString(g.gCenter.GetEndWithBrackets());        WriteNewline();
        WriteNewline();

        // </BODY>
        WriteString(g.gBody.GetEndWithBrackets());          WriteNewline();

        // </HTML>
        WriteString(g.gHTML.GetEndWithBrackets());
    }

    /*******************************************************************************
        CompleteIndexTable::AddLetterToFinalIndex
    *******************************************************************************/
    void AddLetterToFinalIndex( StringBuffer b, String ourCharString)
    {
        b.append( kUniversalLinkCode + kDoubleQuotes + kHashSign + ourCharString);
        b.append( kDoubleQuotes + kRightBracket + kSpace + ourCharString + kSpace);
        b.append( kLinkEnd + kSpace);
        // Seperate entries with a space. Would rather use a return but had
        // problems with line endings. 18/8/98.
    }

    /*******************************************************************************
        CompleteIndexTable::WriteString
    *******************************************************************************/
    void WriteString( String s) throws java.io.IOException
    {
        fWriter.write( s, 0, s.length());
    }

    /*******************************************************************************
        CompleteIndexTable::WriteStringBuffer
    *******************************************************************************/
    void WriteStringBuffer( StringBuffer sb) throws java.io.IOException
    {
        fWriter.write( sb.toString(), 0, sb.length());
    }

    /*******************************************************************************
        CompleteIndexTable::WriteTab
    *******************************************************************************/
    void WriteTab() throws java.io.IOException
    {
        char    arr[] = {'\t'};
        fWriter.write(arr);
    }

    /*******************************************************************************
        CompleteIndexTable::WriteOutHeader
    *******************************************************************************/
    void WriteOutHeader() throws java.io.IOException
    {
        // <HTML> code now
        WriteString(g.gHTML.GetStartWithBrackets());        WriteNewline();     // <HTML>
        WriteNewline();

        // Start header
        WriteString(g.gHead.GetStartWithBrackets());        WriteNewline();     // <HEAD> ...

        // Title now
        WriteString( g.gTitle.GetStartWithBrackets() + kSpace);                 // <TITLE> ...
        WriteString( kDocumentTitle + kDoubleQuotes + fApp.GetInitialFilename() + kDoubleQuotes);
        WriteString( kSpace + g.gTitle.GetEndWithBrackets());                   // ... </TITLE>
        WriteNewline();

        // End header
        WriteString(g.gHead.GetEndWithBrackets());          WriteNewline();     // ... </HEAD>
        WriteNewline();

        // Start body, then table
        WriteString(g.gBody.GetStartWithBrackets());        WriteNewline();     // <BODY> ...
        WriteString(g.gTable.GetStartWithBrackets());                           //   <TABLE...
    }

    /*******************************************************************************
        CompleteIndexTable::GetNumEntriesWritten
    *******************************************************************************/
    int GetNumEntriesWritten()
    {
        return fNumEntriesWritten;
    }

    /*******************************************************************************
        CompleteIndexTable::WriteOutPageArray
    *******************************************************************************/
    void WriteOutPageArray( PageArray array, int termIndex) throws java.io.IOException
    {
        String      lastLocation = null;
        int         termFrequency = array.GetListLength();
        int         doc, j, numberOfMatchedDocuments = -1;


        for ( j = 0; j <= termFrequency - 1; ++j)
        {
            PageReference   pageRef = array.GetReferenceAt(j);
            StringBuffer    buffer = new StringBuffer();


            if ( pageRef.GetPageLocation() != lastLocation)
            {
                ++numberOfMatchedDocuments;             ////////////////////// 10/7/98
                lastLocation = pageRef.GetPageLocation();

                if ( fOptions.DoWeOutputHTML() && ( j > 0))
                            buffer.append(g.gParagraph.GetStartWithBrackets());     // "<P>"

                buffer.append("(");
                AddLocationToBuffer( pageRef.GetPageLocation(), buffer);
                AddWeightsToBuffer( numberOfMatchedDocuments, termIndex, buffer);
                buffer.append(") ");
            }


            int             priority = pageRef.GetImportanceOfReference();
            HTMLcode        priorityCode = fPriorityCodeList[priority];


            if (fOptions.DoWeOutputHTML())
            {
                buffer.append(priorityCode.GetStartWithBrackets());

                if ( priority >= kNoteIconThreshold)
                            buffer.append(fNoteIconString);
            }

            if (pageRef.IsPartOfTitle())
                            buffer.append(kAppearsInTitleString);       // "Title" number
            else            buffer.append( pageRef.GetPageNumber());    // Page number

            if (fOptions.fMoreDetailedResults)
            {
                // Level, then importance
                buffer.append( " ( Level-" + pageRef.GetLevelOfReference() +
                                " Importance-" + priority + ") ");
            }

            if ( j < termFrequency - 1)         buffer.append(kComma);
            else                                buffer.append(".");

            if (fOptions.DoWeOutputHTML())
            {
                buffer.append( priorityCode.GetEndWithBrackets() + kSpace);

                if ( j >= termFrequency - 1)
                {
                    buffer.append(g.gTableCol.GetEndWithBrackets());
                    buffer.append(g.gTableRow.GetEndWithBrackets());
                }
            }
            else
            {
                if ( j < termFrequency - 1)     buffer.append(kSpace);
            }

            WriteStringBuffer(buffer);
        }
    }

    /*******************************************************************************
        CompleteIndexTable::AddLocationToBuffer
    *******************************************************************************/
    void AddLocationToBuffer( String loc, StringBuffer b)
    {
        if (fOptions.DoWeOutputHTML())      b.append( kUniversalLinkCode + loc + kRightBracket);

        b.append(loc);

        if (fOptions.DoWeOutputHTML())      b.append(kLinkEnd);
    }

    /*******************************************************************************
        CompleteIndexTable::AddWeightsToBuffer
    *******************************************************************************/
    void AddWeightsToBuffer( int numDocs, int termIndex, StringBuffer b)
    {
        ////////////////////// 10/7/98
        if ( fNumberOfDocumentsInIndex > 1)
        {
            WeightRec   wrec;
            Vector      v;

            v = (Vector) fWeightForEachTerm_SJ.elementAt(termIndex);
            wrec = (WeightRec) v.elementAt(numDocs);
            b.append( " Weight = " + Util.DoubleToString( wrec.GetWeight(), 2) + kSlash);

            v = (Vector) fWeightForEachTerm_Sa.elementAt(termIndex);
            wrec = (WeightRec) v.elementAt(numDocs);
            b.append( Util.DoubleToString( wrec.GetWeight(), 2));
        }
        //////////////////////
    }

    /*******************************************************************************
        CompleteIndexTable::WriteNewline
    *******************************************************************************/
    void WriteNewline() throws java.io.IOException
    {
        fWriter.newLine();
    }

    /*******************************************************************************
        CompleteIndexTable::AddNewLetterToTable
    *******************************************************************************/
    void AddNewLetterToTable( StringBuffer prefix, String ourCharString) throws java.io.IOException
    {
        WriteNewline();                 // start the first line
        WriteTab();                     // inset it
        WriteString(g.gTableRow.GetStartWithBrackets());    // <TR> on its own

        // -----------------------------------

        WriteNewline();                 // start the second line
        WriteTab();                     // align this line with
        WriteTab();                     // ... the one above
        WriteStringBuffer(prefix);      // finish off the line that starts: <TD BGCOLOR= ....

        // -----------------------------------

        WriteNewline();                 // start the line that goes:  <A NAME="?" ....
        WriteTab();                     // align this line with
        WriteTab();                     // ... the one above
        WriteString(kLeftBracket);      // link starts
        WriteString( kAnchorLinkSmall + kEquals + kDoubleQuotes + ourCharString + kDoubleQuotes);
        WriteString( kRightBracket + kLinkEnd + kSpace);    // link ends
        WriteString(ourCharString);     // character to display

        // -----------------------------------

        WriteNewline();                 // fourth and final line starts with </FONT> etc
        WriteTab();
        WriteTab();

        WriteString(g.gFont.GetEndWithBrackets());
        WriteString(g.gBold.GetEndWithBrackets());
        WriteString(g.gHeaderOne.GetEndWithBrackets());

        WriteString(kSpace);
        WriteString(g.gTableCol.GetEndWithBrackets());
        WriteString(g.gTableRow.GetEndWithBrackets());
        WriteNewline();
    }

    /*******************************************************************************
        CompleteIndexTable::SortFromAToZ
    *******************************************************************************/
    boolean SortFromAToZ()
    {
        boolean         result = true;

        if (fOptions.fSortEntries)
        {
            result = Sorter.Quicksort( fWordTable, fDataTable, 0, GetTableSize() - 1,
                                        Sorter.SortStrings, fOptions.fSortDirection);

            // Now sort the list of page numbers
            for ( int i = 0; i <= GetTableSize() - 1; ++i)
            {
                try
                {
                    GetPageArrayOfIndexEntry(i).SortArray();
                }
                catch ( java.lang.NullPointerException e)
                {
                    // It's an entry from the Word List and it's unused (null page array)
                }
            }
        }

        return result;
    }

    /*******************************************************************************
        CompleteIndexTable::HandlePostProcessing        6/8/98
    *******************************************************************************/
    void HandlePostProcessing()
    {
        String      entry;
        int         i = 0;

        while ( i < GetTableSize())
        {
            entry = GetTheWordOfIndexEntry(i);

            // 6/8/98: With this rule, "AmericAN" and "AmericANS" are combined if found
            // together, but not if only one of the words is found. This is a very useful
            // rule for countries and suchlike.
            // 9/8/98: Added "ist" suffix, so "ElitIST" and "ElitISTS" go together.

            if ( entry.endsWith("an") || entry.endsWith("ist"))
            {
                int     pos = fWordTable.indexOf( entry + "s");

                if ( pos != -1)     // good, we've found the word but with an "S" suffix
                {
                    PageArray   ianArray = GetPageArrayOfIndexEntry(i);
                    PageArray   iansArray = GetPageArrayOfIndexEntry(pos);

                    // Copy the references from "ElitIST" into the "ElitISTS" list
                    // then delete the "ElitIST" entry.

                    for ( int j = 0; j <= ianArray.GetListLength() - 1; ++j)
                                iansArray.AddPageReference(ianArray.GetReferenceAt(j));
                    RemoveIndexEntry(i);
                }
                else
                {
                    if (fOptions.fSmartNamePrefixes)    HandleSmartNameAdjusting(i);
                    ++i;
                }
            }
            else
            {
                if (fOptions.fSmartNamePrefixes)        HandleSmartNameAdjusting(i);
                ++i;
            }
        }
    }

    /*******************************************************************************
        CompleteIndexTable::HandleSmartNameAdjusting        24/8/98
    *******************************************************************************/
    void HandleSmartNameAdjusting( int index)
    {
        String      entry;

        for ( int i = 0; i <= Prefixes.fSupportedNamePrefixes.length - 1; ++i)
        {
            entry = GetTheWordOfIndexEntry(index);
            if ( entry.startsWith(Prefixes.fSupportedNamePrefixes[i]))
            {
                // prefix firstname surname  -->  surname, prefix firstname
                // category name  -->  name, category

                int         firstNameEnds = entry.lastIndexOf(kSpace);
                int         lastPrefixChar = Prefixes.fSupportedNamePrefixes[i].length();

                if ( Character.isUpperCase( entry.charAt( entry.length() - 1)) &&
                    ( firstNameEnds > 1))
                {
                    // If we have "King Edward IV", the "IV" shouldn't be regarded as the
                    // last name, so we need to break at the space BEFORE the last one.
                    firstNameEnds = entry.lastIndexOf( kSpace, firstNameEnds - 1);
                }

                String      ss = entry.substring( firstNameEnds + 1);

                if ( firstNameEnds >= 0)
                            ss += kComma + kSpace + entry.substring( 0, firstNameEnds);

                SetTheWordOfIndexEntry( ss, index);
                break;
            }
        }
    }
}