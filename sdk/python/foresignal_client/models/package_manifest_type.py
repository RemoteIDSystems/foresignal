from enum import StrEnum


class PackageManifestType(StrEnum):
    CHRONICLE_EXTRACT = "chronicle-extract"
    EVIDENCE = "evidence"
    INVESTIGATION = "investigation"
    REPORT = "report"

    def __str__(self) -> str:
        return str(self.value)
