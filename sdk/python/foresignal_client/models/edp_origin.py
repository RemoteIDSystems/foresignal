from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.edp_origin_class_type_1 import EdpOriginClassType1
from ..models.edp_origin_class_type_2_type_1 import EdpOriginClassType2Type1
from ..models.edp_origin_class_type_3_type_1 import EdpOriginClassType3Type1
from ..types import UNSET, Unset

T = TypeVar("T", bound="EdpOrigin")


@_attrs_define
class EdpOrigin:
    """Item-level provenance: who originally observed or published this item, when it differs from the manifest originator
    (for evidence independence)

        Attributes:
            originator (str):
            class_ (EdpOriginClassType1 | EdpOriginClassType2Type1 | EdpOriginClassType3Type1 | None | Unset):
            upstream_id (None | str | Unset): The original publisher's id for the observation or report
            derived_from (list[str] | Unset): Other items (stream:id) or upstream ids this item was derived from
    """

    originator: str
    class_: (
        EdpOriginClassType1
        | EdpOriginClassType2Type1
        | EdpOriginClassType3Type1
        | None
        | Unset
    ) = UNSET
    upstream_id: None | str | Unset = UNSET
    derived_from: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        originator = self.originator

        class_: None | str | Unset
        if isinstance(self.class_, Unset):
            class_ = UNSET
        elif (
            isinstance(self.class_, EdpOriginClassType1)
            or isinstance(self.class_, EdpOriginClassType2Type1)
            or isinstance(self.class_, EdpOriginClassType3Type1)
        ):
            class_ = self.class_.value
        else:
            class_ = self.class_

        upstream_id: None | str | Unset
        if isinstance(self.upstream_id, Unset):
            upstream_id = UNSET
        else:
            upstream_id = self.upstream_id

        derived_from: list[str] | Unset = UNSET
        if not isinstance(self.derived_from, Unset):
            derived_from = self.derived_from

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "originator": originator,
            }
        )
        if class_ is not UNSET:
            field_dict["class"] = class_
        if upstream_id is not UNSET:
            field_dict["upstreamId"] = upstream_id
        if derived_from is not UNSET:
            field_dict["derivedFrom"] = derived_from

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        originator = d.pop("originator")

        def _parse_class_(
            data: object,
        ) -> (
            EdpOriginClassType1
            | EdpOriginClassType2Type1
            | EdpOriginClassType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                class_type_1 = EdpOriginClassType1(data)

                return class_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                class_type_2_type_1 = EdpOriginClassType2Type1(data)

                return class_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                class_type_3_type_1 = EdpOriginClassType3Type1(data)

                return class_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                EdpOriginClassType1
                | EdpOriginClassType2Type1
                | EdpOriginClassType3Type1
                | None
                | Unset,
                data,
            )

        class_ = _parse_class_(d.pop("class", UNSET))

        def _parse_upstream_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        upstream_id = _parse_upstream_id(d.pop("upstreamId", UNSET))

        derived_from = cast(list[str], d.pop("derivedFrom", UNSET))

        edp_origin = cls(
            originator=originator,
            class_=class_,
            upstream_id=upstream_id,
            derived_from=derived_from,
        )

        edp_origin.additional_properties = d
        return edp_origin

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
