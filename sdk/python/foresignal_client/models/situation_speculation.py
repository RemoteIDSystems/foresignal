from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SituationSpeculation")


@_attrs_define
class SituationSpeculation:
    """Optional LLM brief, labelled speculation-capable narrative; never changes members, status or severity

    Attributes:
        label (Literal['speculation']):
        brief (None | str): Markdown brief, null when none was generated
        text_origin (Literal['llm'] | Unset):
        brief_generated_at (datetime.datetime | None | Unset):
        brief_model (None | str | Unset):
        manifest_id (None | str | Unset):
    """

    label: Literal["speculation"]
    brief: None | str
    text_origin: Literal["llm"] | Unset = UNSET
    brief_generated_at: datetime.datetime | None | Unset = UNSET
    brief_model: None | str | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        label = self.label

        brief: None | str
        brief = self.brief

        text_origin = self.text_origin

        brief_generated_at: None | str | Unset
        if isinstance(self.brief_generated_at, Unset):
            brief_generated_at = UNSET
        elif isinstance(self.brief_generated_at, datetime.datetime):
            brief_generated_at = self.brief_generated_at.isoformat()
        else:
            brief_generated_at = self.brief_generated_at

        brief_model: None | str | Unset
        if isinstance(self.brief_model, Unset):
            brief_model = UNSET
        else:
            brief_model = self.brief_model

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "label": label,
                "brief": brief,
            }
        )
        if text_origin is not UNSET:
            field_dict["textOrigin"] = text_origin
        if brief_generated_at is not UNSET:
            field_dict["briefGeneratedAt"] = brief_generated_at
        if brief_model is not UNSET:
            field_dict["briefModel"] = brief_model
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        label = cast(Literal["speculation"], d.pop("label"))
        if label != "speculation":
            raise ValueError(f"label must match const 'speculation', got '{label}'")

        def _parse_brief(data: object) -> None | str:
            if data is None:
                return data
            return cast(None | str, data)

        brief = _parse_brief(d.pop("brief"))

        text_origin = cast(Literal["llm"] | Unset, d.pop("textOrigin", UNSET))
        if text_origin != "llm" and not isinstance(text_origin, Unset):
            raise ValueError(f"textOrigin must match const 'llm', got '{text_origin}'")

        def _parse_brief_generated_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                brief_generated_at_type_0 = datetime.datetime.fromisoformat(data)

                return brief_generated_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        brief_generated_at = _parse_brief_generated_at(d.pop("briefGeneratedAt", UNSET))

        def _parse_brief_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        brief_model = _parse_brief_model(d.pop("briefModel", UNSET))

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        situation_speculation = cls(
            label=label,
            brief=brief,
            text_origin=text_origin,
            brief_generated_at=brief_generated_at,
            brief_model=brief_model,
            manifest_id=manifest_id,
        )

        situation_speculation.additional_properties = d
        return situation_speculation

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
