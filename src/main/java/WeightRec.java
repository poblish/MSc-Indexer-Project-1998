/*
    WeightRec.java

    When we have to pick out the most important terms by means of weights
    we calculate the smallest Salton weight for each, and create a WeightRec
    for each to store the weight itself and the original index of the entry
    because sorting will obviously change the order of the terms.
*/

public class WeightRec      // 24/7/98 at 14:48
{
    private int     fIndex;
    private double  fWeight;

    int GetIndex()      { return fIndex; }
    double  GetWeight() { return fWeight; }

    /*******************************************************************************
        WeightRec::WeightRec
    *******************************************************************************/
    public WeightRec( int index, double weight)
    {
        fIndex = index;
        fWeight = weight;
    }
}