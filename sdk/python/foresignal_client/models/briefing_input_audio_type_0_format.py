from enum import StrEnum


class BriefingInputAudioType0Format(StrEnum):
    MP3 = "mp3"
    OPUS = "opus"

    def __str__(self) -> str:
        return str(self.value)
