from enum import StrEnum


class ExternalSourceStateEventChange(StrEnum):
    APPROVED = "approved"
    DELETED = "deleted"
    MANIFEST_CHANGED = "manifest-changed"
    REGISTERED = "registered"
    SUSPENDED = "suspended"

    def __str__(self) -> str:
        return str(self.value)
