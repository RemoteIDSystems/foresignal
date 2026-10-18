from enum import StrEnum


class SocialCueBackend(StrEnum):
    PLATFORM_APIS = "platform-apis"
    XAI_X_SEARCH = "xai-x-search"

    def __str__(self) -> str:
        return str(self.value)
