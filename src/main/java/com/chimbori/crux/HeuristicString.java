package com.chimbori.crux;

/**
 * A string that is determined heuristically, with a fluent API. When extracting meaning from
 * arbitrary content, a {@link HeuristicString} offers a quick way to assign the first matching
 * candidate out of several possible candidates, and ignores all further attempts to set it after
 * a candidate has been picked.
 *
 * When a candidate is found, it is returned via the {@link CandidateFound} Exception. This is to
 * ensure that other pending calls to {@link .or(...)} are not executed, and the caller can skip
 * evaluating other candidates as soon as the first one is found.
 */
class HeuristicString {
  private String string = null;

  public HeuristicString(String string) throws CandidateFound {
    this.string = string;
    if (string != null && !string.isEmpty()) {
      throw new CandidateFound(string);
    }
  }

  public HeuristicString or(String candidate) throws CandidateFound {
    if (candidate != null) {
      if (string == null || string.isEmpty()) {
        string = candidate;
      } else {
        throw new CandidateFound(string);
      }
    }
    return this;
  }

  @Override
  public String toString() {
    return string;
  }

  public static class CandidateFound extends Exception {
    public final String candidate;

    public CandidateFound(String candidate) {
      this.candidate = candidate;
    }
  }
}
