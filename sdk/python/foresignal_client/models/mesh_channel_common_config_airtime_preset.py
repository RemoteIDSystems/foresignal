from enum import StrEnum


class MeshChannelCommonConfigAirtimePreset(StrEnum):
    CUSTOM = "custom"
    MESHTASTIC_LONGFAST = "meshtastic-longfast"
    MESHTASTIC_MEDIUMFAST = "meshtastic-mediumfast"

    def __str__(self) -> str:
        return str(self.value)
