from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="QuestionLinks")


@_attrs_define
class QuestionLinks:
    """Objects a question is about

    Attributes:
        situation_id (None | str | Unset):
        hypothesis_set_id (None | str | Unset):
        precursor_id (None | str | Unset):
        finding_id (None | str | Unset):
        series_ref (None | str | Unset):
        outlook_id (None | str | Unset):
    """

    situation_id: None | str | Unset = UNSET
    hypothesis_set_id: None | str | Unset = UNSET
    precursor_id: None | str | Unset = UNSET
    finding_id: None | str | Unset = UNSET
    series_ref: None | str | Unset = UNSET
    outlook_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        situation_id: None | str | Unset
        if isinstance(self.situation_id, Unset):
            situation_id = UNSET
        else:
            situation_id = self.situation_id

        hypothesis_set_id: None | str | Unset
        if isinstance(self.hypothesis_set_id, Unset):
            hypothesis_set_id = UNSET
        else:
            hypothesis_set_id = self.hypothesis_set_id

        precursor_id: None | str | Unset
        if isinstance(self.precursor_id, Unset):
            precursor_id = UNSET
        else:
            precursor_id = self.precursor_id

        finding_id: None | str | Unset
        if isinstance(self.finding_id, Unset):
            finding_id = UNSET
        else:
            finding_id = self.finding_id

        series_ref: None | str | Unset
        if isinstance(self.series_ref, Unset):
            series_ref = UNSET
        else:
            series_ref = self.series_ref

        outlook_id: None | str | Unset
        if isinstance(self.outlook_id, Unset):
            outlook_id = UNSET
        else:
            outlook_id = self.outlook_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if situation_id is not UNSET:
            field_dict["situationId"] = situation_id
        if hypothesis_set_id is not UNSET:
            field_dict["hypothesisSetId"] = hypothesis_set_id
        if precursor_id is not UNSET:
            field_dict["precursorId"] = precursor_id
        if finding_id is not UNSET:
            field_dict["findingId"] = finding_id
        if series_ref is not UNSET:
            field_dict["seriesRef"] = series_ref
        if outlook_id is not UNSET:
            field_dict["outlookId"] = outlook_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_situation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        situation_id = _parse_situation_id(d.pop("situationId", UNSET))

        def _parse_hypothesis_set_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        hypothesis_set_id = _parse_hypothesis_set_id(d.pop("hypothesisSetId", UNSET))

        def _parse_precursor_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        precursor_id = _parse_precursor_id(d.pop("precursorId", UNSET))

        def _parse_finding_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        finding_id = _parse_finding_id(d.pop("findingId", UNSET))

        def _parse_series_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        series_ref = _parse_series_ref(d.pop("seriesRef", UNSET))

        def _parse_outlook_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        outlook_id = _parse_outlook_id(d.pop("outlookId", UNSET))

        question_links = cls(
            situation_id=situation_id,
            hypothesis_set_id=hypothesis_set_id,
            precursor_id=precursor_id,
            finding_id=finding_id,
            series_ref=series_ref,
            outlook_id=outlook_id,
        )

        question_links.additional_properties = d
        return question_links

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
