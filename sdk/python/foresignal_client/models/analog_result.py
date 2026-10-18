from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.analog_result_method import AnalogResultMethod
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analog import Analog
    from ..models.analog_result_outcome_distribution_type_0 import (
        AnalogResultOutcomeDistributionType0,
    )
    from ..models.analog_result_signature_type_0 import AnalogResultSignatureType0
    from ..models.analog_result_target import AnalogResultTarget
    from ..models.caveat import Caveat
    from ..models.detector_ref import DetectorRef


T = TypeVar("T", bound="AnalogResult")


@_attrs_define
class AnalogResult:
    """Analog search result with the count found and an outcome distribution shrunk toward the base rate

    Attributes:
        target (AnalogResultTarget): What was matched
        k (int):
        count_found (int): Analogs found before truncation to k
        method (AnalogResultMethod):
        analogs (list[Analog]):
        caveats (list[Caveat]):
        detector (DetectorRef):
        manifest_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        method_note (None | str | Unset): e.g. `feature similarity only: embeddings unavailable`
        signature (AnalogResultSignatureType0 | None | Unset):
        outcome_distribution (AnalogResultOutcomeDistributionType0 | None | Unset):
    """

    target: AnalogResultTarget
    k: int
    count_found: int
    method: AnalogResultMethod
    analogs: list[Analog]
    caveats: list[Caveat]
    detector: DetectorRef
    manifest_id: str
    method_note: None | str | Unset = UNSET
    signature: AnalogResultSignatureType0 | None | Unset = UNSET
    outcome_distribution: AnalogResultOutcomeDistributionType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.analog_result_outcome_distribution_type_0 import (
            AnalogResultOutcomeDistributionType0,
        )
        from ..models.analog_result_signature_type_0 import AnalogResultSignatureType0

        target = self.target.to_dict()

        k = self.k

        count_found = self.count_found

        method = self.method.value

        analogs = []
        for analogs_item_data in self.analogs:
            analogs_item = analogs_item_data.to_dict()
            analogs.append(analogs_item)

        caveats = []
        for caveats_item_data in self.caveats:
            caveats_item = caveats_item_data.to_dict()
            caveats.append(caveats_item)

        detector = self.detector.to_dict()

        manifest_id = self.manifest_id

        method_note: None | str | Unset
        if isinstance(self.method_note, Unset):
            method_note = UNSET
        else:
            method_note = self.method_note

        signature: dict[str, Any] | None | Unset
        if isinstance(self.signature, Unset):
            signature = UNSET
        elif isinstance(self.signature, AnalogResultSignatureType0):
            signature = self.signature.to_dict()
        else:
            signature = self.signature

        outcome_distribution: dict[str, Any] | None | Unset
        if isinstance(self.outcome_distribution, Unset):
            outcome_distribution = UNSET
        elif isinstance(
            self.outcome_distribution, AnalogResultOutcomeDistributionType0
        ):
            outcome_distribution = self.outcome_distribution.to_dict()
        else:
            outcome_distribution = self.outcome_distribution

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "target": target,
                "k": k,
                "countFound": count_found,
                "method": method,
                "analogs": analogs,
                "caveats": caveats,
                "detector": detector,
                "manifestId": manifest_id,
            }
        )
        if method_note is not UNSET:
            field_dict["methodNote"] = method_note
        if signature is not UNSET:
            field_dict["signature"] = signature
        if outcome_distribution is not UNSET:
            field_dict["outcomeDistribution"] = outcome_distribution

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analog import Analog
        from ..models.analog_result_outcome_distribution_type_0 import (
            AnalogResultOutcomeDistributionType0,
        )
        from ..models.analog_result_signature_type_0 import AnalogResultSignatureType0
        from ..models.analog_result_target import AnalogResultTarget
        from ..models.caveat import Caveat
        from ..models.detector_ref import DetectorRef

        d = dict(src_dict)
        target = AnalogResultTarget.from_dict(d.pop("target"))

        k = d.pop("k")

        count_found = d.pop("countFound")

        method = AnalogResultMethod(d.pop("method"))

        analogs = []
        _analogs = d.pop("analogs")
        for analogs_item_data in _analogs:
            analogs_item = Analog.from_dict(analogs_item_data)

            analogs.append(analogs_item)

        caveats = []
        _caveats = d.pop("caveats")
        for caveats_item_data in _caveats:
            caveats_item = Caveat.from_dict(caveats_item_data)

            caveats.append(caveats_item)

        detector = DetectorRef.from_dict(d.pop("detector"))

        manifest_id = d.pop("manifestId")

        def _parse_method_note(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        method_note = _parse_method_note(d.pop("methodNote", UNSET))

        def _parse_signature(data: object) -> AnalogResultSignatureType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                signature_type_0 = AnalogResultSignatureType0.from_dict(data)

                return signature_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AnalogResultSignatureType0 | None | Unset, data)

        signature = _parse_signature(d.pop("signature", UNSET))

        def _parse_outcome_distribution(
            data: object,
        ) -> AnalogResultOutcomeDistributionType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                outcome_distribution_type_0 = (
                    AnalogResultOutcomeDistributionType0.from_dict(data)
                )

                return outcome_distribution_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AnalogResultOutcomeDistributionType0 | None | Unset, data)

        outcome_distribution = _parse_outcome_distribution(
            d.pop("outcomeDistribution", UNSET)
        )

        analog_result = cls(
            target=target,
            k=k,
            count_found=count_found,
            method=method,
            analogs=analogs,
            caveats=caveats,
            detector=detector,
            manifest_id=manifest_id,
            method_note=method_note,
            signature=signature,
            outcome_distribution=outcome_distribution,
        )

        analog_result.additional_properties = d
        return analog_result

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
