from enum import StrEnum


class McpAuthSettingsAllowedAlgsItem(StrEnum):
    ED25519 = "Ed25519"
    EDDSA = "EdDSA"
    ES256 = "ES256"
    RS256 = "RS256"

    def __str__(self) -> str:
        return str(self.value)
