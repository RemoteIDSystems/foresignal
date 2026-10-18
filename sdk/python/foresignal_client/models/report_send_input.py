from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ReportSendInput")


@_attrs_define
class ReportSendInput:
    """Send a report to principals and groups on this instance

    Attributes:
        principals (list[str] | Unset): Principal ids or handles
        groups (list[str] | Unset):
        channels (list[str] | Unset): Recipients' own channel ids (130 preferences) for HTML or link delivery
        message (None | str | Unset):
        revision (int | None | Unset): Default: latest published revision
    """

    principals: list[str] | Unset = UNSET
    groups: list[str] | Unset = UNSET
    channels: list[str] | Unset = UNSET
    message: None | str | Unset = UNSET
    revision: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        principals: list[str] | Unset = UNSET
        if not isinstance(self.principals, Unset):
            principals = self.principals

        groups: list[str] | Unset = UNSET
        if not isinstance(self.groups, Unset):
            groups = self.groups

        channels: list[str] | Unset = UNSET
        if not isinstance(self.channels, Unset):
            channels = self.channels

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        revision: int | None | Unset
        if isinstance(self.revision, Unset):
            revision = UNSET
        else:
            revision = self.revision

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if principals is not UNSET:
            field_dict["principals"] = principals
        if groups is not UNSET:
            field_dict["groups"] = groups
        if channels is not UNSET:
            field_dict["channels"] = channels
        if message is not UNSET:
            field_dict["message"] = message
        if revision is not UNSET:
            field_dict["revision"] = revision

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        principals = cast(list[str], d.pop("principals", UNSET))

        groups = cast(list[str], d.pop("groups", UNSET))

        channels = cast(list[str], d.pop("channels", UNSET))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        def _parse_revision(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        revision = _parse_revision(d.pop("revision", UNSET))

        report_send_input = cls(
            principals=principals,
            groups=groups,
            channels=channels,
            message=message,
            revision=revision,
        )

        report_send_input.additional_properties = d
        return report_send_input

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
