/**
 * Copyright 2017 Twitter. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.graphjet.algorithms.filters;

import com.twitter.graphjet.algorithms.RecommendationRequest;
import com.twitter.graphjet.hashing.SmallArrayBasedLongToDoubleMap;
import com.twitter.graphjet.stats.StatsReceiver;

/**
 * This filter assumes the social proofs' edge metadata represent timestamps.
 * Guarantees that the social proofs for a result node at at least older than a
 * specified value, ex. at least 3 days old. If any social proof is younger, the node is filtered
 */
public class RecentEdgeMetadataFilter extends ResultFilter {
  private final long youngestTimestampMillis;
  private final byte socialProofType;

  /**
   * @param minTimeFromNowInMillis The minimum age of a social proof edge to not have it filtered
   * @param socialProofType Only check social proof edges of this type
   * @param statsReceiver
   */
  public RecentEdgeMetadataFilter(
    long minTimeFromNowInMillis,
    byte socialProofType,
    StatsReceiver statsReceiver
  ) {
    super(statsReceiver);
    this.youngestTimestampMillis = System.currentTimeMillis() - minTimeFromNowInMillis;
    this.socialProofType = socialProofType;
  }

  @Override
  public void resetFilter(RecommendationRequest request) { }

  @Override
  public boolean filterResult(long resultNode, SmallArrayBasedLongToDoubleMap[] socialProofs) {
    SmallArrayBasedLongToDoubleMap socialProof = socialProofs[socialProofType];
    if (socialProof == null) {
      return false;
    }

    long[] allMetadata = socialProof.metadata();
    int numMetadata = socialProof.size();

    for (int i = 0; i < numMetadata; i++) {
      long timestamp = allMetadata[i];
      if (youngestTimestampMillis < timestamp) {
        return true; // Too young, filter
      }
    }
    return false;
  }
}
