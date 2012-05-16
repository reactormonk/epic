package scalanlp.parser.features

import scalanlp.epic.Feature

/**
 *
 * @author dlwh
 */

trait SpanFeature extends Feature

object StandardSpanFeatures {
  case class SpanLengthFeature(dist: Int) extends SpanFeature
  case class EndSentFeature[L](label: L) extends SpanFeature
  case class BeginSentFeature[L](label: L) extends SpanFeature
  // Huang's WordEdges Feature without distance
  case class WordEdges[L, W](label: L, left: W, right: W) extends SpanFeature
}

