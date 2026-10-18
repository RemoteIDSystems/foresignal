from enum import StrEnum


class StructureDiffFormat(StrEnum):
    BINARY = "binary"
    CSV = "csv"
    HTML = "html"
    JSON = "json"
    TEXT = "text"
    TSV = "tsv"
    XML = "xml"

    def __str__(self) -> str:
        return str(self.value)
