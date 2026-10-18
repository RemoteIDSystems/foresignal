from enum import StrEnum


class ReceivedPackageReceivedVia(StrEnum):
    IMPORT = "import"
    PEER = "peer"

    def __str__(self) -> str:
        return str(self.value)
