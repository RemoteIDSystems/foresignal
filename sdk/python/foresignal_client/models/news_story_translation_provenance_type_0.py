from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..types import UNSET, Unset

T = TypeVar("T", bound="NewsStoryTranslationProvenanceType0")


@_attrs_define
class NewsStoryTranslationProvenanceType0:
    """How the translation was made

    Attributes:
        engine (str):
        at (datetime.datetime):
        model (None | str | Unset):
    """

    engine: str
    at: datetime.datetime
    model: None | str | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        engine = self.engine

        at = self.at.isoformat()

        model: None | str | Unset
        if isinstance(self.model, Unset):
            model = UNSET
        else:
            model = self.model

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "engine": engine,
                "at": at,
            }
        )
        if model is not UNSET:
            field_dict["model"] = model

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        engine = d.pop("engine")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        def _parse_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        model = _parse_model(d.pop("model", UNSET))

        news_story_translation_provenance_type_0 = cls(
            engine=engine,
            at=at,
            model=model,
        )

        return news_story_translation_provenance_type_0
