package epic.sequences

import epic.slab.annotators.{Tagger => SlabTagger}
import epic.slab._
import epic.trees.AnnotatedLabel

object Tagger {
  // Merges tag information back into the Vector[Tagged[Tag]] format.
  def tag[Tag](fun: Vector[String] => Vector[Tag])(content: String, tokens: Vector[Token]): Vector[Tagged[Tag]] = {
    val strings = tokens.map(t => content.substring(t.span.begin, t.span.end))
    val tagSeq = fun(strings)
    tokens.zip(tagSeq).map({case (token, tag) => Tagged[Tag](token.span, tag)})
  }
  def apply[Tag](fun: Vector[String] => Vector[Tag]): SlabTagger[Sentence, Token, Tag] = new SlabTagger[Sentence, Token, Tag](fun)

  def posTagger(crf: CRF[AnnotatedLabel, String]) = fromCRF(crf, (a: AnnotatedLabel) => a.label)

  def fromCRF[L, Tag](crf: CRF[L, String], lToTag: L=>Tag): SlabTagger[Sentence, Token, Tag] = new CRFTagger[Sentence, Token, L, Tag](crf, lToTag)

  case class CRFTagger[S <: Sentence, T <: Token, L, Tag](crf: CRF[L, String], lToTag: L=>Tag) extends SlabTagger[S, T, Tag](v1 => crf.bestSequence(v1).tags.map(lToTag).toVector)
}
