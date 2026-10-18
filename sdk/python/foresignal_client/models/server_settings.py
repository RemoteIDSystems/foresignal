from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.client_links import ClientLinks
    from ..models.push_settings import PushSettings
    from ..models.settings_warning import SettingsWarning


T = TypeVar("T", bound="ServerSettings")


@_attrs_define
class ServerSettings:
    """
    Attributes:
        public_base_url (str): Base URL used in notification links Example: http://foresignal.local:8080.
        timezone (str | Unset): IANA zone; fallback for channel timezones Default: 'UTC'. Example: America/Chicago.
        client_links (ClientLinks | Unset): Deep-link templates clients and notifications use (023). Defaults use the
            `foresignal://` scheme; operators may set verified https links.
        warnings (list[SettingsWarning] | Unset): PUBLIC_BASE_URL_INSECURE when publicBaseUrl is http:// on a non-local
            host; PUBLIC_BASE_URL_MISMATCH when requests arrive over https
        push (PushSettings | Unset): Push settings inside ServerSettings (031)
        contact_email (None | str | Unset): Operator contact appended to the upstream User-Agent as `contact=…` (the NWS
            API asks for a contact); null for none Example: ops@example.org.
    """

    public_base_url: str
    timezone: str | Unset = "UTC"
    client_links: ClientLinks | Unset = UNSET
    warnings: list[SettingsWarning] | Unset = UNSET
    push: PushSettings | Unset = UNSET
    contact_email: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        public_base_url = self.public_base_url

        timezone = self.timezone

        client_links: dict[str, Any] | Unset = UNSET
        if not isinstance(self.client_links, Unset):
            client_links = self.client_links.to_dict()

        warnings: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = []
            for warnings_item_data in self.warnings:
                warnings_item = warnings_item_data.to_dict()
                warnings.append(warnings_item)

        push: dict[str, Any] | Unset = UNSET
        if not isinstance(self.push, Unset):
            push = self.push.to_dict()

        contact_email: None | str | Unset
        if isinstance(self.contact_email, Unset):
            contact_email = UNSET
        else:
            contact_email = self.contact_email

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "publicBaseUrl": public_base_url,
            }
        )
        if timezone is not UNSET:
            field_dict["timezone"] = timezone
        if client_links is not UNSET:
            field_dict["clientLinks"] = client_links
        if warnings is not UNSET:
            field_dict["warnings"] = warnings
        if push is not UNSET:
            field_dict["push"] = push
        if contact_email is not UNSET:
            field_dict["contactEmail"] = contact_email

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.client_links import ClientLinks
        from ..models.push_settings import PushSettings
        from ..models.settings_warning import SettingsWarning

        d = dict(src_dict)
        public_base_url = d.pop("publicBaseUrl")

        timezone = d.pop("timezone", UNSET)

        _client_links = d.pop("clientLinks", UNSET)
        client_links: ClientLinks | Unset
        if isinstance(_client_links, Unset):
            client_links = UNSET
        else:
            client_links = ClientLinks.from_dict(_client_links)

        _warnings = d.pop("warnings", UNSET)
        warnings: list[SettingsWarning] | Unset = UNSET
        if _warnings is not UNSET:
            warnings = []
            for warnings_item_data in _warnings:
                warnings_item = SettingsWarning.from_dict(warnings_item_data)

                warnings.append(warnings_item)

        _push = d.pop("push", UNSET)
        push: PushSettings | Unset
        if isinstance(_push, Unset):
            push = UNSET
        else:
            push = PushSettings.from_dict(_push)

        def _parse_contact_email(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        contact_email = _parse_contact_email(d.pop("contactEmail", UNSET))

        server_settings = cls(
            public_base_url=public_base_url,
            timezone=timezone,
            client_links=client_links,
            warnings=warnings,
            push=push,
            contact_email=contact_email,
        )

        server_settings.additional_properties = d
        return server_settings

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> Any:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: Any) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
