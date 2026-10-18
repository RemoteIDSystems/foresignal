from enum import StrEnum


class AttentionTopicSocialMode(StrEnum):
    CUE_ONLY = "cue-only"
    STANDING = "standing"

    def __str__(self) -> str:
        return str(self.value)
