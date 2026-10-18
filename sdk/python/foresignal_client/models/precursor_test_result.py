from __future__ import annotations

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

from ..models.precursor_test_result_verdict import PrecursorTestResultVerdict
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.backtest_caveat import BacktestCaveat
    from ..models.precursor_test_result_hit_rate_ci import PrecursorTestResultHitRateCi
    from ..models.precursor_test_result_lag import PrecursorTestResultLag
    from ..models.precursor_test_result_multiple_testing import (
        PrecursorTestResultMultipleTesting,
    )
    from ..models.precursor_test_result_reverse import PrecursorTestResultReverse


T = TypeVar("T", bound="PrecursorTestResult")


@_attrs_define
class PrecursorTestResult:
    """Descriptive statistics of an association in stored data; not causation and not a calibrated probability

    Attributes:
        tester_version (str):  Example: 1.0.0.
        n_antecedent (int):
        hits (int):
        verdict (PrecursorTestResultVerdict):
        statement (Literal['association in stored data; not causation']):
        caveats (list[BacktestCaveat]):
        n_consequent (int | Unset):
        hit_rate (float | None | Unset):
        hit_rate_ci (PrecursorTestResultHitRateCi | Unset): Wilson 95% interval
        lag (PrecursorTestResultLag | Unset): Lag distribution of hits (ISO-8601 durations)
        base_rate (float | None | Unset): Mean hit rate of 200 circular time-shifted copies
        lift (float | None | Unset):
        p_value (float | None | Unset): (1 + #shifted ≥ observed) ÷ 201
        permutations (Literal[200] | Unset):
        seed (str | Unset): Deterministic seed (hash of definition + range)
        reverse (PrecursorTestResultReverse | Unset): Same test with roles swapped
        co_occurrence_not_precedence (bool | Unset):
        multiple_testing (PrecursorTestResultMultipleTesting | Unset): Tests run on this instance in the last 30 days
    """

    tester_version: str
    n_antecedent: int
    hits: int
    verdict: PrecursorTestResultVerdict
    statement: Literal["association in stored data; not causation"]
    caveats: list[BacktestCaveat]
    n_consequent: int | Unset = UNSET
    hit_rate: float | None | Unset = UNSET
    hit_rate_ci: PrecursorTestResultHitRateCi | Unset = UNSET
    lag: PrecursorTestResultLag | Unset = UNSET
    base_rate: float | None | Unset = UNSET
    lift: float | None | Unset = UNSET
    p_value: float | None | Unset = UNSET
    permutations: Literal[200] | Unset = UNSET
    seed: str | Unset = UNSET
    reverse: PrecursorTestResultReverse | Unset = UNSET
    co_occurrence_not_precedence: bool | Unset = UNSET
    multiple_testing: PrecursorTestResultMultipleTesting | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        tester_version = self.tester_version

        n_antecedent = self.n_antecedent

        hits = self.hits

        verdict = self.verdict.value

        statement = self.statement

        caveats = []
        for caveats_item_data in self.caveats:
            caveats_item = caveats_item_data.to_dict()
            caveats.append(caveats_item)

        n_consequent = self.n_consequent

        hit_rate: float | None | Unset
        if isinstance(self.hit_rate, Unset):
            hit_rate = UNSET
        else:
            hit_rate = self.hit_rate

        hit_rate_ci: dict[str, Any] | Unset = UNSET
        if not isinstance(self.hit_rate_ci, Unset):
            hit_rate_ci = self.hit_rate_ci.to_dict()

        lag: dict[str, Any] | Unset = UNSET
        if not isinstance(self.lag, Unset):
            lag = self.lag.to_dict()

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

        p_value: float | None | Unset
        if isinstance(self.p_value, Unset):
            p_value = UNSET
        else:
            p_value = self.p_value

        permutations = self.permutations

        seed = self.seed

        reverse: dict[str, Any] | Unset = UNSET
        if not isinstance(self.reverse, Unset):
            reverse = self.reverse.to_dict()

        co_occurrence_not_precedence = self.co_occurrence_not_precedence

        multiple_testing: dict[str, Any] | Unset = UNSET
        if not isinstance(self.multiple_testing, Unset):
            multiple_testing = self.multiple_testing.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "testerVersion": tester_version,
                "nAntecedent": n_antecedent,
                "hits": hits,
                "verdict": verdict,
                "statement": statement,
                "caveats": caveats,
            }
        )
        if n_consequent is not UNSET:
            field_dict["nConsequent"] = n_consequent
        if hit_rate is not UNSET:
            field_dict["hitRate"] = hit_rate
        if hit_rate_ci is not UNSET:
            field_dict["hitRateCi"] = hit_rate_ci
        if lag is not UNSET:
            field_dict["lag"] = lag
        if base_rate is not UNSET:
            field_dict["baseRate"] = base_rate
        if lift is not UNSET:
            field_dict["lift"] = lift
        if p_value is not UNSET:
            field_dict["pValue"] = p_value
        if permutations is not UNSET:
            field_dict["permutations"] = permutations
        if seed is not UNSET:
            field_dict["seed"] = seed
        if reverse is not UNSET:
            field_dict["reverse"] = reverse
        if co_occurrence_not_precedence is not UNSET:
            field_dict["coOccurrenceNotPrecedence"] = co_occurrence_not_precedence
        if multiple_testing is not UNSET:
            field_dict["multipleTesting"] = multiple_testing

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.backtest_caveat import BacktestCaveat
        from ..models.precursor_test_result_hit_rate_ci import (
            PrecursorTestResultHitRateCi,
        )
        from ..models.precursor_test_result_lag import PrecursorTestResultLag
        from ..models.precursor_test_result_multiple_testing import (
            PrecursorTestResultMultipleTesting,
        )
        from ..models.precursor_test_result_reverse import PrecursorTestResultReverse

        d = dict(src_dict)
        tester_version = d.pop("testerVersion")

        n_antecedent = d.pop("nAntecedent")

        hits = d.pop("hits")

        verdict = PrecursorTestResultVerdict(d.pop("verdict"))

        statement = cast(
            Literal["association in stored data; not causation"], d.pop("statement")
        )
        if statement != "association in stored data; not causation":
            raise ValueError(
                f"statement must match const 'association in stored data; not causation', got '{statement}'"
            )

        caveats = []
        _caveats = d.pop("caveats")
        for caveats_item_data in _caveats:
            caveats_item = BacktestCaveat.from_dict(caveats_item_data)

            caveats.append(caveats_item)

        n_consequent = d.pop("nConsequent", UNSET)

        def _parse_hit_rate(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        hit_rate = _parse_hit_rate(d.pop("hitRate", UNSET))

        _hit_rate_ci = d.pop("hitRateCi", UNSET)
        hit_rate_ci: PrecursorTestResultHitRateCi | Unset
        if isinstance(_hit_rate_ci, Unset):
            hit_rate_ci = UNSET
        else:
            hit_rate_ci = PrecursorTestResultHitRateCi.from_dict(_hit_rate_ci)

        _lag = d.pop("lag", UNSET)
        lag: PrecursorTestResultLag | Unset
        if isinstance(_lag, Unset):
            lag = UNSET
        else:
            lag = PrecursorTestResultLag.from_dict(_lag)

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

        def _parse_p_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p_value = _parse_p_value(d.pop("pValue", UNSET))

        permutations = cast(Literal[200] | Unset, d.pop("permutations", UNSET))
        if permutations != 200 and not isinstance(permutations, Unset):
            raise ValueError(f"permutations must match const 200, got '{permutations}'")

        seed = d.pop("seed", UNSET)

        _reverse = d.pop("reverse", UNSET)
        reverse: PrecursorTestResultReverse | Unset
        if isinstance(_reverse, Unset):
            reverse = UNSET
        else:
            reverse = PrecursorTestResultReverse.from_dict(_reverse)

        co_occurrence_not_precedence = d.pop("coOccurrenceNotPrecedence", UNSET)

        _multiple_testing = d.pop("multipleTesting", UNSET)
        multiple_testing: PrecursorTestResultMultipleTesting | Unset
        if isinstance(_multiple_testing, Unset):
            multiple_testing = UNSET
        else:
            multiple_testing = PrecursorTestResultMultipleTesting.from_dict(
                _multiple_testing
            )

        precursor_test_result = cls(
            tester_version=tester_version,
            n_antecedent=n_antecedent,
            hits=hits,
            verdict=verdict,
            statement=statement,
            caveats=caveats,
            n_consequent=n_consequent,
            hit_rate=hit_rate,
            hit_rate_ci=hit_rate_ci,
            lag=lag,
            base_rate=base_rate,
            lift=lift,
            p_value=p_value,
            permutations=permutations,
            seed=seed,
            reverse=reverse,
            co_occurrence_not_precedence=co_occurrence_not_precedence,
            multiple_testing=multiple_testing,
        )

        precursor_test_result.additional_properties = d
        return precursor_test_result

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
