from enum import StrEnum


class ExchangePackageInputDeliver(StrEnum):
    FILE = "file"
    PEER = "peer"

    def __str__(self) -> str:
        return str(self.value)
