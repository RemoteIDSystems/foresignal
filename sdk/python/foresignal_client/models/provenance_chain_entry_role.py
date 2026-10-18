from enum import StrEnum


class ProvenanceChainEntryRole(StrEnum):
    AUTHOR = "author"
    FORWARDER = "forwarder"
    IMPORTER = "importer"
    ORIGIN = "origin"

    def __str__(self) -> str:
        return str(self.value)
