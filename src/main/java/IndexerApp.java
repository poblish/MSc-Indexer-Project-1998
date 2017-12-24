import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class IndexerApp implements Constants, ActionListener
{
    private StopWordList            mStopWordList;
    private Globals                 mGlobals;
    private Options                 mOpts;
    private CompleteIndexTable      mTheCompleteIndex;
    private SubIndexTable           mKeyWordsIndex;
    private HTMLFile                mCurrentDocument, mSavedDocument, mParent;
    private ResourceBundle          fOurBundle;
    private GridBagConstraints      mOurGridBagConstraints;
    private Frame                   fWindow;
    private Vector                  mListOfDocumentsInProgress, mListOfDocumentsUsed;
    private String                  mFileName, mStartFileName, mIconFileName;
    private String                  mWordListFileName, fOutputFileName, fStopListFileName;
    private boolean                 fAreWeAddingNewDocument, fWeHaveADocumentToAnalyse;
    private boolean                 fAnalysisComplete;
    private boolean                 fSuccessfullyGotKeyWordList, fCurrentlyBuildingWordList;

    private TextField               mInputFileField, mOutputFileField, fIconFileField;
    private TextField               fWordListField, fMaxDepthField, fStoplistFileField;
    private Checkbox                fIndividualCheck, fUseFrequencyCheck, fSmartNameCheck;
    private Checkbox                fAllowRemoteCheck, fUseStoplistCheck, fRemoveIngsCheck;
    private Checkbox                fFindInQuotesCheck, fSortOutputCheck, fStayWithHostCheck;
    private Checkbox                mSaveHTMLCheck, fMoreDetailCheck, fAscendingRadio;
    private Choice                  fOutputOptionsChoice;
    private Button                  fStartButton;


    Globals     GetApplicationGlobals()             { return mGlobals; }
    Options     GetApplicationOptions()             { return mOpts; }
    String      GetCurrentFilename()                { return mFileName; }
    int         GetDocumentsLevelInHierarchy()      { return mListOfDocumentsInProgress.size(); }
    int         GetNumberOfDocumentsUsed()          { return mListOfDocumentsUsed.size(); }
    HTMLFile    GetRememberedDocument()             { return mSavedDocument; }
    String      GetInitialFilename()                { return mStartFileName; }
    boolean     IsAlreadyUsingWordList()            { return fSuccessfullyGotKeyWordList; }
    boolean     IsBuildingWordList()                { return fCurrentlyBuildingWordList; }
    void        RememberThisDocument( HTMLFile doc) { mSavedDocument = doc; }
    void        ChangeCurrentFilenameTo( String s)  { mFileName = s; }


    /*******************************************************************************
        main
    *******************************************************************************/
    public static void main( String args[])
    {
        IndexerApp      app = new IndexerApp();

        app.init();     // initialize the applet
    }


    /*******************************************************************************
        IndexerApp::init
    *******************************************************************************/
    public void init()
    {
        SetupWindow();

        // We'll use this constraint object as a template
        mOurGridBagConstraints          = new GridBagConstraints();
        mOurGridBagConstraints.fill     = GridBagConstraints.HORIZONTAL;
        mOurGridBagConstraints.insets   = new Insets(2,2,2,2);
        mOurGridBagConstraints.anchor   = GridBagConstraints.NORTHWEST;

        // We open the bundle to store language-specific strings
        fOurBundle  = ResourceBundle.getBundle("LabelsBundle");
        mOpts       = new Options();            // filled in with default settings
        mGlobals    = new Globals();

        /////////////////////////

        SetupComponents();
        fWindow.show();     /////
    }

    /*******************************************************************************
        IndexerApp::actionPerformed
    *******************************************************************************/
    public void actionPerformed( ActionEvent e)
    {
        if ( e.getSource() == fStartButton)     StartAnalysis();    // Start button pressed
    }

    /*******************************************************************************
        IndexerApp::GetString
    *******************************************************************************/
    String GetString( String key)
    {
        try
        {
            return fOurBundle.getString(key);
        }
        catch (MissingResourceException e)
        {
            return "?";
        }
    }

    /*******************************************************************************
        IndexerApp::SetupWindow
    *******************************************************************************/
    void SetupWindow()
    {
        fWindow = new Frame("Indexer");

        fWindow.setLayout( new GridBagLayout());
        fWindow.setResizable(true);
        fWindow.setSize(520,450);
        fWindow.setLocation(40,40);     // ie. don't stick our title bar under the menu bar
        fWindow.addWindowListener
        (
            new WindowAdapter()         // inner class, so clicking the close box works
            {
                public void windowClosing( WindowEvent e)   { fWindow.dispose(); }
            }
        );
    }

    /*******************************************************************************
        IndexerApp::SetupComponents
    *******************************************************************************/
    void SetupComponents()
    {
        mInputFileField     = new TextField( kDefaultInputFile, 20);
        mOutputFileField    = new TextField( kDefaultOutputFile, 20);
        fIconFileField      = new TextField( "triangle.gif", 20);
        fWordListField      = new TextField( "null", 20);
        fStoplistFileField  = new TextField( "stoplist.txt", 20);
        fMaxDepthField      = new TextField( new Integer(mOpts.fDeepestWebFileAllowed).toString(), 4);
        fIndividualCheck    = new Checkbox( GetString("indiv"), mOpts.fIndividualWordsOnly);

        CheckboxGroup       freqGroup = new CheckboxGroup();
        fUseFrequencyCheck  = new Checkbox( GetString("ufm"), mOpts.fUseFrequencyMethod, freqGroup);
        fAllowRemoteCheck   = new Checkbox( GetString("arf"), mOpts.fShouldAccessNet);
        fStayWithHostCheck  = new Checkbox( GetString("swfh"), mOpts.fKeepWithOriginalHost);
        fUseStoplistCheck   = new Checkbox( GetString("uswf"), mOpts.UsingStopWordList());
        fSmartNameCheck     = new Checkbox( GetString("smc"), mOpts.fSmartNamePrefixes);
        fRemoveIngsCheck    = new Checkbox( GetString("rww"), mOpts.fTryToRemoveIngs);
        fFindInQuotesCheck  = new Checkbox( GetString("ffwq"), mOpts.fFindFirstWordInQuotes);
        fSortOutputCheck    = new Checkbox( GetString("so"), mOpts.fSortEntries);

        CheckboxGroup       group = new CheckboxGroup();
        fAscendingRadio     = new Checkbox( GetString("sasc"), ( mOpts.fSortDirection == 1), group);
        mSaveHTMLCheck      = new Checkbox( GetString("shtml"), mOpts.DoWeOutputHTML());
        fMoreDetailCheck    = new Checkbox( GetString("mdr"), mOpts.fMoreDetailedResults);

        fOutputOptionsChoice = new Choice();
        fOutputOptionsChoice.add(GetString("all"));
        fOutputOptionsChoice.add(GetString("b50%"));
        fOutputOptionsChoice.add(GetString("b25%"));
        fOutputOptionsChoice.add(GetString("b10%"));
        fOutputOptionsChoice.add(GetString("b1%"));
        fOutputOptionsChoice.add(GetString("b100"));
        fOutputOptionsChoice.add(GetString("b20"));
        fOutputOptionsChoice.select(mOpts.fOutputOptions);

        fStartButton = new Button(GetString("start"));
        fStartButton.addActionListener(this);


        int     row = 0, col = 0;

        AddComponent( new Label(GetString("stf")), row, col);
        AddComponent( mInputFileField, row, col + 1, 2);
        AddComponent( new Label(GetString("outf")), ++row, col);
        AddComponent( mOutputFileField, row, col + 1, 2);
        AddComponent( new Label(GetString("if")), ++row, col);
        AddComponent( fIconFileField, row, col + 1, 2);
        AddComponent( new Label(GetString("kwf")), ++row, col);
        AddComponent( fWordListField, row, col + 1, 2);

        AddComponent( fIndividualCheck, ++row, col, 3);
        AddComponent( new Checkbox( GetString("defm"), !mOpts.fUseFrequencyMethod, freqGroup), ++row, col);
        AddComponent( fUseFrequencyCheck, row, col + 1, 2);

        AddComponent( fUseStoplistCheck, ++row, col);
        AddComponent( fStoplistFileField, row, col + 1, 2);

        // Remote files options
        AddComponent( fAllowRemoteCheck, ++row, col);
        AddComponent( fStayWithHostCheck, row, col + 1);

        AddComponent( new Label(GetString("mrd")), ++row, col);
        AddComponent( fMaxDepthField, row, col + 1);

        // Parse options
        AddComponent( fFindInQuotesCheck, ++row, col);
        AddComponent( fRemoveIngsCheck, row, col + 1, 2);
        AddComponent( fSmartNameCheck, ++row, col, 2);

        // Output options
        AddComponent( fSortOutputCheck, ++row, col);
        AddComponent( fAscendingRadio, row, col + 1);
        AddComponent( new Checkbox( GetString("sdsc"), ( mOpts.fSortDirection != 1), group), row, col + 2);

        AddComponent( mSaveHTMLCheck, ++row, col);
        AddComponent( fMoreDetailCheck, row, col + 1);

        AddComponent( new Label(GetString("oe")), ++row, col);
        AddComponent( fOutputOptionsChoice, row, col + 1);

        // Start button
        AddComponent( fStartButton, ++row, col + 1);
    }

    /*******************************************************************************
        IndexerApp::AddComponent
    *******************************************************************************/
    void AddComponent( Component c, int row, int col)
    {
        AddComponent( c, row, col, 1);
    }

    /*******************************************************************************
        IndexerApp::AddComponent
    *******************************************************************************/
    void AddComponent( Component c, int row, int col, int width)
    {
        LayoutManager   layout = fWindow.getLayout();

        if (( layout != null) && ( layout instanceof GridBagLayout))
        {
            mOurGridBagConstraints.gridx = col;
            mOurGridBagConstraints.gridy = row;
            mOurGridBagConstraints.gridwidth = width;
            ((GridBagLayout)layout).setConstraints( c, mOurGridBagConstraints);
            // position the component
        }

        fWindow.add(c);     // add the component in position
    }

    /*******************************************************************************
        IndexerApp::InitialiseAnalysis
    *******************************************************************************/
    void InitialiseAnalysis()
    {
        System.out.println( GetString("app_started") + Util.MyMakeDateString(new Date()));
        System.out.println();

        mParent = null;
        RememberThisDocument(null);
        mKeyWordsIndex = null;

        mListOfDocumentsUsed       = new Vector();
        mListOfDocumentsInProgress = new Vector();

        /////////////////////

        mStartFileName     = mInputFileField.getText();
        mIconFileName      = fIconFileField.getText();
        mWordListFileName  = fWordListField.getText();
        fOutputFileName    = mOutputFileField.getText();
        fStopListFileName  = fStoplistFileField.getText();

        // if the input file isn't right (maybe blank), try the default file name

        if ( mStartFileName.length() < "?.htm".length())    mStartFileName = kDefaultInputFile;

        ChangeCurrentFilenameTo(GetInitialFilename());

        /////////////////////

        mOpts.RestoreDefaults();

        try
        {
            mOpts.fSmartNamePrefixes     = fSmartNameCheck.getState();
            mOpts.fTryToRemoveIngs       = fRemoveIngsCheck.getState();
            mOpts.fOutputHTMLFormat      = mSaveHTMLCheck.getState();
            mOpts.fFindFirstWordInQuotes = fFindInQuotesCheck.getState();
            mOpts.fUseAStopWordList      = fUseStoplistCheck.getState();
            mOpts.fSortEntries           = fSortOutputCheck.getState();
            mOpts.fMoreDetailedResults   = fMoreDetailCheck.getState();
            mOpts.fShouldAccessNet       = fAllowRemoteCheck.getState();
            mOpts.fIndividualWordsOnly   = fIndividualCheck.getState();
            mOpts.fUseFrequencyMethod    = fUseFrequencyCheck.getState();
            mOpts.fKeepWithOriginalHost  = fStayWithHostCheck.getState();
            mOpts.fSortDirection         = fAscendingRadio.getState() ? kSortAscending : kSortDescending;
            mOpts.fOutputOptions         = fOutputOptionsChoice.getSelectedIndex();
            mOpts.fDeepestWebFileAllowed = Util.StringToInteger(fMaxDepthField.getText());
        }
        catch ( java.lang.Exception e)
        {
            // We've been given a bad integer, probably a floating point.
            mOpts.fDeepestWebFileAllowed = 3;
        }

        // Reset globals, such as bytes read, and original url, for this program run
        mGlobals.Reset();

        // Initialise the 'global' or complete, index
        mTheCompleteIndex = new CompleteIndexTable(this);

        if (mOpts.UsingStopWordList())  mStopWordList = new StopWordList(fStopListFileName);
        else                            mStopWordList = null;

        fWeHaveADocumentToAnalyse = false;
        fCurrentlyBuildingWordList = fAreWeAddingNewDocument = true;
    }

    /*******************************************************************************
    *******************************************************************************/
    void StartAnalysis()
    {
        // Spin the cursor to show we're working
        fWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        InitialiseAnalysis();   // initialise for this run

        long        ms = System.currentTimeMillis();
        boolean     canWriteOutput = true;

        try
        {
            //////// 20/4/98 ///////////

            try
            {
                if ( mWordListFileName == null)     // no word list specified, so skip next bit
                        throw new BadHTMLFileException();
                else
                {
                    HTMLFile    keyWordsDocument;

                    // Try to open the Key Word file and index its contents
                    keyWordsDocument = new HTMLFile( this, mWordListFileName, mStopWordList);
                    keyWordsDocument.SetupDocument();           // we'll catch any errors.
                    keyWordsDocument.AnalyseDocument();

                    // get the index, we'll need it when indexing the proper documents
                    mKeyWordsIndex = keyWordsDocument.GetDocumentsIndex();

                    fSuccessfullyGotKeyWordList = true;
                }
            }
            catch ( BadHTMLFileException e)
            {
                fSuccessfullyGotKeyWordList = false;
                System.out.println(GetString("no_keyword_list"));
            }

            fCurrentlyBuildingWordList = false;         // start indexing properly now
            fAnalysisComplete = false;

            ////////////////////////////

            while (!fAnalysisComplete)
            {
                try
                {
                    // We've found a new document to index, so try to read it in
                    if (fAreWeAddingNewDocument)        TryToReadDocument();
                }
                catch ( BadHTMLFileException e)
                {
                    if ( GetCurrentFilename() == GetInitialFilename())
                    {
                        // Can't open our start file. Can't do anything. Let's leave.
                        throw new CouldNotStartException();
                    }
                    else
                    {
                        // Invalid reference found: continue with the document that called it
                        mCurrentDocument = GetRememberedDocument();
                    }
                }

                // Start indexing the document
                TryToAnalyseDocument();
            }
        }
        catch (java.util.NoSuchElementException e)
        {
            // We will usually break out of the loop this way. Hopefully!!
        }
        catch ( CouldNotStartException e)
        {
            canWriteOutput = false;
            System.out.println(GetString("no_input"));
        }

        if (canWriteOutput)
        {
            System.out.println( GetString("finished") + ( System.currentTimeMillis() - ms) + " msecs");
            ms = System.currentTimeMillis();

            mTheCompleteIndex.HandlePostProcessing();   // here, we format names and other stuff
            mTheCompleteIndex.SortFromAToZ();

            System.out.println( GetString("sorted") + ( System.currentTimeMillis() - ms) + " msecs");
            ms = System.currentTimeMillis();

            // Write out the complete index.
            mTheCompleteIndex.WriteOut( mIconFileName, fOutputFileName);

            ///////////////////////////////
            String      sss = GetString("written") + ( System.currentTimeMillis() - ms) + " msecs, " +
                                mTheCompleteIndex.GetNumEntriesWritten() + " entries";

            if ( mTheCompleteIndex.GetNumEntriesWritten() != mTheCompleteIndex.GetTableSize())
            {
                sss += GetString("out_of") + mTheCompleteIndex.GetTableSize() + ")";
            }
            else    sss += GetString("found");

            System.out.println( sss + " from " + GetNumberOfDocumentsUsed() + GetString("docs"));
            ///////////////////////////////

            System.out.println( mGlobals.mDataBytesRead + GetString("bytes_read"));

        }
        System.out.println( GetString("app_done") + Util.MyMakeDateString(new Date()));
        System.out.println("---------------------------------------------------");
        System.out.println();

        Toolkit.getDefaultToolkit().beep();
        fWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /*******************************************************************************
    *******************************************************************************/
    void TryToReadDocument() throws BadHTMLFileException
    {
        RememberThisDocument(mCurrentDocument);

        if (( GetNumberOfDocumentsUsed() > 0) && mListOfDocumentsUsed.contains(GetCurrentFilename()))
        {
            // We've already opened that file. Don't get circular!
            throw new BadHTMLFileException();
        }

        mCurrentDocument = new HTMLFile( this, GetCurrentFilename(), mParent, mKeyWordsIndex,
                                            GetDocumentsLevelInHierarchy(), mStopWordList);

        // Read in this new document, we'll catch any errors.
        mCurrentDocument.SetupDocument();

        fWeHaveADocumentToAnalyse = true;
        mListOfDocumentsInProgress.addElement(mCurrentDocument);
        mListOfDocumentsUsed.addElement(GetCurrentFilename());
    }

    /*******************************************************************************
    *******************************************************************************/
    void TryToAnalyseDocument()
    {
        if (fWeHaveADocumentToAnalyse)
        {
            // Index it, and if we find a link, stop, and return true.
            // We'll come back to it later.
            fAreWeAddingNewDocument = mCurrentDocument.AnalyseDocument();

            if (fAreWeAddingNewDocument)
            {
                // We've found a new file to analyse, so let's point to that one, and make
                // sure we know that it's a 'child' of the file we were working on.
                ChangeCurrentFilenameTo(mCurrentDocument.GetNextLink());
                mParent = mCurrentDocument;
            }
            else
            {
                // We haven't found a new file to go to, so we've finished with 'mCurrentDocument'
                // Remove it from the list, and find the one at the 'top of the stack' (ie. the
                // last element in the list). If it's empty, we've finished all the files.

                // 10/7/98. Firstly, we take the index accumulated for the file and add it to
                // the main index before forgetting about the file.

                SubIndexTable   thisIndex = mCurrentDocument.GetDocumentsIndex();

                if (mOpts.fUseFrequencyMethod)      thisIndex.AdjustForFrequencies();
                mTheCompleteIndex.MergeWith(thisIndex);

                mListOfDocumentsInProgress.removeElement(mCurrentDocument);
                mCurrentDocument = (HTMLFile) mListOfDocumentsInProgress.lastElement();

                if ( mCurrentDocument == null)      fAnalysisComplete = true;   // list empty!
            }
        }
        else    fAnalysisComplete = true;
    }
}