from enum import StrEnum


class PackageManifestAuthorsItemRole(StrEnum):
    AUTHOR = "author"
    FORWARDER = "forwarder"

    def __str__(self) -> str:
        return str(self.value)
