from enum import StrEnum


class ReceivedPackageAuthorsItemRole(StrEnum):
    AUTHOR = "author"
    FORWARDER = "forwarder"

    def __str__(self) -> str:
        return str(self.value)
