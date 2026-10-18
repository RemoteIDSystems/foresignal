from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.window_state_payload_state import WindowStatePayloadState
from ..models.window_state_payload_type import WindowStatePayloadType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.detector_ref import DetectorRef


T = TypeVar("T", bound="WindowStatePayload")


@_attrs_define
class WindowStatePayload:
    """Payload of `rule-windows` records of kind `window-state` (externalId `{ruleId}:{groupKey}`); excludes the evaluation
    timestamp so it changes only with state

        Attributes:
            rule_id (UUID):
            type_ (WindowStatePayloadType):
            group_key (str):
            state (WindowStatePayloadState):
            name (str | Unset):
            value (float | None | Unset):
            threshold (float | None | Unset):
            reason (None | str | Unset):
            detector (DetectorRef | Unset):
            automated (bool | Unset):
    """

    rule_id: UUID
    type_: WindowStatePayloadType
    group_key: str
    state: WindowStatePayloadState
    name: str | Unset = UNSET
    value: float | None | Unset = UNSET
    threshold: float | None | Unset = UNSET
    reason: None | str | Unset = UNSET
    detector: DetectorRef | Unset = UNSET
    automated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rule_id = str(self.rule_id)

        type_ = self.type_.value

        group_key = self.group_key

        state = self.state.value

        name = self.name

        value: float | None | Unset
        if isinstance(self.value, Unset):
            value = UNSET
        else:
            value = self.value

        threshold: float | None | Unset
        if isinstance(self.threshold, Unset):
            threshold = UNSET
        else:
            threshold = self.threshold

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        detector: dict[str, Any] | Unset = UNSET
        if not isinstance(self.detector, Unset):
            detector = self.detector.to_dict()

        automated = self.automated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ruleId": rule_id,
                "type": type_,
                "groupKey": group_key,
                "state": state,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if value is not UNSET:
            field_dict["value"] = value
        if threshold is not UNSET:
            field_dict["threshold"] = threshold
        if reason is not UNSET:
            field_dict["reason"] = reason
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

        type_ = WindowStatePayloadType(d.pop("type"))

        group_key = d.pop("groupKey")

        state = WindowStatePayloadState(d.pop("state"))

        name = d.pop("name", UNSET)

        def _parse_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        value = _parse_value(d.pop("value", UNSET))

        def _parse_threshold(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        threshold = _parse_threshold(d.pop("threshold", UNSET))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        _detector = d.pop("detector", UNSET)
        detector: DetectorRef | Unset
        if isinstance(_detector, Unset):
            detector = UNSET
        else:
            detector = DetectorRef.from_dict(_detector)

        automated = d.pop("automated", UNSET)

        window_state_payload = cls(
            rule_id=rule_id,
            type_=type_,
            group_key=group_key,
            state=state,
            name=name,
            value=value,
            threshold=threshold,
            reason=reason,
            detector=detector,
            automated=automated,
        )

        window_state_payload.additional_properties = d
        return window_state_payload

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
