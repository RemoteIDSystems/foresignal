from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.notification_action_action import NotificationActionAction
from ..models.notification_action_method import NotificationActionMethod
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.notification_action_params import NotificationActionParams


T = TypeVar("T", bound="NotificationAction")


@_attrs_define
class NotificationAction:
    """A capability URL minted for the calling device (033); single use, 12 h

    Attributes:
        action (NotificationActionAction):
        url (str): <publicBaseUrl>/v1/actions/eac_…
        expires_at (datetime.datetime):
        label (str | Unset):
        method (NotificationActionMethod | Unset):
        confirm_url (str | Unset): Same URL; GET shows the confirm page
        params (NotificationActionParams | Unset): Action parameters
    """

    action: NotificationActionAction
    url: str
    expires_at: datetime.datetime
    label: str | Unset = UNSET
    method: NotificationActionMethod | Unset = UNSET
    confirm_url: str | Unset = UNSET
    params: NotificationActionParams | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        action = self.action.value

        url = self.url

        expires_at = self.expires_at.isoformat()

        label = self.label

        method: str | Unset = UNSET
        if not isinstance(self.method, Unset):
            method = self.method.value

        confirm_url = self.confirm_url

        params: dict[str, Any] | Unset = UNSET
        if not isinstance(self.params, Unset):
            params = self.params.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "action": action,
                "url": url,
                "expiresAt": expires_at,
            }
        )
        if label is not UNSET:
            field_dict["label"] = label
        if method is not UNSET:
            field_dict["method"] = method
        if confirm_url is not UNSET:
            field_dict["confirmUrl"] = confirm_url
        if params is not UNSET:
            field_dict["params"] = params

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.notification_action_params import NotificationActionParams

        d = dict(src_dict)
        action = NotificationActionAction(d.pop("action"))

        url = d.pop("url")

        expires_at = datetime.datetime.fromisoformat(d.pop("expiresAt"))

        label = d.pop("label", UNSET)

        _method = d.pop("method", UNSET)
        method: NotificationActionMethod | Unset
        if isinstance(_method, Unset):
            method = UNSET
        else:
            method = NotificationActionMethod(_method)

        confirm_url = d.pop("confirmUrl", UNSET)

        _params = d.pop("params", UNSET)
        params: NotificationActionParams | Unset
        if isinstance(_params, Unset):
            params = UNSET
        else:
            params = NotificationActionParams.from_dict(_params)

        notification_action = cls(
            action=action,
            url=url,
            expires_at=expires_at,
            label=label,
            method=method,
            confirm_url=confirm_url,
            params=params,
        )

        notification_action.additional_properties = d
        return notification_action

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
