from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.outlet_consensus_bucket_type_1 import OutletConsensusBucketType1
from ..models.outlet_consensus_bucket_type_2_type_1 import (
    OutletConsensusBucketType2Type1,
)
from ..models.outlet_consensus_bucket_type_3_type_1 import (
    OutletConsensusBucketType3Type1,
)
from ..models.outlet_consensus_dimension_type_0 import OutletConsensusDimensionType0
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.outlet_consensus_providers_item import OutletConsensusProvidersItem


T = TypeVar("T", bound="OutletConsensus")


@_attrs_define
class OutletConsensus:
    """Consensus per outlet and dimension over enabled providers, with the per-provider breakdown

    Attributes:
        outlet_id (str):
        dimension (OutletConsensusDimensionType0 | str): Rating dimension; provider axes that are not left–right are
            recorded as `{provider}:{axis}`, never forced into lean
        n_providers (int):
        providers (list[OutletConsensusProvidersItem]):
        median (float | None | Unset):
        disagreement (float | None | Unset): max − min of valueNorm
        bucket (None | OutletConsensusBucketType1 | OutletConsensusBucketType2Type1 | OutletConsensusBucketType3Type1 |
            Unset): Lean only: |median| < 0.75 is center
        computed_at (datetime.datetime | Unset):
    """

    outlet_id: str
    dimension: OutletConsensusDimensionType0 | str
    n_providers: int
    providers: list[OutletConsensusProvidersItem]
    median: float | None | Unset = UNSET
    disagreement: float | None | Unset = UNSET
    bucket: (
        None
        | OutletConsensusBucketType1
        | OutletConsensusBucketType2Type1
        | OutletConsensusBucketType3Type1
        | Unset
    ) = UNSET
    computed_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        outlet_id = self.outlet_id

        dimension: str
        if isinstance(self.dimension, OutletConsensusDimensionType0):
            dimension = self.dimension.value
        else:
            dimension = self.dimension

        n_providers = self.n_providers

        providers = []
        for providers_item_data in self.providers:
            providers_item = providers_item_data.to_dict()
            providers.append(providers_item)

        median: float | None | Unset
        if isinstance(self.median, Unset):
            median = UNSET
        else:
            median = self.median

        disagreement: float | None | Unset
        if isinstance(self.disagreement, Unset):
            disagreement = UNSET
        else:
            disagreement = self.disagreement

        bucket: None | str | Unset
        if isinstance(self.bucket, Unset):
            bucket = UNSET
        elif (
            isinstance(self.bucket, OutletConsensusBucketType1)
            or isinstance(self.bucket, OutletConsensusBucketType2Type1)
            or isinstance(self.bucket, OutletConsensusBucketType3Type1)
        ):
            bucket = self.bucket.value
        else:
            bucket = self.bucket

        computed_at: str | Unset = UNSET
        if not isinstance(self.computed_at, Unset):
            computed_at = self.computed_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "outletId": outlet_id,
                "dimension": dimension,
                "nProviders": n_providers,
                "providers": providers,
            }
        )
        if median is not UNSET:
            field_dict["median"] = median
        if disagreement is not UNSET:
            field_dict["disagreement"] = disagreement
        if bucket is not UNSET:
            field_dict["bucket"] = bucket
        if computed_at is not UNSET:
            field_dict["computedAt"] = computed_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.outlet_consensus_providers_item import (
            OutletConsensusProvidersItem,
        )

        d = dict(src_dict)
        outlet_id = d.pop("outletId")

        def _parse_dimension(data: object) -> OutletConsensusDimensionType0 | str:
            try:
                if not isinstance(data, str):
                    raise TypeError()
                dimension_type_0 = OutletConsensusDimensionType0(data)

                return dimension_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(OutletConsensusDimensionType0 | str, data)

        dimension = _parse_dimension(d.pop("dimension"))

        n_providers = d.pop("nProviders")

        providers = []
        _providers = d.pop("providers")
        for providers_item_data in _providers:
            providers_item = OutletConsensusProvidersItem.from_dict(providers_item_data)

            providers.append(providers_item)

        def _parse_median(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        median = _parse_median(d.pop("median", UNSET))

        def _parse_disagreement(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        disagreement = _parse_disagreement(d.pop("disagreement", UNSET))

        def _parse_bucket(
            data: object,
        ) -> (
            None
            | OutletConsensusBucketType1
            | OutletConsensusBucketType2Type1
            | OutletConsensusBucketType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                bucket_type_1 = OutletConsensusBucketType1(data)

                return bucket_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                bucket_type_2_type_1 = OutletConsensusBucketType2Type1(data)

                return bucket_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                bucket_type_3_type_1 = OutletConsensusBucketType3Type1(data)

                return bucket_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | OutletConsensusBucketType1
                | OutletConsensusBucketType2Type1
                | OutletConsensusBucketType3Type1
                | Unset,
                data,
            )

        bucket = _parse_bucket(d.pop("bucket", UNSET))

        _computed_at = d.pop("computedAt", UNSET)
        computed_at: datetime.datetime | Unset
        if isinstance(_computed_at, Unset):
            computed_at = UNSET
        else:
            computed_at = datetime.datetime.fromisoformat(_computed_at)

        outlet_consensus = cls(
            outlet_id=outlet_id,
            dimension=dimension,
            n_providers=n_providers,
            providers=providers,
            median=median,
            disagreement=disagreement,
            bucket=bucket,
            computed_at=computed_at,
        )

        outlet_consensus.additional_properties = d
        return outlet_consensus

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
