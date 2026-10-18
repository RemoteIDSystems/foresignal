from enum import StrEnum


class PushDeviceInputEnvironmentType2Type1(StrEnum):
    PRODUCTION = "production"
    SANDBOX = "sandbox"

    def __str__(self) -> str:
        return str(self.value)
