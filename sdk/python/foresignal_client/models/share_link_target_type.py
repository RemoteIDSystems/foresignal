from enum import StrEnum


class ShareLinkTargetType(StrEnum):
    ALERT = "alert"
    BRIEFING_RUN = "briefing-run"
    EDITION_SITE = "edition-site"
    EXPORT = "export"
    RECORD = "record"
    REPORT = "report"
    TRACK = "track"
    VIEW = "view"
    WORKSPACE = "workspace"

    def __str__(self) -> str:
        return str(self.value)
