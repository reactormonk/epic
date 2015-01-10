package epic.preprocess

/*
 Copyright 2009 David Hall, Daniel Ramage

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

import java.text.BreakIterator
import java.util.Locale
import epic.slab._
import epic.slab.annotators.SentenceSegmenter
import epic.trees.SpanConvert._

/**
 * A Sentence Segmenter backed by Java's BreakIterator.
 * Given an input string, it will return an iterator over sentences
 *
 * @author dlwh
 */
class JavaSentenceSegmenter(locale: Locale = Locale.getDefault) extends SentenceSegmenter {
  def apply(content: String): Vector[Sentence] = {
    val breaker = BreakIterator.getSentenceInstance(locale)
    breaker.setText(content)
    new SegmentingIterator(breaker).map({ span =>
      Sentence(span)
    }).toVector
  }

}

object JavaSentenceSegmenter extends JavaSentenceSegmenter(Locale.getDefault)
