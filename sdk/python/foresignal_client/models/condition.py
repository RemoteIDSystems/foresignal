from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.condition_op import ConditionOp
from ..types import UNSET, Unset

T = TypeVar("T", bound="Condition")


@_attrs_define
class Condition:
    """
    Attributes:
        op (ConditionOp): Comparison ops take numbers (== and != also strings and booleans); in/notIn a non-empty
            homogeneous scalar array (≤ 1000); exists a boolean; startsWith a string ≤ 256; matches a Java regex ≤ 256
            without backreferences, lookaround or nested quantifiers (only (?:…) groups and the escapes \\d \\D \\w \\W \\s
            \\S \\n \\t \\r \\f, so PostgreSQL reads it the same way). A value of another JSON type than the operator
            expects, a missing path or JSON null never matches (except exists); string operators need a JSON string (the
            number 5 does not equal "5"); != and notIn need the value present. inWatchlist/notInWatchlist (036) take a
            watchlist id; the payload value must be a JSON string or number (compared by its JSON text after the list's
            normalization); anything else, a missing path or JSON null never matches either operator.
        value (bool | float | list[bool | float | str] | str):
        path (str | Unset): Dot path into payload. Required for every operator except inWatchlist and notInWatchlist,
            which without a path read each source's declared identity field (descriptor entityKeys, 036); a source that does
            not declare the watchlist's identity never matches Example: mag.
    """

    op: ConditionOp
    value: bool | float | list[bool | float | str] | str
    path: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        op = self.op.value

        value: bool | float | list[bool | float | str] | str
        if isinstance(self.value, list):
            value = []
            for value_type_3_item_data in self.value:
                value_type_3_item: bool | float | str
                value_type_3_item = value_type_3_item_data
                value.append(value_type_3_item)

        else:
            value = self.value

        path = self.path

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "op": op,
                "value": value,
            }
        )
        if path is not UNSET:
            field_dict["path"] = path

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        op = ConditionOp(d.pop("op"))

        def _parse_value(data: object) -> bool | float | list[bool | float | str] | str:
            try:
                if not isinstance(data, list):
                    raise TypeError()
                value_type_3 = []
                _value_type_3 = data
                for value_type_3_item_data in _value_type_3:

                    def _parse_value_type_3_item(data: object) -> bool | float | str:
                        return cast(bool | float | str, data)

                    value_type_3_item = _parse_value_type_3_item(value_type_3_item_data)

                    value_type_3.append(value_type_3_item)

                return value_type_3
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(bool | float | list[bool | float | str] | str, data)

        value = _parse_value(d.pop("value"))

        path = d.pop("path", UNSET)

        condition = cls(
            op=op,
            value=value,
            path=path,
        )

        condition.additional_properties = d
        return condition

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
