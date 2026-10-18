from enum import StrEnum


class SocialPlatformSettingsOptionsFieldsParam(StrEnum):
    POST = "post"
    TWEET = "tweet"

    def __str__(self) -> str:
        return str(self.value)
