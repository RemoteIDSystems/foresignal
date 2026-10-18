from enum import StrEnum


class OgcItemsFilterLang(StrEnum):
    CQL2_JSON = "cql2-json"
    CQL2_TEXT = "cql2-text"

    def __str__(self) -> str:
        return str(self.value)
