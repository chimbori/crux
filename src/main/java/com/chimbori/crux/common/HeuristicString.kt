package com.chimbori.crux.common

import com.chimbori.crux.common.HeuristicString.CandidateFound

/**
 * A string that is determined heuristically, with a fluent API. When extracting meaning from
 * arbitrary content, a [HeuristicString] offers a quick way to assign the first matching
 * candidate out of several possible candidates, and ignores all further attempts to set it after
 * a candidate has been picked.
 *
 * When a candidate is found, it is returned via the [CandidateFound] Exception. This is to
 * ensure that other pending calls to [.or] are not executed, and the caller can skip
 * evaluating other candidates as soon as the first one is found.
 */
class HeuristicString {
  @Throws(CandidateFound::class)
  fun or(candidate: String?): HeuristicString {
    if (!candidate.isNullOrBlank()) {
      throw CandidateFound(candidate)
    }
    return this
  }

  class CandidateFound(val candidate: String?) : Exception()
}
