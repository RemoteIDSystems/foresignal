from enum import StrEnum


class NotificationPreferenceChannelType(StrEnum):
    DISCORD = "discord"
    EMAIL = "email"
    GOOGLE_CHAT = "google-chat"
    GOTIFY = "gotify"
    MATRIX = "matrix"
    MATTERMOST = "mattermost"
    NTFY = "ntfy"
    PUSHOVER = "pushover"
    SLACK = "slack"
    TEAMS = "teams"
    TELEGRAM = "telegram"
    WEBHOOK = "webhook"
    WEBPUSH = "webpush"

    def __str__(self) -> str:
        return str(self.value)
