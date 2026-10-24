from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.analysis_request_submission_orientation import (
    AnalysisRequestSubmissionOrientation,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="AnalysisRequestSubmission")


@_attrs_define
class AnalysisRequestSubmission:
    """Requests an on-demand forecast of a ledger question from an external forecaster (155).

    Attributes:
        forecaster_id (str): The external forecaster id, edp:{sourceId}/{streamId}.
        question_id (str): The ledger question to forecast; must be open.
        orientation (AnalysisRequestSubmissionOrientation | Unset): Whether the provider's probability is for the
            question as stated (same) or its negation (inverse). Default: AnalysisRequestSubmissionOrientation.SAME.
        horizon (None | str | Unset): Optional forecast horizon passed to the provider (for example an ISO-8601
            duration).
    """

    forecaster_id: str
    question_id: str
    orientation: AnalysisRequestSubmissionOrientation | Unset = (
        AnalysisRequestSubmissionOrientation.SAME
    )
    horizon: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        forecaster_id = self.forecaster_id

        question_id = self.question_id

        orientation: str | Unset = UNSET
        if not isinstance(self.orientation, Unset):
            orientation = self.orientation.value

        horizon: None | str | Unset
        if isinstance(self.horizon, Unset):
            horizon = UNSET
        else:
            horizon = self.horizon

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "forecasterId": forecaster_id,
                "questionId": question_id,
            }
        )
        if orientation is not UNSET:
            field_dict["orientation"] = orientation
        if horizon is not UNSET:
            field_dict["horizon"] = horizon

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        forecaster_id = d.pop("forecasterId")

        question_id = d.pop("questionId")

        _orientation = d.pop("orientation", UNSET)
        orientation: AnalysisRequestSubmissionOrientation | Unset
        if isinstance(_orientation, Unset):
            orientation = UNSET
        else:
            orientation = AnalysisRequestSubmissionOrientation(_orientation)

        def _parse_horizon(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        horizon = _parse_horizon(d.pop("horizon", UNSET))

        analysis_request_submission = cls(
            forecaster_id=forecaster_id,
            question_id=question_id,
            orientation=orientation,
            horizon=horizon,
        )

        analysis_request_submission.additional_properties = d
        return analysis_request_submission

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
