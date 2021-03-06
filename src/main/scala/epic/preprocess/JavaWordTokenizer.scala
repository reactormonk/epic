package epic.preprocess

/*
 Copyright 2010 David Hall, Daniel Ramage

 Licensed under the Apache License, Version 2.0 (the "License")
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

import java.text._
import java.util.Locale
import epic.trees.Span
import epic.slab._
import epic.slab.annotators.Tokenizer


/**
 * A Word Segmenter backed by Java's BreakIterator.
 * Given an input string, it will return an iterator over sentences
 * Doesn't return spaces, does return punctuation.
 *
 * @author dlwh
 */

class JavaWordTokenizer[S <: Sentence](locale: Locale) extends Tokenizer[S, Token] {
  def this() = this(Locale.getDefault)

  def apply(content: String, sentences: Vector[Sentence]): Vector[ContentToken] = {
    sentences.flatMap { s =>
      val breaker = BreakIterator.getWordInstance(locale)
      breaker.setText(content)
      new SegmentingIterator(breaker, s.begin, s.end).map { span =>
        ContentToken(span, content.substring(span.begin, span.end))
      }.filterNot(_.content.forall(_.isWhitespace))
    }
  }

  def apply(sentence: String): Vector[ContentToken] =
    apply(sentence, Vector(Sentence(Span(0, sentence.length))))
}

object JavaWordTokenizer extends JavaWordTokenizer


