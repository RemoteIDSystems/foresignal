from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define

from ..models.precursor_test_input_spatial_type_0 import PrecursorTestInputSpatialType0
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.precursor_test_input_antecedent_type_0 import (
        PrecursorTestInputAntecedentType0,
    )
    from ..models.precursor_test_input_antecedent_type_1 import (
        PrecursorTestInputAntecedentType1,
    )
    from ..models.precursor_test_input_antecedent_type_2 import (
        PrecursorTestInputAntecedentType2,
    )
    from ..models.precursor_test_input_antecedent_type_3 import (
        PrecursorTestInputAntecedentType3,
    )
    from ..models.precursor_test_input_consequent_type_0 import (
        PrecursorTestInputConsequentType0,
    )
    from ..models.precursor_test_input_consequent_type_1 import (
        PrecursorTestInputConsequentType1,
    )
    from ..models.precursor_test_input_consequent_type_2 import (
        PrecursorTestInputConsequentType2,
    )
    from ..models.precursor_test_input_consequent_type_3 import (
        PrecursorTestInputConsequentType3,
    )
    from ..models.precursor_test_input_spatial_type_1 import (
        PrecursorTestInputSpatialType1,
    )
    from ..models.precursor_test_input_spatial_type_2 import (
        PrecursorTestInputSpatialType2,
    )


T = TypeVar("T", bound="PrecursorTestInput")


