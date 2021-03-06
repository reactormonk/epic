package org.scalanlp.epic.opennlp

import epic.slab._
import epic.slab.typeclasses._
import shapeless._
import opennlp.tools.parser._
import SpanToSpan._
import scala.collection.JavaConversions._

class ParseTag(val span: Span, val parseType: String, val probability: Double) extends ProbabilityAnnotation with SpanAnnotation

object ParseTag {
  def apply(span: Span, tag: String, probability: Double): ParseTag = new ParseTag(span, tag, probability)
}

class Parser[S <: Sentence, T <: Token](
  val model: ParserModel,
  val parser: ParserModel => opennlp.tools.parser.Parser
) extends AnalysisFunctionN1[String, List[S] :: List[T] :: HNil, ParseTag] {
  override def apply[In <: HList, Out <: HList](slab: Slab[String, In])(
    implicit sel: SelectMany.Aux[In, List[S] :: List[T] :: HNil, List[S] :: List[T] :: HNil],
    adder: Adder.Aux[In, List[ParseTag], Out]
  ): Slab[String, Out] = {
    val data = slab.selectMany(sel)
    val index = SpanIndex(data.select[List[T]])
    // Required because the API is not threadsafe.
    val pmodel = parser(model)
    val annotatedSentences: List[List[ParseTag]] = data.select[List[S]].flatMap({ sentence =>
      val s = slab.substring(sentence.span)
      val tokens = index(sentence.span)
      val unparsed = new Parse(s, Span(0, sentence.end - sentence.begin), "INC", 1, null)
      tokens.map(t => unparsed.insert(new Parse(s, t.span.offset(-sentence.begin), "TK", 0.0, 0)))
      val parsed = if(unparsed.getChildCount > 0) { Some(pmodel.parse(unparsed)) } else { None }
      parsed.map(p => transform(List(p), sentence.begin))
    })
    slab.add(annotatedSentences.flatten)(adder)
  }

  def transform(parses: List[Parse], offset: Int): List[ParseTag] = {
    parses.flatMap({child =>
      List(ParseTag(child.getSpan().offset(offset), child.getType, child.getProb)) ++ transform(child.getChildren.toList, offset)
    })
  }
}

object Parser {
  def parser(): ParserModel => opennlp.tools.parser.Parser = { model => ParserFactory.create(model)}
  def apply(model: ParserModel): Parser[PSentence, PToken] = apply(model, Parser.parser())
  def apply(
    model: ParserModel,
    tagger: ParserModel => opennlp.tools.parser.Parser
  ): Parser[PSentence, PToken] = new Parser[PSentence, PToken](model, tagger)
}
