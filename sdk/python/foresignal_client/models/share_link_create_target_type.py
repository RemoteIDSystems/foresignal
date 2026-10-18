from enum import StrEnum


class ShareLinkCreateTargetType(StrEnum):
    ALERT = "alert"
    BRIEFING_RUN = "briefing-run"
    EXPORT = "export"
    RECORD = "record"
    TRACK = "track"
    VIEW = "view"

    def __str__(self) -> str:
        return str(self.value)
