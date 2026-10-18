from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.prob_severe_payload_motion import ProbSeverePayloadMotion


T = TypeVar("T", bound="ProbSeverePayload")


@_attrs_define
class ProbSeverePayload:
    """Payload of `probsevere-object` records (mrms-probsevere; automated guidance, not a warning)

    Attributes:
        prob_severe (int):
        prob_tor (int):
        prob_hail (int):
        prob_wind (int):
        label (str):
        automated (bool):
        not_a_warning (bool):
        mesh_in (float | Unset): 0.25 in steps
        comp_ref_dbz (float | Unset):
        vil (float | Unset):
        echo_top_50_kft (float | Unset):
        flash_rate (float | Unset):
        mu_cape (float | Unset):
        ml_cape (float | Unset):
        eb_shear_kt (float | Unset):
        srh01km (float | Unset):
        motion (ProbSeverePayloadMotion | Unset): Storm motion
    """

    prob_severe: int
    prob_tor: int
    prob_hail: int
    prob_wind: int
    label: str
    automated: bool
    not_a_warning: bool
    mesh_in: float | Unset = UNSET
    comp_ref_dbz: float | Unset = UNSET
    vil: float | Unset = UNSET
    echo_top_50_kft: float | Unset = UNSET
    flash_rate: float | Unset = UNSET
    mu_cape: float | Unset = UNSET
    ml_cape: float | Unset = UNSET
    eb_shear_kt: float | Unset = UNSET
    srh01km: float | Unset = UNSET
    motion: ProbSeverePayloadMotion | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        prob_severe = self.prob_severe

        prob_tor = self.prob_tor

        prob_hail = self.prob_hail

        prob_wind = self.prob_wind

        label = self.label

        automated = self.automated

        not_a_warning = self.not_a_warning

        mesh_in = self.mesh_in

        comp_ref_dbz = self.comp_ref_dbz

        vil = self.vil

        echo_top_50_kft = self.echo_top_50_kft

        flash_rate = self.flash_rate

        mu_cape = self.mu_cape

        ml_cape = self.ml_cape

        eb_shear_kt = self.eb_shear_kt

        srh01km = self.srh01km

        motion: dict[str, Any] | Unset = UNSET
        if not isinstance(self.motion, Unset):
            motion = self.motion.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "probSevere": prob_severe,
                "probTor": prob_tor,
                "probHail": prob_hail,
                "probWind": prob_wind,
                "label": label,
                "automated": automated,
                "notAWarning": not_a_warning,
            }
        )
        if mesh_in is not UNSET:
            field_dict["meshIn"] = mesh_in
        if comp_ref_dbz is not UNSET:
            field_dict["compRefDbz"] = comp_ref_dbz
        if vil is not UNSET:
            field_dict["vil"] = vil
        if echo_top_50_kft is not UNSET:
            field_dict["echoTop50Kft"] = echo_top_50_kft
        if flash_rate is not UNSET:
            field_dict["flashRate"] = flash_rate
        if mu_cape is not UNSET:
            field_dict["muCape"] = mu_cape
        if ml_cape is not UNSET:
            field_dict["mlCape"] = ml_cape
        if eb_shear_kt is not UNSET:
            field_dict["ebShearKt"] = eb_shear_kt
        if srh01km is not UNSET:
            field_dict["srh01km"] = srh01km
        if motion is not UNSET:
            field_dict["motion"] = motion

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.prob_severe_payload_motion import ProbSeverePayloadMotion

        d = dict(src_dict)
        prob_severe = d.pop("probSevere")

        prob_tor = d.pop("probTor")

        prob_hail = d.pop("probHail")

        prob_wind = d.pop("probWind")

        label = d.pop("label")

        automated = d.pop("automated")

        not_a_warning = d.pop("notAWarning")

        mesh_in = d.pop("meshIn", UNSET)

        comp_ref_dbz = d.pop("compRefDbz", UNSET)

        vil = d.pop("vil", UNSET)

        echo_top_50_kft = d.pop("echoTop50Kft", UNSET)

        flash_rate = d.pop("flashRate", UNSET)

        mu_cape = d.pop("muCape", UNSET)

        ml_cape = d.pop("mlCape", UNSET)

        eb_shear_kt = d.pop("ebShearKt", UNSET)

        srh01km = d.pop("srh01km", UNSET)

        _motion = d.pop("motion", UNSET)
        motion: ProbSeverePayloadMotion | Unset
        if isinstance(_motion, Unset):
            motion = UNSET
        else:
            motion = ProbSeverePayloadMotion.from_dict(_motion)

        prob_severe_payload = cls(
            prob_severe=prob_severe,
            prob_tor=prob_tor,
            prob_hail=prob_hail,
            prob_wind=prob_wind,
            label=label,
            automated=automated,
            not_a_warning=not_a_warning,
            mesh_in=mesh_in,
            comp_ref_dbz=comp_ref_dbz,
            vil=vil,
            echo_top_50_kft=echo_top_50_kft,
            flash_rate=flash_rate,
            mu_cape=mu_cape,
            ml_cape=ml_cape,
            eb_shear_kt=eb_shear_kt,
            srh01km=srh01km,
            motion=motion,
        )

        prob_severe_payload.additional_properties = d
        return prob_severe_payload

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
