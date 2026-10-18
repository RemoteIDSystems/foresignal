from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.saved_view_input_time_field import SavedViewInputTimeField
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.condition import Condition
    from ..models.geometry import Geometry


T = TypeVar("T", bound="SavedViewInput")


@_attrs_define
class SavedViewInput:
    """Create or replace a saved view (036)

    Attributes:
        slug (str):
        name (str):
        sources (list[str] | Unset):
        kinds (list[str] | Unset):
        place_ids (list[str] | Unset):
        bbox (list[float] | None | Unset): [minLon, minLat, maxLon, maxLat] (WGS84)
        area (Geometry | None | Unset):
        conditions (list[Condition] | Unset): 017 grammar including in/notIn/exists/matches/startsWith and
            inWatchlist/notInWatchlist
        time_field (SavedViewInputTimeField | Unset):  Default: SavedViewInputTimeField.EFFECTIVE.
        include_expired (bool | Unset):  Default: False.
        fields (list[str] | Unset): Projection whitelist of top-level-rooted JSON paths
    """

    slug: str
    name: str
    sources: list[str] | Unset = UNSET
    kinds: list[str] | Unset = UNSET
    place_ids: list[str] | Unset = UNSET
    bbox: list[float] | None | Unset = UNSET
    area: Geometry | None | Unset = UNSET
    conditions: list[Condition] | Unset = UNSET
    time_field: SavedViewInputTimeField | Unset = SavedViewInputTimeField.EFFECTIVE
    include_expired: bool | Unset = False
    fields: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        slug = self.slug

        name = self.name

        sources: list[str] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources

        kinds: list[str] | Unset = UNSET
        if not isinstance(self.kinds, Unset):
            kinds = self.kinds

        place_ids: list[str] | Unset = UNSET
        if not isinstance(self.place_ids, Unset):
            place_ids = self.place_ids

        bbox: list[float] | None | Unset
        if isinstance(self.bbox, Unset):
            bbox = UNSET
        elif isinstance(self.bbox, list):
            bbox = self.bbox

        else:
            bbox = self.bbox

        area: dict[str, Any] | None | Unset
        if isinstance(self.area, Unset):
            area = UNSET
        elif isinstance(self.area, Geometry):
            area = self.area.to_dict()
        else:
            area = self.area

        conditions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.conditions, Unset):
            conditions = []
            for conditions_item_data in self.conditions:
                conditions_item = conditions_item_data.to_dict()
                conditions.append(conditions_item)

        time_field: str | Unset = UNSET
        if not isinstance(self.time_field, Unset):
            time_field = self.time_field.value

        include_expired = self.include_expired

        fields: list[str] | Unset = UNSET
        if not isinstance(self.fields, Unset):
            fields = self.fields

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "slug": slug,
                "name": name,
            }
        )
        if sources is not UNSET:
            field_dict["sources"] = sources
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if place_ids is not UNSET:
            field_dict["placeIds"] = place_ids
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if area is not UNSET:
            field_dict["area"] = area
        if conditions is not UNSET:
            field_dict["conditions"] = conditions
        if time_field is not UNSET:
            field_dict["timeField"] = time_field
        if include_expired is not UNSET:
            field_dict["includeExpired"] = include_expired
        if fields is not UNSET:
            field_dict["fields"] = fields

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.condition import Condition
        from ..models.geometry import Geometry

        d = dict(src_dict)
        slug = d.pop("slug")

        name = d.pop("name")

        sources = cast(list[str], d.pop("sources", UNSET))

        kinds = cast(list[str], d.pop("kinds", UNSET))

        place_ids = cast(list[str], d.pop("placeIds", UNSET))

        def _parse_bbox(data: object) -> list[float] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                bbox_type_0 = cast(list[float], data)

                return bbox_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[float] | None | Unset, data)

        bbox = _parse_bbox(d.pop("bbox", UNSET))

        def _parse_area(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                area_type_0 = Geometry.from_dict(data)

                return area_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        area = _parse_area(d.pop("area", UNSET))

        _conditions = d.pop("conditions", UNSET)
        conditions: list[Condition] | Unset = UNSET
        if _conditions is not UNSET:
            conditions = []
            for conditions_item_data in _conditions:
                conditions_item = Condition.from_dict(conditions_item_data)

                conditions.append(conditions_item)

        _time_field = d.pop("timeField", UNSET)
        time_field: SavedViewInputTimeField | Unset
        if isinstance(_time_field, Unset):
            time_field = UNSET
        else:
            time_field = SavedViewInputTimeField(_time_field)

        include_expired = d.pop("includeExpired", UNSET)

        fields = cast(list[str], d.pop("fields", UNSET))

        saved_view_input = cls(
            slug=slug,
            name=name,
            sources=sources,
            kinds=kinds,
            place_ids=place_ids,
            bbox=bbox,
            area=area,
            conditions=conditions,
            time_field=time_field,
            include_expired=include_expired,
            fields=fields,
        )

        saved_view_input.additional_properties = d
        return saved_view_input

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
