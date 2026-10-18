from enum import StrEnum


class FindingEventEvent(StrEnum):
    FINDING_CREATED = "finding.created"
    FINDING_DISMISSED = "finding.dismissed"
    FINDING_UPDATED = "finding.updated"

    def __str__(self) -> str:
        return str(self.value)
