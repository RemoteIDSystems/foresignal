from enum import StrEnum


class DoctorOptionsLlm(StrEnum):
    AUTO = "auto"
    OFF = "off"
    ON = "on"

    def __str__(self) -> str:
        return str(self.value)
