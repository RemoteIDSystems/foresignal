from enum import StrEnum


class ListSentPackagesStatus(StrEnum):
    BUILDING = "building"
    DELIVERED = "delivered"
    DELIVERING = "delivering"
    FAILED = "failed"
    READY = "ready"

    def __str__(self) -> str:
        return str(self.value)
