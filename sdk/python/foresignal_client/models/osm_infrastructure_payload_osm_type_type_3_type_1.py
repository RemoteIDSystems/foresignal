from enum import StrEnum


class OsmInfrastructurePayloadOsmTypeType3Type1(StrEnum):
    NODE = "node"
    RELATION = "relation"
    WAY = "way"

    def __str__(self) -> str:
        return str(self.value)
