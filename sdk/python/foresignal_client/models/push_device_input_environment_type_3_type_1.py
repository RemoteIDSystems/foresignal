from enum import StrEnum


class PushDeviceInputEnvironmentType3Type1(StrEnum):
    PRODUCTION = "production"
    SANDBOX = "sandbox"

    def __str__(self) -> str:
        return str(self.value)
