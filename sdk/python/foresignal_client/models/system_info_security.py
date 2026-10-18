from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.settings_warning import SettingsWarning


T = TypeVar("T", bound="SystemInfoSecurity")


@_attrs_define
class SystemInfoSecurity:
    """Security posture (023, 035)

    Attributes:
        open_reads (bool | Unset):
        anonymous_restricted (bool | Unset): True when the anonymous profile restricts anything beyond its defaults
        warnings (list[SettingsWarning] | Unset): PUBLIC_BASE_URL_INSECURE, PUBLIC_BASE_URL_MISMATCH (023),
            ANONYMOUS_UNRESTRICTED (035)
    """

    open_reads: bool | Unset = UNSET
    anonymous_restricted: bool | Unset = UNSET
    warnings: list[SettingsWarning] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        open_reads = self.open_reads

        anonymous_restricted = self.anonymous_restricted

        warnings: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = []
            for warnings_item_data in self.warnings:
                warnings_item = warnings_item_data.to_dict()
                warnings.append(warnings_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if open_reads is not UNSET:
            field_dict["openReads"] = open_reads
        if anonymous_restricted is not UNSET:
            field_dict["anonymousRestricted"] = anonymous_restricted
        if warnings is not UNSET:
            field_dict["warnings"] = warnings

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.settings_warning import SettingsWarning

        d = dict(src_dict)
        open_reads = d.pop("openReads", UNSET)

        anonymous_restricted = d.pop("anonymousRestricted", UNSET)

        _warnings = d.pop("warnings", UNSET)
        warnings: list[SettingsWarning] | Unset = UNSET
        if _warnings is not UNSET:
            warnings = []
            for warnings_item_data in _warnings:
                warnings_item = SettingsWarning.from_dict(warnings_item_data)

                warnings.append(warnings_item)

        system_info_security = cls(
            open_reads=open_reads,
            anonymous_restricted=anonymous_restricted,
            warnings=warnings,
        )

        system_info_security.additional_properties = d
        return system_info_security

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
