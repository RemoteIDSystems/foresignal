from enum import StrEnum


class PushDeviceEnvironmentType2Type1(StrEnum):
    PRODUCTION = "production"
    SANDBOX = "sandbox"

    def __str__(self) -> str:
        return str(self.value)
