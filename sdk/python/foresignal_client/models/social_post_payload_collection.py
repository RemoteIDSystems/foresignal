from enum import StrEnum


class SocialPostPayloadCollection(StrEnum):
    CUE = "cue"
    IMPORT = "import"
    SAMPLE = "sample"
    SEARCH = "search"
    STREAM = "stream"

    def __str__(self) -> str:
        return str(self.value)
