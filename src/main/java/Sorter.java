import java.util.*;

public class Sorter implements Constants        // 4/8/98
{
    public static final int     SortStrings = 404, SortWeights = 202;
    public static final int     SortIntegers = 707;

    /*******************************************************************************
        Sorter::GetSortingPivot
    *******************************************************************************/
    static Object GetSortingPivot( Vector v, int lo, int hi)
    {
        int     halfway = ( lo + hi) / 2;

        return v.elementAt(halfway);
    }

    /*******************************************************************************
        Sorter::SwapVectorElements
    *******************************************************************************/
    static void SwapVectorElements( Vector v, int i, int j)
    {
        Object      tempObj = v.elementAt(i);

        v.setElementAt( v.elementAt(j), i);
        v.setElementAt( tempObj, j);
    }

    /*******************************************************************************
        Sorter::QuicksortAscending
    *******************************************************************************/
    static boolean QuicksortAscending( Vector v, Vector v2, int lo, int hi, int type)
    {
        return Quicksort( v, v2, lo, hi, type, 1);
    }

    /*******************************************************************************
        Sorter::Quicksort
    *******************************************************************************/
    static boolean Quicksort( Vector v, Vector v2, int lo, int hi, int type, int dir)
    {
        WeightRec           pivotWeight = null;
        PageReference       pivotRef = null;
        Integer             pivotInteger = null;
        String              pivotString = null;
        int                 i, j, result;
        boolean             done;


        try
        {
            i = lo;
            j = hi;

            switch (type)
            {
                case SortStrings:
                    pivotString = (String) GetSortingPivot( v, i, j);
                    break;
                case SortWeights:
                    pivotWeight = (WeightRec) GetSortingPivot( v, i, j);
                    break;
                case SortIntegers:
                    pivotInteger = (Integer) GetSortingPivot( v, i, j);
                    break;
                default:
                    pivotRef = (PageReference) GetSortingPivot( v, i, j);
                    break;
            }

            while ( i <= j)
            {
                done = false;
                while (!done)
                {
                    result = CompareElements( v, i, type, dir, pivotString, pivotWeight,
                                                                pivotInteger, pivotRef);

                    if (( result < kSameAs) && ( i < hi))       ++i;
                    else                                        done = true;
                }

                done = false;
                while (!done)
                {
                    result = CompareElements( v, j, type, dir, pivotString, pivotWeight,
                                                                pivotInteger, pivotRef);

                    if (( result > kSameAs) && ( j > lo))       --j;
                    else                                        done = true;
                }

                if ( i < j)                 // Swap pointers from i and j
                {
                    SwapVectorElements( v, i, j);
                    if ( v2 != null)        SwapVectorElements( v2, i, j);
                }

                if ( i <= j)
                {
                    ++i;
                    --j;
                }
            };

            if ( lo < j)        Quicksort( v, v2, lo, j, type, dir);
            if ( i < hi)        Quicksort( v, v2, i, hi, type, dir);
        }
        catch (java.lang.ArrayIndexOutOfBoundsException e)
        {
            return false;
        }

        return true;
    }

    /*******************************************************************************
        Sorter::ComparePageReferences
    *******************************************************************************/
    static int ComparePageReferences( Vector v, int a, PageReference bRef, int whichWay)
    {
        PageReference       aRef = (PageReference) v.elementAt(a);
        int                 ordering = kSameAs;

        switch (whichWay)
        {
            case kSortLevels:
                if ( aRef.GetLevelOfReference() > bRef.GetLevelOfReference())
                            ordering = kLaterThan;
                else
                {
                    if ( aRef.GetLevelOfReference() < bRef.GetLevelOfReference())
                            ordering = kEarlierThan;
                }
                break;

            case kSortFileNames:
                ordering = aRef.GetAdjLocation().compareTo(bRef.GetAdjLocation());
                break;

            case kSortPageNums:
                if ( aRef.GetPageNumber() > bRef.GetPageNumber())
                            ordering = kLaterThan;
                else
                {
                    if ( aRef.GetPageNumber() < bRef.GetPageNumber())
                            ordering = kEarlierThan;
                }
                break;
        }

        return ordering;
    }

    /*******************************************************************************
        Sorter::CompareElements
    *******************************************************************************/
    static int CompareElements( Vector v, int index, int type, int dir, String pivotString,
                                WeightRec pivotWeight, Integer pivotInteger,
                                PageReference pivotRef)
    {
        int     result;

        switch (type)
        {
            case SortStrings:
                result = dir * ((String) v.elementAt(index)).compareTo(pivotString);
                break;

            case SortWeights:
                WeightRec   curWeight = (WeightRec) v.elementAt(index);

                if ( curWeight.GetWeight() < pivotWeight.GetWeight())
                    result = dir;
                else
                {
                    result = ( curWeight.GetWeight() > pivotWeight.GetWeight()) ? -dir : kSameAs;
                }
                break;

            case SortIntegers:
                int     currentInt = ((Integer) v.elementAt(index)).intValue();

                if ( currentInt > pivotInteger.intValue())      result = dir;
                else
                {
                    result = ( currentInt < pivotInteger.intValue()) ? -dir : kSameAs;
                }
                break;

            default:
                result = dir * ComparePageReferences( v, index, pivotRef, type);
                break;
        }
        return result;
    }
}