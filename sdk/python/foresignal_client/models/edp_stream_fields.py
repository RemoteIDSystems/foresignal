from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.edp_stream_fields_observed_at_mode import EdpStreamFieldsObservedAtMode
from ..types import UNSET, Unset

T = TypeVar("T", bound="EdpStreamFields")


@_attrs_define
class EdpStreamFields:
    """Field roles, as in custom sources (024)

    Attributes:
        title (None | str | Unset):
        significant (list[str] | Unset):
        volatile (list[str] | Unset):
        observed_at_mode (EdpStreamFieldsObservedAtMode | Unset):  Default: EdpStreamFieldsObservedAtMode.UPSTREAM.
    """

    title: None | str | Unset = UNSET
    significant: list[str] | Unset = UNSET
    volatile: list[str] | Unset = UNSET
    observed_at_mode: EdpStreamFieldsObservedAtMode | Unset = (
        EdpStreamFieldsObservedAtMode.UPSTREAM
    )
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        significant: list[str] | Unset = UNSET
        if not isinstance(self.significant, Unset):
            significant = self.significant

        volatile: list[str] | Unset = UNSET
        if not isinstance(self.volatile, Unset):
            volatile = self.volatile

        observed_at_mode: str | Unset = UNSET
        if not isinstance(self.observed_at_mode, Unset):
            observed_at_mode = self.observed_at_mode.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if title is not UNSET:
            field_dict["title"] = title
        if significant is not UNSET:
            field_dict["significant"] = significant
        if volatile is not UNSET:
            field_dict["volatile"] = volatile
        if observed_at_mode is not UNSET:
            field_dict["observedAtMode"] = observed_at_mode

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        significant = cast(list[str], d.pop("significant", UNSET))

        volatile = cast(list[str], d.pop("volatile", UNSET))

        _observed_at_mode = d.pop("observedAtMode", UNSET)
        observed_at_mode: EdpStreamFieldsObservedAtMode | Unset
        if isinstance(_observed_at_mode, Unset):
            observed_at_mode = UNSET
        else:
            observed_at_mode = EdpStreamFieldsObservedAtMode(_observed_at_mode)

        edp_stream_fields = cls(
            title=title,
            significant=significant,
            volatile=volatile,
            observed_at_mode=observed_at_mode,
        )

        edp_stream_fields.additional_properties = d
        return edp_stream_fields

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
