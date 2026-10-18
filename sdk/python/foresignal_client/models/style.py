from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.style_derive_type_1 import StyleDeriveType1
from ..models.style_derive_type_2_type_1 import StyleDeriveType2Type1
from ..models.style_derive_type_3_type_1 import StyleDeriveType3Type1
from ..models.style_type import StyleType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.style_category import StyleCategory
    from ..models.style_meaning import StyleMeaning
    from ..models.style_special import StyleSpecial
    from ..models.style_stop import StyleStop


T = TypeVar("T", bound="Style")


@_attrs_define
class Style:
    """Versioned colour style compiled to a ≤ 256-colour palette

    Attributes:
        id (str):  Example: nws-reflectivity.
        version (int):
        ref (str): Versioned reference `name@v` Example: nws-reflectivity@1.
        type_ (StyleType):
        current (bool | Unset): This is the current version of the style
        units (None | str | Unset):
        stops (list[StyleStop] | Unset):
        categories (list[StyleCategory] | Unset):
        specials (list[StyleSpecial] | Unset):
        meaning (list[StyleMeaning] | Unset):
        notice (None | str | Unset): Ramp origin and licence (viridis CC0, cmocean MIT, GEV MIT)
        derive (None | StyleDeriveType1 | StyleDeriveType2Type1 | StyleDeriveType3Type1 | Unset): `speed`: the style
            colors the vector magnitude √(u²+v²) of a frame's first two bands (051, `wind-speed`); a one-band frame's value
            is used as is
    """

    id: str
    version: int
    ref: str
    type_: StyleType
    current: bool | Unset = UNSET
    units: None | str | Unset = UNSET
    stops: list[StyleStop] | Unset = UNSET
    categories: list[StyleCategory] | Unset = UNSET
    specials: list[StyleSpecial] | Unset = UNSET
    meaning: list[StyleMeaning] | Unset = UNSET
    notice: None | str | Unset = UNSET
    derive: (
        None | StyleDeriveType1 | StyleDeriveType2Type1 | StyleDeriveType3Type1 | Unset
    ) = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        version = self.version

        ref = self.ref

        type_ = self.type_.value

        current = self.current

        units: None | str | Unset
        if isinstance(self.units, Unset):
            units = UNSET
        else:
            units = self.units

        stops: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.stops, Unset):
            stops = []
            for stops_item_data in self.stops:
                stops_item = stops_item_data.to_dict()
                stops.append(stops_item)

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

        notice: None | str | Unset
        if isinstance(self.notice, Unset):
            notice = UNSET
        else:
            notice = self.notice

        derive: None | str | Unset
        if isinstance(self.derive, Unset):
            derive = UNSET
        elif (
            isinstance(self.derive, StyleDeriveType1)
            or isinstance(self.derive, StyleDeriveType2Type1)
            or isinstance(self.derive, StyleDeriveType3Type1)
        ):
            derive = self.derive.value
        else:
            derive = self.derive

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "version": version,
                "ref": ref,
                "type": type_,
            }
        )
        if current is not UNSET:
            field_dict["current"] = current
        if units is not UNSET:
            field_dict["units"] = units
        if stops is not UNSET:
            field_dict["stops"] = stops
        if categories is not UNSET:
            field_dict["categories"] = categories
        if specials is not UNSET:
            field_dict["specials"] = specials
        if meaning is not UNSET:
            field_dict["meaning"] = meaning
        if notice is not UNSET:
            field_dict["notice"] = notice
        if derive is not UNSET:
            field_dict["derive"] = derive

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.style_category import StyleCategory
        from ..models.style_meaning import StyleMeaning
        from ..models.style_special import StyleSpecial
        from ..models.style_stop import StyleStop

        d = dict(src_dict)
        id = d.pop("id")

        version = d.pop("version")

        ref = d.pop("ref")

        type_ = StyleType(d.pop("type"))

        current = d.pop("current", UNSET)

        def _parse_units(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        units = _parse_units(d.pop("units", UNSET))

        _stops = d.pop("stops", UNSET)
        stops: list[StyleStop] | Unset = UNSET
        if _stops is not UNSET:
            stops = []
            for stops_item_data in _stops:
                stops_item = StyleStop.from_dict(stops_item_data)

                stops.append(stops_item)

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

        def _parse_notice(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        notice = _parse_notice(d.pop("notice", UNSET))

        def _parse_derive(
            data: object,
        ) -> (
            None
            | StyleDeriveType1
            | StyleDeriveType2Type1
            | StyleDeriveType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                derive_type_1 = StyleDeriveType1(data)

                return derive_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                derive_type_2_type_1 = StyleDeriveType2Type1(data)

                return derive_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                derive_type_3_type_1 = StyleDeriveType3Type1(data)

                return derive_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | StyleDeriveType1
                | StyleDeriveType2Type1
                | StyleDeriveType3Type1
                | Unset,
                data,
            )

        derive = _parse_derive(d.pop("derive", UNSET))

        style = cls(
            id=id,
            version=version,
            ref=ref,
            type_=type_,
            current=current,
            units=units,
            stops=stops,
            categories=categories,
            specials=specials,
            meaning=meaning,
            notice=notice,
            derive=derive,
        )

        style.additional_properties = d
        return style

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
