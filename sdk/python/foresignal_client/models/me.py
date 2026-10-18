from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.me_scopes_item import MeScopesItem
from ..models.tlp import Tlp
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.me_groups_item import MeGroupsItem
    from ..models.me_token import MeToken
    from ..models.notification_preference import NotificationPreference
    from ..models.principal import Principal


T = TypeVar("T", bound="Me")


@_attrs_define
class Me:
    """The calling principal with its token, effective scopes, groups and notification preferences

    Attributes:
        principal (Principal): A person, agent or service that holds scoped tokens; no passwords are stored
        token (MeToken): The token used for this request
        scopes (list[MeScopesItem]): Scopes the token may call (scope hierarchy expanded)
        groups (list[MeGroupsItem]):
        notification_preferences (list[NotificationPreference]):
        default_tlp (Tlp | Unset): Traffic Light Protocol 2.0 label
    """

    principal: Principal
    token: MeToken
    scopes: list[MeScopesItem]
    groups: list[MeGroupsItem]
    notification_preferences: list[NotificationPreference]
    default_tlp: Tlp | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        principal = self.principal.to_dict()

        token = self.token.to_dict()

        scopes = []
        for scopes_item_data in self.scopes:
            scopes_item = scopes_item_data.value
            scopes.append(scopes_item)

        groups = []
        for groups_item_data in self.groups:
            groups_item = groups_item_data.to_dict()
            groups.append(groups_item)

        notification_preferences = []
        for notification_preferences_item_data in self.notification_preferences:
            notification_preferences_item = notification_preferences_item_data.to_dict()
            notification_preferences.append(notification_preferences_item)

        default_tlp: str | Unset = UNSET
        if not isinstance(self.default_tlp, Unset):
            default_tlp = self.default_tlp.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "principal": principal,
                "token": token,
                "scopes": scopes,
                "groups": groups,
                "notificationPreferences": notification_preferences,
            }
        )
        if default_tlp is not UNSET:
            field_dict["defaultTlp"] = default_tlp

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.me_groups_item import MeGroupsItem
        from ..models.me_token import MeToken
        from ..models.notification_preference import NotificationPreference
        from ..models.principal import Principal

        d = dict(src_dict)
        principal = Principal.from_dict(d.pop("principal"))

        token = MeToken.from_dict(d.pop("token"))

        scopes = []
        _scopes = d.pop("scopes")
        for scopes_item_data in _scopes:
            scopes_item = MeScopesItem(scopes_item_data)

            scopes.append(scopes_item)

        groups = []
        _groups = d.pop("groups")
        for groups_item_data in _groups:
            groups_item = MeGroupsItem.from_dict(groups_item_data)

            groups.append(groups_item)

        notification_preferences = []
        _notification_preferences = d.pop("notificationPreferences")
        for notification_preferences_item_data in _notification_preferences:
            notification_preferences_item = NotificationPreference.from_dict(
                notification_preferences_item_data
            )

            notification_preferences.append(notification_preferences_item)

        _default_tlp = d.pop("defaultTlp", UNSET)
        default_tlp: Tlp | Unset
        if isinstance(_default_tlp, Unset):
            default_tlp = UNSET
        else:
            default_tlp = Tlp(_default_tlp)

        me = cls(
            principal=principal,
            token=token,
            scopes=scopes,
            groups=groups,
            notification_preferences=notification_preferences,
            default_tlp=default_tlp,
        )

        me.additional_properties = d
        return me

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
