from enum import StrEnum


class BriefingRunAudioType0Status(StrEnum):
    FAILED = "failed"
    OK = "ok"
    SKIPPED = "skipped"

    def __str__(self) -> str:
        return str(self.value)
