from enum import StrEnum


class ApnsProviderConfigKeyEnvironment(StrEnum):
    BOTH = "both"
    PRODUCTION = "production"
    SANDBOX = "sandbox"

    def __str__(self) -> str:
        return str(self.value)
