package com.chimbori.crux.common

import com.chimbori.crux.common.HeuristicString.CandidateFound
import org.junit.Assert.*
import org.junit.Test

class HeuristicStringTest {
  @Test
  fun testOriginalStringIsRetained() {
    try {
      HeuristicString("original").or("changed")
    } catch (candidateFound: CandidateFound) {
      assertEquals("original", candidateFound.candidate)
    }
  }

  @Test
  fun testChangedStringIsSetIfOriginalIsNull() {
    try {
      HeuristicString(null).or("changed")
    } catch (candidateFound: CandidateFound) {
      assertEquals("changed", candidateFound.candidate)
    }
  }

  @Test
  fun testChangedStringIsSetIfOriginalIsEmpty() {
    try {
      HeuristicString("").or("changed")
    } catch (candidateFound: CandidateFound) {
      assertEquals("changed", candidateFound.candidate)
    }
  }

  @Test
  fun testOriginalStringIsRetainedIfChangedStringIsNull() {
    try {
      HeuristicString("original").or(null)
    } catch (candidateFound: CandidateFound) {
      assertEquals("original", candidateFound.candidate)
    }
  }

  @Test
  fun testOriginalStringIsRetainedIfChangedStringIsEmpty() {
    try {
      HeuristicString("original").or("")
    } catch (candidateFound: CandidateFound) {
      assertEquals("original", candidateFound.candidate)
    }
  }

  @Test
  fun testNullOriginalStringIsPreservedIfAllChangedStringsAreNull() {
    try {
      assertNull(HeuristicString(null).or(null).toString())
    } catch (candidateFound: CandidateFound) {
      assertEquals(null, candidateFound.candidate)
    }
  }

  @Test
  fun testThatSubsequentStringsAreNotEvaluatedIfOneCandidateHasAlreadyBeenFound() {
    try {
      assertNull(HeuristicString("original")
          .or(getNewCandidate_ShouldNeverBeCalled())
          .toString())
    } catch (candidateFound: CandidateFound) {
      assertEquals("original", candidateFound.candidate)
    }
  }

  private fun getNewCandidate_ShouldNeverBeCalled(): String {
    fail("If an existing candidate is available, subsequent candidates should not be evaluated.")
    return "changed"
  }
}
