from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.infra_build import InfraBuild
    from ..models.infra_status_nodes_by_type import InfraStatusNodesByType


T = TypeVar("T", bound="InfraStatus")


@_attrs_define
class InfraStatus:
    """Graph status: the last build and counts

    Attributes:
        built (bool):
        building (bool):
        last_build (InfraBuild | None | Unset):
        nodes_by_type (InfraStatusNodesByType | Unset):
        osm_extract_date (datetime.datetime | None | Unset):
    """

    built: bool
    building: bool
    last_build: InfraBuild | None | Unset = UNSET
    nodes_by_type: InfraStatusNodesByType | Unset = UNSET
    osm_extract_date: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.infra_build import InfraBuild

        built = self.built

        building = self.building

        last_build: dict[str, Any] | None | Unset
        if isinstance(self.last_build, Unset):
            last_build = UNSET
        elif isinstance(self.last_build, InfraBuild):
            last_build = self.last_build.to_dict()
        else:
            last_build = self.last_build

        nodes_by_type: dict[str, Any] | Unset = UNSET
        if not isinstance(self.nodes_by_type, Unset):
            nodes_by_type = self.nodes_by_type.to_dict()

        osm_extract_date: None | str | Unset
        if isinstance(self.osm_extract_date, Unset):
            osm_extract_date = UNSET
        elif isinstance(self.osm_extract_date, datetime.datetime):
            osm_extract_date = self.osm_extract_date.isoformat()
        else:
            osm_extract_date = self.osm_extract_date

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "built": built,
                "building": building,
            }
        )
        if last_build is not UNSET:
            field_dict["lastBuild"] = last_build
        if nodes_by_type is not UNSET:
            field_dict["nodesByType"] = nodes_by_type
        if osm_extract_date is not UNSET:
            field_dict["osmExtractDate"] = osm_extract_date

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.infra_build import InfraBuild
        from ..models.infra_status_nodes_by_type import InfraStatusNodesByType

        d = dict(src_dict)
        built = d.pop("built")

        building = d.pop("building")

        def _parse_last_build(data: object) -> InfraBuild | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                last_build_type_0 = InfraBuild.from_dict(data)

                return last_build_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(InfraBuild | None | Unset, data)

        last_build = _parse_last_build(d.pop("lastBuild", UNSET))

        _nodes_by_type = d.pop("nodesByType", UNSET)
        nodes_by_type: InfraStatusNodesByType | Unset
        if isinstance(_nodes_by_type, Unset):
            nodes_by_type = UNSET
        else:
            nodes_by_type = InfraStatusNodesByType.from_dict(_nodes_by_type)

        def _parse_osm_extract_date(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                osm_extract_date_type_0 = datetime.datetime.fromisoformat(data)

                return osm_extract_date_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        osm_extract_date = _parse_osm_extract_date(d.pop("osmExtractDate", UNSET))

        infra_status = cls(
            built=built,
            building=building,
            last_build=last_build,
            nodes_by_type=nodes_by_type,
            osm_extract_date=osm_extract_date,
        )

        infra_status.additional_properties = d
        return infra_status

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
