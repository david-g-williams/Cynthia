package io.cynthia.core.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true)
@Builder
@Data
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class LabelScore {
    double score;
    String label;

    private LabelScore(final double score, @NonNull final String label) {
        this.label = label;
        this.score = score;
    }

    public static LabelScore of(final double score, @NonNull final String label) {
        return new LabelScore(score, label);
    }
}
