package scalanlp.parser

import collection.mutable.ArrayBuffer
import scalanlp.trees._

/**
 *
 * @author dlwh
 */
trait ParserTestHarness {
  def getTrainTreesAndReplacer(binarization:(Tree[String]=>BinarizedTree[String]) = (Trees.xBarBinarize _),
                    maxLength:Int= 15) = {
    val treebank = {
      TstTreebank.treebank;
    }
    val trees = massageTrees(treebank.trainTrees,binarization,maxLength);
    (new UnaryChainRemover[String]).removeUnaryChains(trees.iterator);
  }

  def getTestTrees(binarization:(Tree[String]=>BinarizedTree[String]) = (Trees.xBarBinarize _),
                   maxLength:Int= 15) = {
    val treebank = {
      TstTreebank.treebank;
    }
    massageTrees(treebank.testTrees,binarization,maxLength);
  }

  def massageTrees(trees: Iterator[(Tree[String],Seq[String])],
                   binarize:(Tree[String]=>BinarizedTree[String]) = (Trees.xBarBinarize _),
                   maxLength:Int=15) = {
    val xform = Trees.Transforms.StandardStringTransform;
    val trainTrees = ArrayBuffer() ++= (for( (tree,words) <- trees.filter(_._2.length <= maxLength))
    yield (binarize(xform(tree)),words));

    trainTrees
  }


  def evalParser(testTrees: IndexedSeq[(Tree[String],Seq[String])],parser: Parser[String,String]) = {
    val (prec,recall,exact) = ParseEval.evaluate(testTrees.map { case (t,w) => (t,w,SpanScorer.identity)},parser, ParserTestHarness.unaryReplacer);
    val f1 = (2 * prec * recall)/(prec + recall);
    (prec,recall,exact,f1);
  }


}

object ParserTestHarness extends ParserTestHarness {
  val ((simpleLexicon,simpleGrammar), unaryReplacer) = {
    val (trees,replacer) = getTrainTreesAndReplacer();
    (GenerativeParser.extractLexiconAndGrammar(trees.iterator),replacer);
  }
  val simpleParser = {
    val chartBuilder = new CKYChartBuilder[ParseChart.ViterbiParseChart, String, String]("", simpleLexicon, simpleGrammar, ParseChart.viterbi)
    ChartParser(chartBuilder);
  }
}