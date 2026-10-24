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

from ..models.precursor_correlation_sign_type_1 import PrecursorCorrelationSignType1
from ..models.precursor_correlation_sign_type_2_type_1 import (
    PrecursorCorrelationSignType2Type1,
)
from ..models.precursor_correlation_sign_type_3_type_1 import (
    PrecursorCorrelationSignType3Type1,
)
from ..models.precursor_kind import PrecursorKind
from ..models.precursor_origin import PrecursorOrigin
from ..models.precursor_status import PrecursorStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.detector_ref import DetectorRef
    from ..models.precursor_history_item import PrecursorHistoryItem
    from ..models.precursor_lag_distribution_type_0 import PrecursorLagDistributionType0
    from ..models.precursor_side import PrecursorSide
    from ..models.precursor_spatial import PrecursorSpatial
    from ..models.precursor_window_stats import PrecursorWindowStats
    from ..models.stat_interval import StatInterval


T = TypeVar("T", bound="Precursor")


@_attrs_define
class Precursor:
    """A library pattern: association in stored data that held on a later, unseen window

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        kind (PrecursorKind):
        antecedent (PrecursorSide): Antecedent or consequent of a precursor: an event selector or a series
        consequent (PrecursorSide): Antecedent or consequent of a precursor: an event selector or a series
        spatial (PrecursorSpatial): Spatial rule linking antecedent and consequent occurrences (103 forms)
        lag_min (str): ISO-8601 duration Example: P3D.
        lag_max (str): ISO-8601 duration Example: P3D.
        n_antecedent (int):
        discovery (PrecursorWindowStats): Test statistics of a precursor on one window (discovery or validation)
        batch_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        batch_size (int): Tests in the FDR batch
        status (PrecursorStatus):
        detector (DetectorRef):
        origin (PrecursorOrigin):
        description (str): Deterministic plain-language description
        association_note (Literal['association in stored data; not causation']):
        caveats (list[Caveat]):
        manifest_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        region_codes (list[str] | Unset):
        best_lag (None | str | Unset):
        lag_distribution (None | PrecursorLagDistributionType0 | Unset):
        hit_rate (float | None | Unset):
        hit_rate_interval (None | StatInterval | Unset):
        base_rate (float | None | Unset):
        lift (float | None | Unset):
        correlation (float | None | Unset):
        correlation_sign (None | PrecursorCorrelationSignType1 | PrecursorCorrelationSignType2Type1 |
            PrecursorCorrelationSignType3Type1 | Unset):
        validation (None | PrecursorWindowStats | Unset):
        last_retest (None | PrecursorWindowStats | Unset): Statistics of the latest monthly re-test window (newest third
            of the range), or null before the first re-test
        status_reason (None | str | Unset):
        operator_validated (bool | Unset): Pinned as validated by an operator (logged and labeled)
        first_validated_at (datetime.datetime | None | Unset):
        last_tested_at (datetime.datetime | None | Unset):
        history (list[PrecursorHistoryItem] | Unset): Status changes, oldest first (who, when, from, to, why); operator
            retire/pin actions are logged here
        created_at (datetime.datetime | Unset):
        updated_at (datetime.datetime | Unset):
    """

    id: str
    kind: PrecursorKind
    antecedent: PrecursorSide
    consequent: PrecursorSide
    spatial: PrecursorSpatial
    lag_min: str
    lag_max: str
    n_antecedent: int
    discovery: PrecursorWindowStats
    batch_id: str
    batch_size: int
    status: PrecursorStatus
    detector: DetectorRef
    origin: PrecursorOrigin
    description: str
    association_note: Literal["association in stored data; not causation"]
    caveats: list[Caveat]
    manifest_id: str
    region_codes: list[str] | Unset = UNSET
    best_lag: None | str | Unset = UNSET
    lag_distribution: None | PrecursorLagDistributionType0 | Unset = UNSET
    hit_rate: float | None | Unset = UNSET
    hit_rate_interval: None | StatInterval | Unset = UNSET
    base_rate: float | None | Unset = UNSET
    lift: float | None | Unset = UNSET
    correlation: float | None | Unset = UNSET
    correlation_sign: (
        None
        | PrecursorCorrelationSignType1
        | PrecursorCorrelationSignType2Type1
        | PrecursorCorrelationSignType3Type1
        | Unset
    ) = UNSET
    validation: None | PrecursorWindowStats | Unset = UNSET
    last_retest: None | PrecursorWindowStats | Unset = UNSET
    status_reason: None | str | Unset = UNSET
    operator_validated: bool | Unset = UNSET
    first_validated_at: datetime.datetime | None | Unset = UNSET
    last_tested_at: datetime.datetime | None | Unset = UNSET
    history: list[PrecursorHistoryItem] | Unset = UNSET
    created_at: datetime.datetime | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.precursor_lag_distribution_type_0 import (
            PrecursorLagDistributionType0,
        )
        from ..models.precursor_window_stats import PrecursorWindowStats
        from ..models.stat_interval import StatInterval

        id = self.id

        kind = self.kind.value

        antecedent = self.antecedent.to_dict()

        consequent = self.consequent.to_dict()

        spatial = self.spatial.to_dict()

        lag_min = self.lag_min

        lag_max = self.lag_max

        n_antecedent = self.n_antecedent

        discovery = self.discovery.to_dict()

        batch_id = self.batch_id

        batch_size = self.batch_size

        status = self.status.value

        detector = self.detector.to_dict()

        origin = self.origin.value

        description = self.description

        association_note = self.association_note

        caveats = []
        for caveats_item_data in self.caveats:
            caveats_item = caveats_item_data.to_dict()
            caveats.append(caveats_item)

        manifest_id = self.manifest_id

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        best_lag: None | str | Unset
        if isinstance(self.best_lag, Unset):
            best_lag = UNSET
        else:
            best_lag = self.best_lag

        lag_distribution: dict[str, Any] | None | Unset
        if isinstance(self.lag_distribution, Unset):
            lag_distribution = UNSET
        elif isinstance(self.lag_distribution, PrecursorLagDistributionType0):
            lag_distribution = self.lag_distribution.to_dict()
        else:
            lag_distribution = self.lag_distribution

        hit_rate: float | None | Unset
        if isinstance(self.hit_rate, Unset):
            hit_rate = UNSET
        else:
            hit_rate = self.hit_rate

        hit_rate_interval: dict[str, Any] | None | Unset
        if isinstance(self.hit_rate_interval, Unset):
            hit_rate_interval = UNSET
        elif isinstance(self.hit_rate_interval, StatInterval):
            hit_rate_interval = self.hit_rate_interval.to_dict()
        else:
            hit_rate_interval = self.hit_rate_interval

        base_rate: float | None | Unset
        if isinstance(self.base_rate, Unset):
            base_rate = UNSET
        else:
            base_rate = self.base_rate

        lift: float | None | Unset
        if isinstance(self.lift, Unset):
            lift = UNSET
        else:
            lift = self.lift

        correlation: float | None | Unset
        if isinstance(self.correlation, Unset):
            correlation = UNSET
        else:
            correlation = self.correlation

        correlation_sign: None | str | Unset
        if isinstance(self.correlation_sign, Unset):
            correlation_sign = UNSET
        elif (
            isinstance(self.correlation_sign, PrecursorCorrelationSignType1)
            or isinstance(self.correlation_sign, PrecursorCorrelationSignType2Type1)
            or isinstance(self.correlation_sign, PrecursorCorrelationSignType3Type1)
        ):
            correlation_sign = self.correlation_sign.value
        else:
            correlation_sign = self.correlation_sign

        validation: dict[str, Any] | None | Unset
        if isinstance(self.validation, Unset):
            validation = UNSET
        elif isinstance(self.validation, PrecursorWindowStats):
            validation = self.validation.to_dict()
        else:
            validation = self.validation

        last_retest: dict[str, Any] | None | Unset
        if isinstance(self.last_retest, Unset):
            last_retest = UNSET
        elif isinstance(self.last_retest, PrecursorWindowStats):
            last_retest = self.last_retest.to_dict()
        else:
            last_retest = self.last_retest

        status_reason: None | str | Unset
        if isinstance(self.status_reason, Unset):
            status_reason = UNSET
        else:
            status_reason = self.status_reason

        operator_validated = self.operator_validated

        first_validated_at: None | str | Unset
        if isinstance(self.first_validated_at, Unset):
            first_validated_at = UNSET
        elif isinstance(self.first_validated_at, datetime.datetime):
            first_validated_at = self.first_validated_at.isoformat()
        else:
            first_validated_at = self.first_validated_at

        last_tested_at: None | str | Unset
        if isinstance(self.last_tested_at, Unset):
            last_tested_at = UNSET
        elif isinstance(self.last_tested_at, datetime.datetime):
            last_tested_at = self.last_tested_at.isoformat()
        else:
            last_tested_at = self.last_tested_at

        history: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.history, Unset):
            history = []
            for history_item_data in self.history:
                history_item = history_item_data.to_dict()
                history.append(history_item)

        created_at: str | Unset = UNSET
        if not isinstance(self.created_at, Unset):
            created_at = self.created_at.isoformat()

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "kind": kind,
                "antecedent": antecedent,
                "consequent": consequent,
                "spatial": spatial,
                "lagMin": lag_min,
                "lagMax": lag_max,
                "nAntecedent": n_antecedent,
                "discovery": discovery,
                "batchId": batch_id,
                "batchSize": batch_size,
                "status": status,
                "detector": detector,
                "origin": origin,
                "description": description,
                "associationNote": association_note,
                "caveats": caveats,
                "manifestId": manifest_id,
            }
        )
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if best_lag is not UNSET:
            field_dict["bestLag"] = best_lag
        if lag_distribution is not UNSET:
            field_dict["lagDistribution"] = lag_distribution
        if hit_rate is not UNSET:
            field_dict["hitRate"] = hit_rate
        if hit_rate_interval is not UNSET:
            field_dict["hitRateInterval"] = hit_rate_interval
        if base_rate is not UNSET:
            field_dict["baseRate"] = base_rate
        if lift is not UNSET:
            field_dict["lift"] = lift
        if correlation is not UNSET:
            field_dict["correlation"] = correlation
        if correlation_sign is not UNSET:
            field_dict["correlationSign"] = correlation_sign
        if validation is not UNSET:
            field_dict["validation"] = validation
        if last_retest is not UNSET:
            field_dict["lastRetest"] = last_retest
        if status_reason is not UNSET:
            field_dict["statusReason"] = status_reason
        if operator_validated is not UNSET:
            field_dict["operatorValidated"] = operator_validated
        if first_validated_at is not UNSET:
            field_dict["firstValidatedAt"] = first_validated_at
        if last_tested_at is not UNSET:
            field_dict["lastTestedAt"] = last_tested_at
        if history is not UNSET:
            field_dict["history"] = history
        if created_at is not UNSET:
            field_dict["createdAt"] = created_at
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.detector_ref import DetectorRef
        from ..models.precursor_history_item import PrecursorHistoryItem
        from ..models.precursor_lag_distribution_type_0 import (
            PrecursorLagDistributionType0,
        )
        from ..models.precursor_side import PrecursorSide
        from ..models.precursor_spatial import PrecursorSpatial
        from ..models.precursor_window_stats import PrecursorWindowStats
        from ..models.stat_interval import StatInterval

        d = dict(src_dict)
        id = d.pop("id")

        kind = PrecursorKind(d.pop("kind"))

        antecedent = PrecursorSide.from_dict(d.pop("antecedent"))

        consequent = PrecursorSide.from_dict(d.pop("consequent"))

        spatial = PrecursorSpatial.from_dict(d.pop("spatial"))

        lag_min = d.pop("lagMin")

        lag_max = d.pop("lagMax")

        n_antecedent = d.pop("nAntecedent")

        discovery = PrecursorWindowStats.from_dict(d.pop("discovery"))

        batch_id = d.pop("batchId")

        batch_size = d.pop("batchSize")

        status = PrecursorStatus(d.pop("status"))

        detector = DetectorRef.from_dict(d.pop("detector"))

        origin = PrecursorOrigin(d.pop("origin"))

        description = d.pop("description")

        association_note = cast(
            Literal["association in stored data; not causation"],
            d.pop("associationNote"),
        )
        if association_note != "association in stored data; not causation":
            raise ValueError(
                f"associationNote must match const 'association in stored data; not causation', got '{association_note}'"
            )

        caveats = []
        _caveats = d.pop("caveats")
        for caveats_item_data in _caveats:
            caveats_item = Caveat.from_dict(caveats_item_data)

            caveats.append(caveats_item)

        manifest_id = d.pop("manifestId")

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        def _parse_best_lag(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        best_lag = _parse_best_lag(d.pop("bestLag", UNSET))

        def _parse_lag_distribution(
            data: object,
        ) -> None | PrecursorLagDistributionType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                lag_distribution_type_0 = PrecursorLagDistributionType0.from_dict(data)

                return lag_distribution_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PrecursorLagDistributionType0 | Unset, data)

        lag_distribution = _parse_lag_distribution(d.pop("lagDistribution", UNSET))

        def _parse_hit_rate(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        hit_rate = _parse_hit_rate(d.pop("hitRate", UNSET))

        def _parse_hit_rate_interval(data: object) -> None | StatInterval | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                hit_rate_interval_type_0 = StatInterval.from_dict(data)

                return hit_rate_interval_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | StatInterval | Unset, data)

        hit_rate_interval = _parse_hit_rate_interval(d.pop("hitRateInterval", UNSET))

        def _parse_base_rate(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        base_rate = _parse_base_rate(d.pop("baseRate", UNSET))

        def _parse_lift(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lift = _parse_lift(d.pop("lift", UNSET))

        def _parse_correlation(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        correlation = _parse_correlation(d.pop("correlation", UNSET))

        def _parse_correlation_sign(
            data: object,
        ) -> (
            None
            | PrecursorCorrelationSignType1
            | PrecursorCorrelationSignType2Type1
            | PrecursorCorrelationSignType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                correlation_sign_type_1 = PrecursorCorrelationSignType1(data)

                return correlation_sign_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                correlation_sign_type_2_type_1 = PrecursorCorrelationSignType2Type1(
                    data
                )

                return correlation_sign_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                correlation_sign_type_3_type_1 = PrecursorCorrelationSignType3Type1(
                    data
                )

                return correlation_sign_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | PrecursorCorrelationSignType1
                | PrecursorCorrelationSignType2Type1
                | PrecursorCorrelationSignType3Type1
                | Unset,
                data,
            )

        correlation_sign = _parse_correlation_sign(d.pop("correlationSign", UNSET))

        def _parse_validation(data: object) -> None | PrecursorWindowStats | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                validation_type_0 = PrecursorWindowStats.from_dict(data)

                return validation_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PrecursorWindowStats | Unset, data)

        validation = _parse_validation(d.pop("validation", UNSET))

        def _parse_last_retest(data: object) -> None | PrecursorWindowStats | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                last_retest_type_0 = PrecursorWindowStats.from_dict(data)

                return last_retest_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PrecursorWindowStats | Unset, data)

        last_retest = _parse_last_retest(d.pop("lastRetest", UNSET))

        def _parse_status_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        status_reason = _parse_status_reason(d.pop("statusReason", UNSET))

        operator_validated = d.pop("operatorValidated", UNSET)

        def _parse_first_validated_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                first_validated_at_type_0 = datetime.datetime.fromisoformat(data)

                return first_validated_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        first_validated_at = _parse_first_validated_at(d.pop("firstValidatedAt", UNSET))

        def _parse_last_tested_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_tested_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_tested_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_tested_at = _parse_last_tested_at(d.pop("lastTestedAt", UNSET))

        _history = d.pop("history", UNSET)
        history: list[PrecursorHistoryItem] | Unset = UNSET
        if _history is not UNSET:
            history = []
            for history_item_data in _history:
                history_item = PrecursorHistoryItem.from_dict(history_item_data)

                history.append(history_item)

        _created_at = d.pop("createdAt", UNSET)
        created_at: datetime.datetime | Unset
        if isinstance(_created_at, Unset):
            created_at = UNSET
        else:
            created_at = datetime.datetime.fromisoformat(_created_at)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        precursor = cls(
            id=id,
            kind=kind,
            antecedent=antecedent,
            consequent=consequent,
            spatial=spatial,
            lag_min=lag_min,
            lag_max=lag_max,
            n_antecedent=n_antecedent,
            discovery=discovery,
            batch_id=batch_id,
            batch_size=batch_size,
            status=status,
            detector=detector,
            origin=origin,
            description=description,
            association_note=association_note,
            caveats=caveats,
            manifest_id=manifest_id,
            region_codes=region_codes,
            best_lag=best_lag,
            lag_distribution=lag_distribution,
            hit_rate=hit_rate,
            hit_rate_interval=hit_rate_interval,
            base_rate=base_rate,
            lift=lift,
            correlation=correlation,
            correlation_sign=correlation_sign,
            validation=validation,
            last_retest=last_retest,
            status_reason=status_reason,
            operator_validated=operator_validated,
            first_validated_at=first_validated_at,
            last_tested_at=last_tested_at,
            history=history,
            created_at=created_at,
            updated_at=updated_at,
        )

        precursor.additional_properties = d
        return precursor

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
