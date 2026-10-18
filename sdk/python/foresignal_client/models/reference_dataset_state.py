from enum import StrEnum


class ReferenceDatasetState(StrEnum):
    ABSENT = "absent"
    FAILED = "failed"
    LOADED = "loaded"
    LOADING = "loading"

    def __str__(self) -> str:
        return str(self.value)
