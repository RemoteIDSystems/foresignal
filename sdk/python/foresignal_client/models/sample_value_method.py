from enum import StrEnum


class SampleValueMethod(StrEnum):
    BILINEAR = "bilinear"
    NEAREST = "nearest"

    def __str__(self) -> str:
        return str(self.value)
