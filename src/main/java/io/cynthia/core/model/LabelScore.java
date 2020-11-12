package io.cynthia.core.model;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Value
public class LabelScore {
    double score;
    String label;

    public static LabelScore of(final double score, @NonNull final String label) {
        return new LabelScore(score, label);
    }
}
