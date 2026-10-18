from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_forecast_item_properties import EdpForecastItemProperties
    from ..models.edp_forecast_item_question_type_0 import EdpForecastItemQuestionType0
    from ..models.edp_origin import EdpOrigin
    from ..models.quantiles import Quantiles


T = TypeVar("T", bound="EdpForecastItem")


@_attrs_define
class EdpForecastItem:
    """A probability or quantiles (monotonic: p05 ≤ p10 ≤ p25 ≤ p50 ≤ p75 ≤ p90 ≤ p95, checked by the server) for a
    question

        Attributes:
            stream (str):
            type_ (Literal['forecast']):
            id (str):
            made_at (datetime.datetime): Provider-claimed time; the recorded made_at is max(madeAt, receipt time)
            observed_at (datetime.datetime | Unset):
            properties (EdpForecastItemProperties | Unset): Item properties, validated against the stream's payloadSchema
            question (EdpForecastItemQuestionType0 | None | Unset):
            question_ref (None | str | Unset):
            probability (float | None | Unset):
            quantiles (None | Quantiles | Unset):
            origin (EdpOrigin | None | Unset):
    """

    stream: str
    type_: Literal["forecast"]
    id: str
    made_at: datetime.datetime
    observed_at: datetime.datetime | Unset = UNSET
    properties: EdpForecastItemProperties | Unset = UNSET
    question: EdpForecastItemQuestionType0 | None | Unset = UNSET
    question_ref: None | str | Unset = UNSET
    probability: float | None | Unset = UNSET
    quantiles: None | Quantiles | Unset = UNSET
    origin: EdpOrigin | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.edp_forecast_item_question_type_0 import (
            EdpForecastItemQuestionType0,
        )
        from ..models.edp_origin import EdpOrigin
        from ..models.quantiles import Quantiles

        stream = self.stream

        type_ = self.type_

        id = self.id

        made_at = self.made_at.isoformat()

        observed_at: str | Unset = UNSET
        if not isinstance(self.observed_at, Unset):
            observed_at = self.observed_at.isoformat()

        properties: dict[str, Any] | Unset = UNSET
        if not isinstance(self.properties, Unset):
            properties = self.properties.to_dict()

        question: dict[str, Any] | None | Unset
        if isinstance(self.question, Unset):
            question = UNSET
        elif isinstance(self.question, EdpForecastItemQuestionType0):
            question = self.question.to_dict()
        else:
            question = self.question

        question_ref: None | str | Unset
        if isinstance(self.question_ref, Unset):
            question_ref = UNSET
        else:
            question_ref = self.question_ref

        probability: float | None | Unset
        if isinstance(self.probability, Unset):
            probability = UNSET
        else:
            probability = self.probability

        quantiles: dict[str, Any] | None | Unset
        if isinstance(self.quantiles, Unset):
            quantiles = UNSET
        elif isinstance(self.quantiles, Quantiles):
            quantiles = self.quantiles.to_dict()
        else:
            quantiles = self.quantiles

        origin: dict[str, Any] | None | Unset
        if isinstance(self.origin, Unset):
            origin = UNSET
        elif isinstance(self.origin, EdpOrigin):
            origin = self.origin.to_dict()
        else:
            origin = self.origin

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "stream": stream,
                "type": type_,
                "id": id,
                "madeAt": made_at,
            }
        )
        if observed_at is not UNSET:
            field_dict["observedAt"] = observed_at
        if properties is not UNSET:
            field_dict["properties"] = properties
        if question is not UNSET:
            field_dict["question"] = question
        if question_ref is not UNSET:
            field_dict["questionRef"] = question_ref
        if probability is not UNSET:
            field_dict["probability"] = probability
        if quantiles is not UNSET:
            field_dict["quantiles"] = quantiles
        if origin is not UNSET:
            field_dict["origin"] = origin

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_forecast_item_properties import EdpForecastItemProperties
        from ..models.edp_forecast_item_question_type_0 import (
            EdpForecastItemQuestionType0,
        )
        from ..models.edp_origin import EdpOrigin
        from ..models.quantiles import Quantiles

        d = dict(src_dict)
        stream = d.pop("stream")

        type_ = cast(Literal["forecast"], d.pop("type"))
        if type_ != "forecast":
            raise ValueError(f"type must match const 'forecast', got '{type_}'")

        id = d.pop("id")

        made_at = datetime.datetime.fromisoformat(d.pop("madeAt"))

        _observed_at = d.pop("observedAt", UNSET)
        observed_at: datetime.datetime | Unset
        if isinstance(_observed_at, Unset):
            observed_at = UNSET
        else:
            observed_at = datetime.datetime.fromisoformat(_observed_at)

        _properties = d.pop("properties", UNSET)
        properties: EdpForecastItemProperties | Unset
        if isinstance(_properties, Unset):
            properties = UNSET
        else:
            properties = EdpForecastItemProperties.from_dict(_properties)

        def _parse_question(
            data: object,
        ) -> EdpForecastItemQuestionType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                question_type_0 = EdpForecastItemQuestionType0.from_dict(data)

                return question_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EdpForecastItemQuestionType0 | None | Unset, data)

        question = _parse_question(d.pop("question", UNSET))

        def _parse_question_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        question_ref = _parse_question_ref(d.pop("questionRef", UNSET))

        def _parse_probability(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        probability = _parse_probability(d.pop("probability", UNSET))

        def _parse_quantiles(data: object) -> None | Quantiles | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                quantiles_type_0 = Quantiles.from_dict(data)

                return quantiles_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Quantiles | Unset, data)

        quantiles = _parse_quantiles(d.pop("quantiles", UNSET))

        def _parse_origin(data: object) -> EdpOrigin | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                origin_type_0 = EdpOrigin.from_dict(data)

                return origin_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EdpOrigin | None | Unset, data)

        origin = _parse_origin(d.pop("origin", UNSET))

        edp_forecast_item = cls(
            stream=stream,
            type_=type_,
            id=id,
            made_at=made_at,
            observed_at=observed_at,
            properties=properties,
            question=question,
            question_ref=question_ref,
            probability=probability,
            quantiles=quantiles,
            origin=origin,
        )

        edp_forecast_item.additional_properties = d
        return edp_forecast_item

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
