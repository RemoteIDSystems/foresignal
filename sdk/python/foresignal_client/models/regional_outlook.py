from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.regional_outlook_region_level import RegionalOutlookRegionLevel
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.outlook_window import OutlookWindow
    from ..models.regional_outlook_skill_type_0 import RegionalOutlookSkillType0


T = TypeVar("T", bound="RegionalOutlook")


@_attrs_define
class RegionalOutlook:
    """CAST-style count outlook for one region and family: six 4-week windows with accuracy

    Attributes:
        region (str):
        region_level (RegionalOutlookRegionLevel):
        family (str): Outlook family from `intel/outlook-families.json` Example: conflict:battles.
        published_at (datetime.datetime):
        model_version (str):  Example: regional-outlook 1.0.0.
        windows (list[OutlookWindow]):
        category (str | Unset):  Example: conflict.
        as_of_seq (int | Unset):
        manifest_id (None | str | Unset):
        history (list[OutlookWindow] | Unset): Past windows with actuals (detail endpoint only)
        skill (None | RegionalOutlookSkillType0 | Unset):
        low_reporting_density (bool | Unset):
        low_detectability (bool | Unset):
        caveats (list[Caveat] | Unset):
        record_ref (None | str | Unset):
        id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        source (None | str | Unset): The reference source the counts came from (acled, ucdp, usgs, gdacs or foresignal)
    """

    region: str
    region_level: RegionalOutlookRegionLevel
    family: str
    published_at: datetime.datetime
    model_version: str
    windows: list[OutlookWindow]
    category: str | Unset = UNSET
    as_of_seq: int | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    history: list[OutlookWindow] | Unset = UNSET
    skill: None | RegionalOutlookSkillType0 | Unset = UNSET
    low_reporting_density: bool | Unset = UNSET
    low_detectability: bool | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    record_ref: None | str | Unset = UNSET
    id: str | Unset = UNSET
    source: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.regional_outlook_skill_type_0 import RegionalOutlookSkillType0

        region = self.region

        region_level = self.region_level.value

        family = self.family

        published_at = self.published_at.isoformat()

        model_version = self.model_version

        windows = []
        for windows_item_data in self.windows:
            windows_item = windows_item_data.to_dict()
            windows.append(windows_item)

        category = self.category

        as_of_seq = self.as_of_seq

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        history: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.history, Unset):
            history = []
            for history_item_data in self.history:
                history_item = history_item_data.to_dict()
                history.append(history_item)

        skill: dict[str, Any] | None | Unset
        if isinstance(self.skill, Unset):
            skill = UNSET
        elif isinstance(self.skill, RegionalOutlookSkillType0):
            skill = self.skill.to_dict()
        else:
            skill = self.skill

        low_reporting_density = self.low_reporting_density

        low_detectability = self.low_detectability

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        record_ref: None | str | Unset
        if isinstance(self.record_ref, Unset):
            record_ref = UNSET
        else:
            record_ref = self.record_ref

        id = self.id

        source: None | str | Unset
        if isinstance(self.source, Unset):
            source = UNSET
        else:
            source = self.source

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "region": region,
                "regionLevel": region_level,
                "family": family,
                "publishedAt": published_at,
                "modelVersion": model_version,
                "windows": windows,
            }
        )
        if category is not UNSET:
            field_dict["category"] = category
        if as_of_seq is not UNSET:
            field_dict["asOfSeq"] = as_of_seq
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id
        if history is not UNSET:
            field_dict["history"] = history
        if skill is not UNSET:
            field_dict["skill"] = skill
        if low_reporting_density is not UNSET:
            field_dict["lowReportingDensity"] = low_reporting_density
        if low_detectability is not UNSET:
            field_dict["lowDetectability"] = low_detectability
        if caveats is not UNSET:
            field_dict["caveats"] = caveats
        if record_ref is not UNSET:
            field_dict["recordRef"] = record_ref
        if id is not UNSET:
            field_dict["id"] = id
        if source is not UNSET:
            field_dict["source"] = source

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.outlook_window import OutlookWindow
        from ..models.regional_outlook_skill_type_0 import RegionalOutlookSkillType0

        d = dict(src_dict)
        region = d.pop("region")

        region_level = RegionalOutlookRegionLevel(d.pop("regionLevel"))

        family = d.pop("family")

        published_at = datetime.datetime.fromisoformat(d.pop("publishedAt"))

        model_version = d.pop("modelVersion")

        windows = []
        _windows = d.pop("windows")
        for windows_item_data in _windows:
            windows_item = OutlookWindow.from_dict(windows_item_data)

            windows.append(windows_item)

        category = d.pop("category", UNSET)

        as_of_seq = d.pop("asOfSeq", UNSET)

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        _history = d.pop("history", UNSET)
        history: list[OutlookWindow] | Unset = UNSET
        if _history is not UNSET:
            history = []
            for history_item_data in _history:
                history_item = OutlookWindow.from_dict(history_item_data)

                history.append(history_item)

        def _parse_skill(data: object) -> None | RegionalOutlookSkillType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                skill_type_0 = RegionalOutlookSkillType0.from_dict(data)

                return skill_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RegionalOutlookSkillType0 | Unset, data)

        skill = _parse_skill(d.pop("skill", UNSET))

        low_reporting_density = d.pop("lowReportingDensity", UNSET)

        low_detectability = d.pop("lowDetectability", UNSET)

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        def _parse_record_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        record_ref = _parse_record_ref(d.pop("recordRef", UNSET))

        id = d.pop("id", UNSET)

        def _parse_source(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        source = _parse_source(d.pop("source", UNSET))

        regional_outlook = cls(
            region=region,
            region_level=region_level,
            family=family,
            published_at=published_at,
            model_version=model_version,
            windows=windows,
            category=category,
            as_of_seq=as_of_seq,
            manifest_id=manifest_id,
            history=history,
            skill=skill,
            low_reporting_density=low_reporting_density,
            low_detectability=low_detectability,
            caveats=caveats,
            record_ref=record_ref,
            id=id,
            source=source,
        )

        regional_outlook.additional_properties = d
        return regional_outlook

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