@_attrs_define
class PrecursorTestInput:
    """A precursor test definition: does X tend to precede Y within T?

    Attributes:
        name (str):
        antecedent (PrecursorTestInputAntecedentType0 | PrecursorTestInputAntecedentType1 |
            PrecursorTestInputAntecedentType2 | PrecursorTestInputAntecedentType3): Occurrence selector
        consequent (PrecursorTestInputConsequentType0 | PrecursorTestInputConsequentType1 |
            PrecursorTestInputConsequentType2 | PrecursorTestInputConsequentType3): Occurrence selector
        max_lag (str): Maximum lag, ≤ 30d. ISO-8601 duration or shorthand (30m, 6h, 7d). Example: 6h.
        spatial (PrecursorTestInputSpatialType0 | PrecursorTestInputSpatialType1 | PrecursorTestInputSpatialType2):
            `sameEntity` | `{withinKm: n}` | `{sameRegion: country|admin1}` | `none`
        statement (str | Unset): Plain-language statement Example: GPS jamming precedes AIS gaps within 6 h within 100
            km.
        min_lag (str | Unset): Minimum lag (default 0). ISO-8601 duration or shorthand (30m, 6h, 7d). Example: 0s.
        dedup_window (str | Unset): Antecedent occurrences closer than this merge (default maxLag). ISO-8601 duration or
            shorthand (30m, 6h, 7d).
    """

    name: str
    antecedent: (
        PrecursorTestInputAntecedentType0
        | PrecursorTestInputAntecedentType1
        | PrecursorTestInputAntecedentType2
        | PrecursorTestInputAntecedentType3
    )
    consequent: (
        PrecursorTestInputConsequentType0
        | PrecursorTestInputConsequentType1
        | PrecursorTestInputConsequentType2
        | PrecursorTestInputConsequentType3
    )
    max_lag: str
    spatial: (
        PrecursorTestInputSpatialType0
        | PrecursorTestInputSpatialType1
        | PrecursorTestInputSpatialType2
    )
    statement: str | Unset = UNSET
    min_lag: str | Unset = UNSET
    dedup_window: str | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        from ..models.precursor_test_input_antecedent_type_0 import (
            PrecursorTestInputAntecedentType0,
        )
        from ..models.precursor_test_input_antecedent_type_1 import (
            PrecursorTestInputAntecedentType1,
        )
        from ..models.precursor_test_input_antecedent_type_2 import (
            PrecursorTestInputAntecedentType2,
        )
        from ..models.precursor_test_input_consequent_type_0 import (
            PrecursorTestInputConsequentType0,
        )
        from ..models.precursor_test_input_consequent_type_1 import (
            PrecursorTestInputConsequentType1,
        )
        from ..models.precursor_test_input_consequent_type_2 import (
            PrecursorTestInputConsequentType2,
        )
        from ..models.precursor_test_input_spatial_type_1 import (
            PrecursorTestInputSpatialType1,
        )

        name = self.name

        antecedent: dict[str, Any]
        if (
            isinstance(self.antecedent, PrecursorTestInputAntecedentType0)
            or isinstance(self.antecedent, PrecursorTestInputAntecedentType1)
            or isinstance(self.antecedent, PrecursorTestInputAntecedentType2)
        ):
            antecedent = self.antecedent.to_dict()
        else:
            antecedent = self.antecedent.to_dict()

        consequent: dict[str, Any]
        if (
            isinstance(self.consequent, PrecursorTestInputConsequentType0)
            or isinstance(self.consequent, PrecursorTestInputConsequentType1)
            or isinstance(self.consequent, PrecursorTestInputConsequentType2)
        ):
            consequent = self.consequent.to_dict()
        else:
            consequent = self.consequent.to_dict()

        max_lag = self.max_lag

        spatial: dict[str, Any] | str
        if isinstance(self.spatial, PrecursorTestInputSpatialType0):
            spatial = self.spatial.value
        elif isinstance(self.spatial, PrecursorTestInputSpatialType1):
            spatial = self.spatial.to_dict()
        else:
            spatial = self.spatial.to_dict()

        statement = self.statement

        min_lag = self.min_lag

        dedup_window = self.dedup_window

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "name": name,
                "antecedent": antecedent,
                "consequent": consequent,
                "maxLag": max_lag,
                "spatial": spatial,
            }
        )
        if statement is not UNSET:
            field_dict["statement"] = statement
        if min_lag is not UNSET:
            field_dict["minLag"] = min_lag
        if dedup_window is not UNSET:
            field_dict["dedupWindow"] = dedup_window

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.precursor_test_input_antecedent_type_0 import (
            PrecursorTestInputAntecedentType0,
        )
        from ..models.precursor_test_input_antecedent_type_1 import (
            PrecursorTestInputAntecedentType1,
        )
        from ..models.precursor_test_input_antecedent_type_2 import (
            PrecursorTestInputAntecedentType2,
        )
        from ..models.precursor_test_input_antecedent_type_3 import (
            PrecursorTestInputAntecedentType3,
        )
        from ..models.precursor_test_input_consequent_type_0 import (
            PrecursorTestInputConsequentType0,
        )
        from ..models.precursor_test_input_consequent_type_1 import (
            PrecursorTestInputConsequentType1,
        )
        from ..models.precursor_test_input_consequent_type_2 import (
            PrecursorTestInputConsequentType2,
        )
        from ..models.precursor_test_input_consequent_type_3 import (
            PrecursorTestInputConsequentType3,
        )
        from ..models.precursor_test_input_spatial_type_1 import (
            PrecursorTestInputSpatialType1,
        )
        from ..models.precursor_test_input_spatial_type_2 import (
            PrecursorTestInputSpatialType2,
        )

        d = dict(src_dict)
        name = d.pop("name")

        def _parse_antecedent(
            data: object,
        ) -> (
            PrecursorTestInputAntecedentType0
            | PrecursorTestInputAntecedentType1
            | PrecursorTestInputAntecedentType2
            | PrecursorTestInputAntecedentType3
        ):
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                antecedent_type_0 = PrecursorTestInputAntecedentType0.from_dict(data)

                return antecedent_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                antecedent_type_1 = PrecursorTestInputAntecedentType1.from_dict(data)

                return antecedent_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                antecedent_type_2 = PrecursorTestInputAntecedentType2.from_dict(data)

                return antecedent_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            antecedent_type_3 = PrecursorTestInputAntecedentType3.from_dict(data)

            return antecedent_type_3

        antecedent = _parse_antecedent(d.pop("antecedent"))

        def _parse_consequent(
            data: object,
        ) -> (
            PrecursorTestInputConsequentType0
            | PrecursorTestInputConsequentType1
            | PrecursorTestInputConsequentType2
            | PrecursorTestInputConsequentType3
        ):
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                consequent_type_0 = PrecursorTestInputConsequentType0.from_dict(data)

                return consequent_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                consequent_type_1 = PrecursorTestInputConsequentType1.from_dict(data)

                return consequent_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                consequent_type_2 = PrecursorTestInputConsequentType2.from_dict(data)

                return consequent_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            consequent_type_3 = PrecursorTestInputConsequentType3.from_dict(data)

            return consequent_type_3

        consequent = _parse_consequent(d.pop("consequent"))

        max_lag = d.pop("maxLag")

        def _parse_spatial(
            data: object,
        ) -> (
            PrecursorTestInputSpatialType0
            | PrecursorTestInputSpatialType1
            | PrecursorTestInputSpatialType2
        ):
            try:
                if not isinstance(data, str):
                    raise TypeError()
                spatial_type_0 = PrecursorTestInputSpatialType0(data)

                return spatial_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                spatial_type_1 = PrecursorTestInputSpatialType1.from_dict(data)

                return spatial_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            spatial_type_2 = PrecursorTestInputSpatialType2.from_dict(data)

            return spatial_type_2

        spatial = _parse_spatial(d.pop("spatial"))

        statement = d.pop("statement", UNSET)

        min_lag = d.pop("minLag", UNSET)

        dedup_window = d.pop("dedupWindow", UNSET)

        precursor_test_input = cls(
            name=name,
            antecedent=antecedent,
            consequent=consequent,
            max_lag=max_lag,
            spatial=spatial,
            statement=statement,
            min_lag=min_lag,
            dedup_window=dedup_window,
        )

        return precursor_test_input
