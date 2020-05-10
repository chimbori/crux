package com.chimbori.crux.common

import com.chimbori.crux.common.HeuristicString.CandidateFound
import org.junit.Assert.*
import org.junit.Test

class HeuristicStringTest {
  @Test
  fun testOriginalStringIsRetained() {
    try {
      HeuristicString()
          .or("original")
          .or("changed")
    } catch (candidateFound: CandidateFound) {
      assertEquals("original", candidateFound.candidate)
    }
  }

  @Test
  fun testChangedStringIsSetIfOriginalIsNull() {
    try {
      HeuristicString()
          .or("changed")
    } catch (candidateFound: CandidateFound) {
      assertEquals("changed", candidateFound.candidate)
    }
  }

  @Test
  fun testChangedStringIsSetIfOriginalIsEmpty() {
    try {
      HeuristicString().or("changed")
    } catch (candidateFound: CandidateFound) {
      assertEquals("changed", candidateFound.candidate)
    }
  }

  @Test
  fun testOriginalStringIsRetainedIfChangedStringIsNull() {
    try {
      HeuristicString().or(null)
    } catch (candidateFound: CandidateFound) {
      assertEquals("original", candidateFound.candidate)
    }
  }

  @Test
  fun testOriginalStringIsRetainedIfChangedStringIsEmpty() {
    try {
      HeuristicString().or("")
    } catch (candidateFound: CandidateFound) {
      assertEquals("original", candidateFound.candidate)
    }
  }

  @Test
  fun testNullOriginalStringIsPreservedIfAllChangedStringsAreNull() {
    try {
      HeuristicString().or(null)
    } catch (candidateFound: CandidateFound) {
      assertNull(candidateFound.candidate)
    }
  }

  @Test
  fun testThatSubsequentStringsAreNotEvaluatedIfOneCandidateHasAlreadyBeenFound() {
    try {
      HeuristicString()
          .or("original")
          .or(getNewCandidate_ShouldNeverBeCalled())
    } catch (candidateFound: CandidateFound) {
      assertEquals("original", candidateFound.candidate)
    }
  }

  private fun getNewCandidate_ShouldNeverBeCalled(): String {
    fail("If an existing candidate is available, subsequent candidates should not be evaluated.")
    return "changed"
  }
}
