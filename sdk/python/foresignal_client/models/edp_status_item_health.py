from enum import StrEnum


class EdpStatusItemHealth(StrEnum):
    BLOCKED = "blocked"
    DEGRADED = "degraded"
    GEO_RESTRICTED = "geo-restricted"
    LAYOUT_CHANGED = "layout-changed"
    OK = "ok"

    def __str__(self) -> str:
        return str(self.value)
