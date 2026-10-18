from enum import StrEnum


class LlmProviderRolesItem(StrEnum):
    CHAT = "chat"
    EMBEDDING = "embedding"
    TTS = "tts"

    def __str__(self) -> str:
        return str(self.value)
