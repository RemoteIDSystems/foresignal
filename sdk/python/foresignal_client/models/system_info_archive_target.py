from enum import StrEnum


class SystemInfoArchiveTarget(StrEnum):
    LOCAL = "local"
    S3 = "s3"

    def __str__(self) -> str:
        return str(self.value)
