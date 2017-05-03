import java.io.*;
import java.util.*;

/**
 * Created by anish on 4/28/17.
 */
public class Dataset {
    private LinkedList transactionList = new LinkedList();
    public Dataset(String filename) throws IOException {
        LineNumberReader lineReader = new LineNumberReader(
                new InputStreamReader(new FileInputStream(filename)));
        String line = null;
        while ((line = lineReader.readLine()) != null) {
            Itemset newItemset = new Itemset();
            StringTokenizer tokenizer = new StringTokenizer(line, " ,\t");
            while (tokenizer.hasMoreTokens()) {
                newItemset.addItem(new Item(tokenizer.nextToken()));
            }
            // ignore all empty itemsets
            if (newItemset.size() != 0) {
                transactionList.add(newItemset);
            }
        }
    }

    public void dumpItemsets() {
        Iterator itItemset = getTransactionIterator();
        while (itItemset.hasNext()) {
            Itemset itemset = (Itemset) itItemset.next();
            System.out.println(itemset.toString());
        }
    }

    public Iterator getTransactionIterator() {
        return transactionList.iterator();
    }

    public int getNumTransactions() {
        return transactionList.size();
    }


    public double computeSupportForItemset(Itemset itemset) {
        int occurrenceCount = 0;
        Iterator itItemset = getTransactionIterator();
        while (itItemset.hasNext()) {
            Itemset shoppingList = (Itemset) itItemset.next();
            if (shoppingList.intersectWith(itemset).size() == itemset.size()) {
                occurrenceCount++;
            }
        }
        return ((double) occurrenceCount) / getNumTransactions();
    }

    public double computeConfidenceForAssociationRule(
            AssociationRule associationRule) {
        Itemset union = associationRule.getItemsetA().unionWith(
                associationRule.getItemsetB());
        return computeSupportForItemset(union)
                / computeSupportForItemset(associationRule.getItemsetA());
    }

    public Set getAllItemsetsOfSizeOne() {
        Iterator itItemset = getTransactionIterator();
        Itemset bigUnion = new Itemset();
        while (itItemset.hasNext()) {
            Itemset itemset = (Itemset) itItemset.next();
            bigUnion = bigUnion.unionWith(itemset);
        }

        // break up the big unioned itemset into one element itemsets
        HashSet allItemsets = new HashSet();
        Iterator itItem = bigUnion.getItemIterator();
        while (itItem.hasNext()) {
            Item item = (Item) itItem.next();
            Itemset itemset = new Itemset();
            itemset.addItem(item);
            allItemsets.add(itemset);
        }

        return allItemsets;
    }

    public Collection runApriori(double minSupport, double minConfidence) {
        Collection discoveredAssociationRules = new LinkedList();

        // generate candidate itemsets
        final int MAX_NUM_ITEMS = 100;
        Set[] candidates = new Set[MAX_NUM_ITEMS];
        candidates[1] = getAllItemsetsOfSizeOne();
        for (int numItems = 1; numItems < MAX_NUM_ITEMS
                && !candidates[numItems].isEmpty(); numItems++) {
            candidates[numItems + 1] = new HashSet();
            for (Iterator itItemset1 = candidates[numItems].iterator(); itItemset1
                    .hasNext(); ) {
                Itemset itemset1 = (Itemset) itItemset1.next();
                for (Iterator itItemset2 = candidates[numItems].iterator(); itItemset2
                        .hasNext(); ) {
                    Itemset itemset2 = (Itemset) itItemset2.next();
                    if (itemset1.intersectWith(itemset2).size() == numItems - 1) {
                        Itemset candidateItemset = itemset1.unionWith(itemset2);
                        assert (candidateItemset.size() == numItems + 1);
                        if (computeSupportForItemset(candidateItemset) > minSupport) {
                            candidates[numItems + 1].add(candidateItemset);
                        }
                    }
                }
            }
        }
        // generate association rules from candidate itemsets
        for (int numItems = 1; numItems < MAX_NUM_ITEMS
                && !candidates[numItems].isEmpty(); numItems++) {
            for (Iterator itItemsetCandidate = candidates[numItems].iterator(); itItemsetCandidate
                    .hasNext();) {
                Itemset itemsetCandidate = (Itemset) itItemsetCandidate.next();
                for (Iterator itItemsetSub = itemsetCandidate
                        .generateAllNonEmptySubsets().iterator(); itItemsetSub
                             .hasNext();) {
                    Itemset itemsetSub = (Itemset) itItemsetSub.next();
                    Itemset itemsetA = itemsetSub;
                    Itemset itemsetB = itemsetCandidate.minusAllIn(itemsetSub);
                    AssociationRule candidateAssociationRule = new AssociationRule(
                            itemsetA, itemsetB);
                    if (computeConfidenceForAssociationRule(candidateAssociationRule) > minConfidence) {
                        discoveredAssociationRules
                                .add(candidateAssociationRule);

                    }
                }
            }
        }
        return discoveredAssociationRules;
    }

    public static void main(String[] args) {
        try {
            Dataset dataset = new Dataset(
                    "/home/anish/IdeaProjects/FinalYearProject/src/Dataset.txt");
            Collection discoveredAssociationRules = dataset
                    .runApriori(0.12, 0.51);

            Iterator itAssociationRule = discoveredAssociationRules.iterator();
            while (itAssociationRule.hasNext()) {
                AssociationRule associationRule = (AssociationRule) itAssociationRule
                        .next();
                System.out
                        .println("assoctiation rule: "
                                + associationRule
                                + "\tsupport: "
                                + dataset
                                .computeSupportForItemset(associationRule
                                        .getItemsetA().unionWith(
                                                associationRule
                                                        .getItemsetB()))
                                + "\tconfidence: "
                                + dataset.computeConfidenceForAssociationRule(associationRule));

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
