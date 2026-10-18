from enum import StrEnum


class SearchDocTypeItem(StrEnum):
    ALERT = "alert"
    BRIEFING = "briefing"
    RECORD = "record"

    def __str__(self) -> str:
        return str(self.value)
