from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_area import ChronicleArea


T = TypeVar("T", bound="ChroniclePatch")


@_attrs_define
class ChroniclePatch:
    """JSON merge patch for a chronicle (owner or editor)

    Attributes:
        name (str | Unset):
        description (None | str | Unset):
        visibility (Visibility | Unset):
        tlp (Tlp | Unset): Traffic Light Protocol 2.0 label
        area (ChronicleArea | None | Unset):
        retention (str | Unset): `forever` (default) or an ISO-8601 duration kept after stop Example: forever.
        size_cap_bytes (int | Unset):
        situation_ids (list[str] | Unset):
        hypothesis_set_ids (list[str] | Unset):
        investigation_ids (list[str] | Unset):
        topic_ids (list[str] | Unset):
    """

    name: str | Unset = UNSET
    description: None | str | Unset = UNSET
    visibility: Visibility | Unset = UNSET
    tlp: Tlp | Unset = UNSET
    area: ChronicleArea | None | Unset = UNSET
    retention: str | Unset = UNSET
    size_cap_bytes: int | Unset = UNSET
    situation_ids: list[str] | Unset = UNSET
    hypothesis_set_ids: list[str] | Unset = UNSET
    investigation_ids: list[str] | Unset = UNSET
    topic_ids: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.chronicle_area import ChronicleArea

        name = self.name

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        visibility: str | Unset = UNSET
        if not isinstance(self.visibility, Unset):
            visibility = self.visibility.value

        tlp: str | Unset = UNSET
        if not isinstance(self.tlp, Unset):
            tlp = self.tlp.value

        area: dict[str, Any] | None | Unset
        if isinstance(self.area, Unset):
            area = UNSET
        elif isinstance(self.area, ChronicleArea):
            area = self.area.to_dict()
        else:
            area = self.area

        retention = self.retention

        size_cap_bytes = self.size_cap_bytes

        situation_ids: list[str] | Unset = UNSET
        if not isinstance(self.situation_ids, Unset):
            situation_ids = self.situation_ids

        hypothesis_set_ids: list[str] | Unset = UNSET
        if not isinstance(self.hypothesis_set_ids, Unset):
            hypothesis_set_ids = self.hypothesis_set_ids

        investigation_ids: list[str] | Unset = UNSET
        if not isinstance(self.investigation_ids, Unset):
            investigation_ids = self.investigation_ids

        topic_ids: list[str] | Unset = UNSET
        if not isinstance(self.topic_ids, Unset):
            topic_ids = self.topic_ids

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if name is not UNSET:
            field_dict["name"] = name
        if description is not UNSET:
            field_dict["description"] = description
        if visibility is not UNSET:
            field_dict["visibility"] = visibility
        if tlp is not UNSET:
            field_dict["tlp"] = tlp
        if area is not UNSET:
            field_dict["area"] = area
        if retention is not UNSET:
            field_dict["retention"] = retention
        if size_cap_bytes is not UNSET:
            field_dict["sizeCapBytes"] = size_cap_bytes
        if situation_ids is not UNSET:
            field_dict["situationIds"] = situation_ids
        if hypothesis_set_ids is not UNSET:
            field_dict["hypothesisSetIds"] = hypothesis_set_ids
        if investigation_ids is not UNSET:
            field_dict["investigationIds"] = investigation_ids
        if topic_ids is not UNSET:
            field_dict["topicIds"] = topic_ids

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_area import ChronicleArea

        d = dict(src_dict)
        name = d.pop("name", UNSET)

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        _visibility = d.pop("visibility", UNSET)
        visibility: Visibility | Unset
        if isinstance(_visibility, Unset):
            visibility = UNSET
        else:
            visibility = Visibility(_visibility)

        _tlp = d.pop("tlp", UNSET)
        tlp: Tlp | Unset
        if isinstance(_tlp, Unset):
            tlp = UNSET
        else:
            tlp = Tlp(_tlp)

        def _parse_area(data: object) -> ChronicleArea | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                area_type_0 = ChronicleArea.from_dict(data)

                return area_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ChronicleArea | None | Unset, data)

        area = _parse_area(d.pop("area", UNSET))

        retention = d.pop("retention", UNSET)

        size_cap_bytes = d.pop("sizeCapBytes", UNSET)

        situation_ids = cast(list[str], d.pop("situationIds", UNSET))

        hypothesis_set_ids = cast(list[str], d.pop("hypothesisSetIds", UNSET))

        investigation_ids = cast(list[str], d.pop("investigationIds", UNSET))

        topic_ids = cast(list[str], d.pop("topicIds", UNSET))

        chronicle_patch = cls(
            name=name,
            description=description,
            visibility=visibility,
            tlp=tlp,
            area=area,
            retention=retention,
            size_cap_bytes=size_cap_bytes,
            situation_ids=situation_ids,
            hypothesis_set_ids=hypothesis_set_ids,
            investigation_ids=investigation_ids,
            topic_ids=topic_ids,
        )

        chronicle_patch.additional_properties = d
        return chronicle_patch

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
