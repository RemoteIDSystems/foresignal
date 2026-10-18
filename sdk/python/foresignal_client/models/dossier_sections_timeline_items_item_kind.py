from enum import StrEnum


class DossierSectionsTimelineItemsItemKind(StrEnum):
    ALERT = "alert"
    ANNOTATION = "annotation"
    FINDING = "finding"
    OTHER = "other"
    SITUATION_LOG = "situation-log"
    VERSION = "version"

    def __str__(self) -> str:
        return str(self.value)
