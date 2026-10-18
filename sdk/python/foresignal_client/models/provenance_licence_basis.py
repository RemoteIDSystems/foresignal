from enum import StrEnum


class ProvenanceLicenceBasis(StrEnum):
    BATCH = "batch"
    DESCRIPTOR = "descriptor"
    MLAT_OVERRIDE = "mlat-override"
    PEER_ORIGIN = "peer-origin"

    def __str__(self) -> str:
        return str(self.value)
