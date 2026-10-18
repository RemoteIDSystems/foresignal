from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.social_settings_track_record_overrides_item import (
        SocialSettingsTrackRecordOverridesItem,
    )


T = TypeVar("T", bound="SocialSettingsTrackRecord")


@_attrs_define
class SocialSettingsTrackRecord:
    """Admiralty source-reliability grading of accounts and channels

    Attributes:
        min_assessed (int | Unset):  Default: 10.
        grade_e_share (float | Unset):  Default: 0.7.
        grade_d_share (float | Unset):  Default: 0.85.
        overrides (list[SocialSettingsTrackRecordOverridesItem] | Unset):
    """

    min_assessed: int | Unset = 10
    grade_e_share: float | Unset = 0.7
    grade_d_share: float | Unset = 0.85
    overrides: list[SocialSettingsTrackRecordOverridesItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        min_assessed = self.min_assessed

        grade_e_share = self.grade_e_share

        grade_d_share = self.grade_d_share

        overrides: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.overrides, Unset):
            overrides = []
            for overrides_item_data in self.overrides:
                overrides_item = overrides_item_data.to_dict()
                overrides.append(overrides_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if min_assessed is not UNSET:
            field_dict["minAssessed"] = min_assessed
        if grade_e_share is not UNSET:
            field_dict["gradeEShare"] = grade_e_share
        if grade_d_share is not UNSET:
            field_dict["gradeDShare"] = grade_d_share
        if overrides is not UNSET:
            field_dict["overrides"] = overrides

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_settings_track_record_overrides_item import (
            SocialSettingsTrackRecordOverridesItem,
        )

        d = dict(src_dict)
        min_assessed = d.pop("minAssessed", UNSET)

        grade_e_share = d.pop("gradeEShare", UNSET)

        grade_d_share = d.pop("gradeDShare", UNSET)

        _overrides = d.pop("overrides", UNSET)
        overrides: list[SocialSettingsTrackRecordOverridesItem] | Unset = UNSET
        if _overrides is not UNSET:
            overrides = []
            for overrides_item_data in _overrides:
                overrides_item = SocialSettingsTrackRecordOverridesItem.from_dict(
                    overrides_item_data
                )

                overrides.append(overrides_item)

        social_settings_track_record = cls(
            min_assessed=min_assessed,
            grade_e_share=grade_e_share,
            grade_d_share=grade_d_share,
            overrides=overrides,
        )

        social_settings_track_record.additional_properties = d
        return social_settings_track_record

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
