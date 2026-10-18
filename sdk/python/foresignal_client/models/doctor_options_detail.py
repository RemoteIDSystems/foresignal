from enum import StrEnum


class DoctorOptionsDetail(StrEnum):
    COMPACT = "compact"
    FULL = "full"

    def __str__(self) -> str:
        return str(self.value)
