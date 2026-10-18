from enum import StrEnum


class SocialSettingsAttentionGapMinSeverity(StrEnum):
    NOTABLE = "notable"
    SIGNIFICANT = "significant"

    def __str__(self) -> str:
        return str(self.value)
