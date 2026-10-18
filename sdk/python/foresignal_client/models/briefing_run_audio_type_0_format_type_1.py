from enum import StrEnum


class BriefingRunAudioType0FormatType1(StrEnum):
    MP3 = "mp3"
    OPUS = "opus"

    def __str__(self) -> str:
        return str(self.value)
