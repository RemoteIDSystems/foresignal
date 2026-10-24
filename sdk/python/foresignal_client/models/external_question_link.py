from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.external_question_link_input_orientation import (
    ExternalQuestionLinkInputOrientation,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="ExternalQuestionLink")


@_attrs_define
class ExternalQuestionLink:
    """A provider question linked to a ledger question

    Attributes:
        stream_id (str):
        external_question_id (str):
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        source_id (str):
        forecaster_id (str):  Example: edp:ext-markets:kalshi.
        created_at (datetime.datetime):
        orientation (ExternalQuestionLinkInputOrientation | Unset):  Default: ExternalQuestionLinkInputOrientation.SAME.
        last_forecast_at (datetime.datetime | None | Unset):
    """

    stream_id: str
    external_question_id: str
    question_id: str
    id: str
    source_id: str
    forecaster_id: str
    created_at: datetime.datetime
    orientation: ExternalQuestionLinkInputOrientation | Unset = (
        ExternalQuestionLinkInputOrientation.SAME
    )
    last_forecast_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        stream_id = self.stream_id

        external_question_id = self.external_question_id

        question_id = self.question_id

        id = self.id

        source_id = self.source_id

        forecaster_id = self.forecaster_id

        created_at = self.created_at.isoformat()

        orientation: str | Unset = UNSET
        if not isinstance(self.orientation, Unset):
            orientation = self.orientation.value

        last_forecast_at: None | str | Unset
        if isinstance(self.last_forecast_at, Unset):
            last_forecast_at = UNSET
        elif isinstance(self.last_forecast_at, datetime.datetime):
            last_forecast_at = self.last_forecast_at.isoformat()
        else:
            last_forecast_at = self.last_forecast_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "streamId": stream_id,
                "externalQuestionId": external_question_id,
                "questionId": question_id,
                "id": id,
                "sourceId": source_id,
                "forecasterId": forecaster_id,
                "createdAt": created_at,
            }
        )
        if orientation is not UNSET:
            field_dict["orientation"] = orientation
        if last_forecast_at is not UNSET:
            field_dict["lastForecastAt"] = last_forecast_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        stream_id = d.pop("streamId")

        external_question_id = d.pop("externalQuestionId")

        question_id = d.pop("questionId")

        id = d.pop("id")

        source_id = d.pop("sourceId")

        forecaster_id = d.pop("forecasterId")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        _orientation = d.pop("orientation", UNSET)
        orientation: ExternalQuestionLinkInputOrientation | Unset
        if isinstance(_orientation, Unset):
            orientation = UNSET
        else:
            orientation = ExternalQuestionLinkInputOrientation(_orientation)

        def _parse_last_forecast_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_forecast_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_forecast_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_forecast_at = _parse_last_forecast_at(d.pop("lastForecastAt", UNSET))

        external_question_link = cls(
            stream_id=stream_id,
            external_question_id=external_question_id,
            question_id=question_id,
            id=id,
            source_id=source_id,
            forecaster_id=forecaster_id,
            created_at=created_at,
            orientation=orientation,
            last_forecast_at=last_forecast_at,
        )

        external_question_link.additional_properties = d
        return external_question_link

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
