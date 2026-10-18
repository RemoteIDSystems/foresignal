from enum import StrEnum


class ChannelTypeId(StrEnum):
    APPRISE = "apprise"
    DISCORD = "discord"
    EMAIL = "email"
    GOOGLE_CHAT = "google-chat"
    GOTIFY = "gotify"
    HOMEASSISTANT = "homeassistant"
    MATRIX = "matrix"
    MATTERMOST = "mattermost"
    MESHCORE = "meshcore"
    MESHTASTIC = "meshtastic"
    MQTT = "mqtt"
    NTFY = "ntfy"
    PUSHOVER = "pushover"
    SLACK = "slack"
    TEAMS = "teams"
    TELEGRAM = "telegram"
    WEBHOOK = "webhook"
    WEBPUSH = "webpush"
    ZULIP = "zulip"

    def __str__(self) -> str:
        return str(self.value)
