from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.legend_type import LegendType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.legend_tick import LegendTick
    from ..models.style_category import StyleCategory
    from ..models.style_meaning import StyleMeaning
    from ..models.style_special import StyleSpecial
    from ..models.style_stop import StyleStop


T = TypeVar("T", bound="Legend")


@_attrs_define
class Legend:
    """JSON legend of a product in a style and unit system

    Attributes:
        style_id (str):
        style_version (int):
        type_ (LegendType):
        product_id (str | Unset):
        units (None | str | Unset):
        units_label (None | str | Unset):  Example: dBZ.
        stops (list[StyleStop] | Unset):
        ticks (list[LegendTick] | Unset):
        categories (list[StyleCategory] | Unset):
        specials (list[StyleSpecial] | Unset):
        meaning (list[StyleMeaning] | Unset):
    """

    style_id: str
    style_version: int
    type_: LegendType
    product_id: str | Unset = UNSET
    units: None | str | Unset = UNSET
    units_label: None | str | Unset = UNSET
    stops: list[StyleStop] | Unset = UNSET
    ticks: list[LegendTick] | Unset = UNSET
    categories: list[StyleCategory] | Unset = UNSET
    specials: list[StyleSpecial] | Unset = UNSET
    meaning: list[StyleMeaning] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        style_id = self.style_id

        style_version = self.style_version

        type_ = self.type_.value

        product_id = self.product_id

        units: None | str | Unset
        if isinstance(self.units, Unset):
            units = UNSET
        else:
            units = self.units

        units_label: None | str | Unset
        if isinstance(self.units_label, Unset):
            units_label = UNSET
        else:
            units_label = self.units_label

        stops: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.stops, Unset):
            stops = []
            for stops_item_data in self.stops:
                stops_item = stops_item_data.to_dict()
                stops.append(stops_item)

        ticks: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.ticks, Unset):
            ticks = []
            for ticks_item_data in self.ticks:
                ticks_item = ticks_item_data.to_dict()
                ticks.append(ticks_item)

        categories: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.categories, Unset):
            categories = []
            for categories_item_data in self.categories:
                categories_item = categories_item_data.to_dict()
                categories.append(categories_item)

        specials: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.specials, Unset):
            specials = []
            for specials_item_data in self.specials:
                specials_item = specials_item_data.to_dict()
                specials.append(specials_item)

        meaning: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.meaning, Unset):
            meaning = []
            for meaning_item_data in self.meaning:
                meaning_item = meaning_item_data.to_dict()
                meaning.append(meaning_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "styleId": style_id,
                "styleVersion": style_version,
                "type": type_,
            }
        )
        if product_id is not UNSET:
            field_dict["productId"] = product_id
        if units is not UNSET:
            field_dict["units"] = units
        if units_label is not UNSET:
            field_dict["unitsLabel"] = units_label
        if stops is not UNSET:
            field_dict["stops"] = stops
        if ticks is not UNSET:
            field_dict["ticks"] = ticks
        if categories is not UNSET:
            field_dict["categories"] = categories
        if specials is not UNSET:
            field_dict["specials"] = specials
        if meaning is not UNSET:
            field_dict["meaning"] = meaning

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.legend_tick import LegendTick
        from ..models.style_category import StyleCategory
        from ..models.style_meaning import StyleMeaning
        from ..models.style_special import StyleSpecial
        from ..models.style_stop import StyleStop

        d = dict(src_dict)
        style_id = d.pop("styleId")

        style_version = d.pop("styleVersion")

        type_ = LegendType(d.pop("type"))

        product_id = d.pop("productId", UNSET)

        def _parse_units(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        units = _parse_units(d.pop("units", UNSET))

        def _parse_units_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        units_label = _parse_units_label(d.pop("unitsLabel", UNSET))

        _stops = d.pop("stops", UNSET)
        stops: list[StyleStop] | Unset = UNSET
        if _stops is not UNSET:
            stops = []
            for stops_item_data in _stops:
                stops_item = StyleStop.from_dict(stops_item_data)

                stops.append(stops_item)

        _ticks = d.pop("ticks", UNSET)
        ticks: list[LegendTick] | Unset = UNSET
        if _ticks is not UNSET:
            ticks = []
            for ticks_item_data in _ticks:
                ticks_item = LegendTick.from_dict(ticks_item_data)

                ticks.append(ticks_item)

        _categories = d.pop("categories", UNSET)
        categories: list[StyleCategory] | Unset = UNSET
        if _categories is not UNSET:
            categories = []
            for categories_item_data in _categories:
                categories_item = StyleCategory.from_dict(categories_item_data)

                categories.append(categories_item)

        _specials = d.pop("specials", UNSET)
        specials: list[StyleSpecial] | Unset = UNSET
        if _specials is not UNSET:
            specials = []
            for specials_item_data in _specials:
                specials_item = StyleSpecial.from_dict(specials_item_data)

                specials.append(specials_item)

        _meaning = d.pop("meaning", UNSET)
        meaning: list[StyleMeaning] | Unset = UNSET
        if _meaning is not UNSET:
            meaning = []
            for meaning_item_data in _meaning:
                meaning_item = StyleMeaning.from_dict(meaning_item_data)

                meaning.append(meaning_item)

        legend = cls(
            style_id=style_id,
            style_version=style_version,
            type_=type_,
            product_id=product_id,
            units=units,
            units_label=units_label,
            stops=stops,
            ticks=ticks,
            categories=categories,
            specials=specials,
            meaning=meaning,
        )

        legend.additional_properties = d
        return legend

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
