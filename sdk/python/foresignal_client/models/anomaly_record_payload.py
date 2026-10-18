from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.anomaly_record_payload_classification import (
    AnomalyRecordPayloadClassification,
)
from ..models.anomaly_record_payload_state import AnomalyRecordPayloadState
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.detector_ref import DetectorRef


T = TypeVar("T", bound="AnomalyRecordPayload")


@_attrs_define
class AnomalyRecordPayload:
    """Payload of `rule-windows` records of kind `anomaly` (externalId `{ruleId}:{groupKey}`)

    Attributes:
        rule_id (UUID):
        group_key (str):
        classification (AnomalyRecordPayloadClassification):
        name (str | Unset):
        state (AnomalyRecordPayloadState | Unset):
        observed (int | Unset):
        expected (float | Unset):
        p_value (float | None | Unset):
        detector (DetectorRef | Unset):
        automated (bool | Unset):
    """

    rule_id: UUID
    group_key: str
    classification: AnomalyRecordPayloadClassification
    name: str | Unset = UNSET
    state: AnomalyRecordPayloadState | Unset = UNSET
    observed: int | Unset = UNSET
    expected: float | Unset = UNSET
    p_value: float | None | Unset = UNSET
    detector: DetectorRef | Unset = UNSET
    automated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rule_id = str(self.rule_id)

        group_key = self.group_key

        classification = self.classification.value

        name = self.name

        state: str | Unset = UNSET
        if not isinstance(self.state, Unset):
            state = self.state.value

        observed = self.observed

        expected = self.expected

        p_value: float | None | Unset
        if isinstance(self.p_value, Unset):
            p_value = UNSET
        else:
            p_value = self.p_value

        detector: dict[str, Any] | Unset = UNSET
        if not isinstance(self.detector, Unset):
            detector = self.detector.to_dict()

        automated = self.automated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ruleId": rule_id,
                "groupKey": group_key,
                "classification": classification,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if state is not UNSET:
            field_dict["state"] = state
        if observed is not UNSET:
            field_dict["observed"] = observed
        if expected is not UNSET:
            field_dict["expected"] = expected
        if p_value is not UNSET:
            field_dict["pValue"] = p_value
        if detector is not UNSET:
            field_dict["detector"] = detector
        if automated is not UNSET:
            field_dict["automated"] = automated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.detector_ref import DetectorRef

        d = dict(src_dict)
        rule_id = UUID(d.pop("ruleId"))

        group_key = d.pop("groupKey")

        classification = AnomalyRecordPayloadClassification(d.pop("classification"))

        name = d.pop("name", UNSET)

        _state = d.pop("state", UNSET)
        state: AnomalyRecordPayloadState | Unset
        if isinstance(_state, Unset):
            state = UNSET
        else:
            state = AnomalyRecordPayloadState(_state)

        observed = d.pop("observed", UNSET)

        expected = d.pop("expected", UNSET)

        def _parse_p_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p_value = _parse_p_value(d.pop("pValue", UNSET))

        _detector = d.pop("detector", UNSET)
        detector: DetectorRef | Unset
        if isinstance(_detector, Unset):
            detector = UNSET
        else:
            detector = DetectorRef.from_dict(_detector)

        automated = d.pop("automated", UNSET)

        anomaly_record_payload = cls(
            rule_id=rule_id,
            group_key=group_key,
            classification=classification,
            name=name,
            state=state,
            observed=observed,
            expected=expected,
            p_value=p_value,
            detector=detector,
            automated=automated,
        )

        anomaly_record_payload.additional_properties = d
        return anomaly_record_payload

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
