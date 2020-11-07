package io.cynthia.core.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true)
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LabelScore {
    double score;
    String label;

    public static LabelScore of(final double score, @NonNull final String label) {
        return new LabelScore(score, label);
    }
}
