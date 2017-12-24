import java.util.ListResourceBundle;

public class LabelsBundle extends ListResourceBundle    // 3/8/98
{
    public Object[][] getContents()     { return fContents; }

    static final Object[][] fContents =
    {
        { "arf", "Allow remote files?"},
        { "swfh", "Stay with first host?"},
        { "uswf", "Use Stop Word file:"},
        { "smc", "Use smart name prefix handling?"},
        { "rww", "Remove words with 'ing'/'ally'?"},
        { "ing_suffix", "ing"},
        { "ally_suffix", "ally"},
        { "ffwq", "Find first word in quotes?"},
        { "so", "Sort Output ?"},
        { "sasc", "Ascending A-Z"},
        { "sdsc", "Descending Z-A"},
        { "shtml", "Save as HTML?"},
        { "indiv", "Only index individual words"},
        { "defm", "Default indexing method"},
        { "ufm", "Frequency method"},

        { "mdr", "More detailed results?"},
        { "all", "All entries"},
        { "b50%", "Best 50%"},
        { "b25%", "Best 25%"},
        { "b10%", "Best 10%"},
        { "b1%", "Best 1%"},
        { "b100", "Best 100"},
        { "b20", "Best 20"},

        { "start", "Start indexing"},

        { "stf", "Start document or URL:"},
        { "outf", "Output filename:"},
        { "if", "Icon filename:"},
        { "kwf", "Key Word filename:"},
        { "mrd", "Max remote depth:"},
        { "oe", "Output entries:"},

        { "outside_a", "- Warning: URL <"},
        { "outside_b", "> will not be accessed as it is not on host <"},
        { "connecting", "**** Connecting to: <"},

        { "app_started", "Application started on "},
        { "no_keyword_list", "- Warning: Could not find a Key Word list. You must rely on my rules."},
        { "no_input", "- Error: Could not open initial HTML document; no output will be made."},
        { "finished", "Analysis finished in "},
        { "sorted", "Sorting done in "},
        { "written", "Output written in "},
        { "out_of", " written (out of "},
        { "found", " found"},
        { "docs", " documents."},
        { "bytes_read", " bytes read"},
        { "app_done", "Application finished on "}
    };
}