from enum import StrEnum


class DownloadExportBundle(StrEnum):
    ZIP = "zip"

    def __str__(self) -> str:
        return str(self.value)
