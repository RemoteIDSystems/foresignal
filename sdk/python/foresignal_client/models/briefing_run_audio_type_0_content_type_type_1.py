from enum import StrEnum


class BriefingRunAudioType0ContentTypeType1(StrEnum):
    AUDIOMPEG = "audio/mpeg"
    AUDIOOGG = "audio/ogg"

    def __str__(self) -> str:
        return str(self.value)
