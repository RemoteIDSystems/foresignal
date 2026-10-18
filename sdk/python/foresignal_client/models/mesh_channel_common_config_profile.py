from enum import StrEnum


class MeshChannelCommonConfigProfile(StrEnum):
    CBOR = "cbor"
    TEXT = "text"

    def __str__(self) -> str:
        return str(self.value)
