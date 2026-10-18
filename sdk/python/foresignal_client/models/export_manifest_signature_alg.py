from enum import StrEnum


class ExportManifestSignatureAlg(StrEnum):
    ED25519 = "Ed25519"
    EDDSA = "EdDSA"

    def __str__(self) -> str:
        return str(self.value)
