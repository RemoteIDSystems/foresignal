from enum import StrEnum


class GetWmtsKvpSERVICE(StrEnum):
    WMTS = "WMTS"

    def __str__(self) -> str:
        return str(self.value)
