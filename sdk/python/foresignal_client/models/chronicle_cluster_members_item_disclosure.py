from enum import StrEnum


class ChronicleClusterMembersItemDisclosure(StrEnum):
    FULL = "full"
    OPAQUE = "opaque"

    def __str__(self) -> str:
        return str(self.value)
