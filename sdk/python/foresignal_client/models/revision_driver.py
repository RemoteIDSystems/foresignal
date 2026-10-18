from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.revision_driver_kind import RevisionDriverKind
from ..models.revision_driver_suppression_reason_type_1 import (
    RevisionDriverSuppressionReasonType1,
)
from ..models.revision_driver_suppression_reason_type_2_type_1 import (
    RevisionDriverSuppressionReasonType2Type1,
)
from ..models.revision_driver_suppression_reason_type_3_type_1 import (
    RevisionDriverSuppressionReasonType3Type1,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="RevisionDriver")


@_attrs_define
class RevisionDriver:
    """One driver of a revision and its log-likelihood contribution

    Attributes:
        kind (RevisionDriverKind):
        hypothesis_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        applied (float): Log-odds change applied after suppression and damping
        suppressed (bool):
        id (None | str | Unset): Indicator, evidence or base-rate id
        ref (None | str | Unset):
        signal_family (None | str | Unset):
        lr (float | None | Unset):
        ln_lr (float | None | Unset):
        p_detect (float | None | Unset):
        suppression_reason (None | RevisionDriverSuppressionReasonType1 | RevisionDriverSuppressionReasonType2Type1 |
            RevisionDriverSuppressionReasonType3Type1 | Unset):
    """

    kind: RevisionDriverKind
    hypothesis_id: str
    applied: float
    suppressed: bool
    id: None | str | Unset = UNSET
    ref: None | str | Unset = UNSET
    signal_family: None | str | Unset = UNSET
    lr: float | None | Unset = UNSET
    ln_lr: float | None | Unset = UNSET
    p_detect: float | None | Unset = UNSET
    suppression_reason: (
        None
        | RevisionDriverSuppressionReasonType1
        | RevisionDriverSuppressionReasonType2Type1
        | RevisionDriverSuppressionReasonType3Type1
        | Unset
    ) = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kind = self.kind.value

        hypothesis_id = self.hypothesis_id

        applied = self.applied

        suppressed = self.suppressed

        id: None | str | Unset
        if isinstance(self.id, Unset):
            id = UNSET
        else:
            id = self.id

        ref: None | str | Unset
        if isinstance(self.ref, Unset):
            ref = UNSET
        else:
            ref = self.ref

        signal_family: None | str | Unset
        if isinstance(self.signal_family, Unset):
            signal_family = UNSET
        else:
            signal_family = self.signal_family

        lr: float | None | Unset
        if isinstance(self.lr, Unset):
            lr = UNSET
        else:
            lr = self.lr

        ln_lr: float | None | Unset
        if isinstance(self.ln_lr, Unset):
            ln_lr = UNSET
        else:
            ln_lr = self.ln_lr

        p_detect: float | None | Unset
        if isinstance(self.p_detect, Unset):
            p_detect = UNSET
        else:
            p_detect = self.p_detect

        suppression_reason: None | str | Unset
        if isinstance(self.suppression_reason, Unset):
            suppression_reason = UNSET
        elif (
            isinstance(self.suppression_reason, RevisionDriverSuppressionReasonType1)
            or isinstance(
                self.suppression_reason, RevisionDriverSuppressionReasonType2Type1
            )
            or isinstance(
                self.suppression_reason, RevisionDriverSuppressionReasonType3Type1
            )
        ):
            suppression_reason = self.suppression_reason.value
        else:
            suppression_reason = self.suppression_reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kind": kind,
                "hypothesisId": hypothesis_id,
                "applied": applied,
                "suppressed": suppressed,
            }
        )
        if id is not UNSET:
            field_dict["id"] = id
        if ref is not UNSET:
            field_dict["ref"] = ref
        if signal_family is not UNSET:
            field_dict["signalFamily"] = signal_family
        if lr is not UNSET:
            field_dict["lr"] = lr
        if ln_lr is not UNSET:
            field_dict["lnLr"] = ln_lr
        if p_detect is not UNSET:
            field_dict["pDetect"] = p_detect
        if suppression_reason is not UNSET:
            field_dict["suppressionReason"] = suppression_reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        kind = RevisionDriverKind(d.pop("kind"))

        hypothesis_id = d.pop("hypothesisId")

        applied = d.pop("applied")

        suppressed = d.pop("suppressed")

        def _parse_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        id = _parse_id(d.pop("id", UNSET))

        def _parse_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ref = _parse_ref(d.pop("ref", UNSET))

        def _parse_signal_family(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        signal_family = _parse_signal_family(d.pop("signalFamily", UNSET))

        def _parse_lr(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lr = _parse_lr(d.pop("lr", UNSET))

        def _parse_ln_lr(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        ln_lr = _parse_ln_lr(d.pop("lnLr", UNSET))

        def _parse_p_detect(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p_detect = _parse_p_detect(d.pop("pDetect", UNSET))

        def _parse_suppression_reason(
            data: object,
        ) -> (
            None
            | RevisionDriverSuppressionReasonType1
            | RevisionDriverSuppressionReasonType2Type1
            | RevisionDriverSuppressionReasonType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                suppression_reason_type_1 = RevisionDriverSuppressionReasonType1(data)

                return suppression_reason_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                suppression_reason_type_2_type_1 = (
                    RevisionDriverSuppressionReasonType2Type1(data)
                )

                return suppression_reason_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                suppression_reason_type_3_type_1 = (
                    RevisionDriverSuppressionReasonType3Type1(data)
                )

                return suppression_reason_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | RevisionDriverSuppressionReasonType1
                | RevisionDriverSuppressionReasonType2Type1
                | RevisionDriverSuppressionReasonType3Type1
                | Unset,
                data,
            )

        suppression_reason = _parse_suppression_reason(
            d.pop("suppressionReason", UNSET)
        )

        revision_driver = cls(
            kind=kind,
            hypothesis_id=hypothesis_id,
            applied=applied,
            suppressed=suppressed,
            id=id,
            ref=ref,
            signal_family=signal_family,
            lr=lr,
            ln_lr=ln_lr,
            p_detect=p_detect,
            suppression_reason=suppression_reason,
        )

        revision_driver.additional_properties = d
        return revision_driver

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
