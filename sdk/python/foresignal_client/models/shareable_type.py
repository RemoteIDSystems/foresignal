from enum import StrEnum


class ShareableType(StrEnum):
    ANNOTATION = "annotation"
    ATTACHMENT = "attachment"
    CHRONICLE = "chronicle"
    EVIDENCE_ITEM = "evidence-item"
    INVESTIGATION = "investigation"
    REPORT = "report"
    SAVED_SEARCH = "saved-search"
    TAG = "tag"
    WORKSPACE = "workspace"

    def __str__(self) -> str:
        return str(self.value)
