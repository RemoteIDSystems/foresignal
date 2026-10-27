from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="FirstRunStatus")


@_attrs_define
class FirstRunStatus:
    """Whether the instance still needs first-run setup (C4/190)

    Attributes:
        first_run (bool): True when no users exist yet
        has_users (bool):
        smtp_configured (bool | Unset):
        sources_enabled (int | Unset): How many sources are in the collection set
        api_version (str | Unset):
    """

    first_run: bool
    has_users: bool
    smtp_configured: bool | Unset = UNSET
    sources_enabled: int | Unset = UNSET
    api_version: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        first_run = self.first_run

        has_users = self.has_users

        smtp_configured = self.smtp_configured

        sources_enabled = self.sources_enabled

        api_version = self.api_version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "firstRun": first_run,
                "hasUsers": has_users,
            }
        )
        if smtp_configured is not UNSET:
            field_dict["smtpConfigured"] = smtp_configured
        if sources_enabled is not UNSET:
            field_dict["sourcesEnabled"] = sources_enabled
        if api_version is not UNSET:
            field_dict["apiVersion"] = api_version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        first_run = d.pop("firstRun")

        has_users = d.pop("hasUsers")

        smtp_configured = d.pop("smtpConfigured", UNSET)

        sources_enabled = d.pop("sourcesEnabled", UNSET)

        api_version = d.pop("apiVersion", UNSET)

        first_run_status = cls(
            first_run=first_run,
            has_users=has_users,
            smtp_configured=smtp_configured,
            sources_enabled=sources_enabled,
            api_version=api_version,
        )

        first_run_status.additional_properties = d
        return first_run_status

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
