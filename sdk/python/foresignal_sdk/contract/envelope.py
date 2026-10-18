"""The object envelope (docs/Client-Contract.md section 3), resolve results and contract versions.

Written by hand as ``TypedDict``s so the contract helpers do not depend on the generated client.
"""
from __future__ import annotations

from typing import Any, NotRequired, TypedDict

#: The context schema version this SDK writes and validates.
CONTEXT_SCHEMA_VERSION = 1
#: The deep link payload version this SDK encodes and decodes.
DEEP_LINK_VERSION = 1
#: The contract major version this SDK was built for (within /v1 changes are additive).
CONTRACT_MAJOR_VERSION = 1


class ObjectAction(TypedDict, total=False):
    """An action the calling token may take on an object."""

    id: str
    label: str
    method: str
    href: str
    scope: str


class ObjectEnvelope(TypedDict):
    """The uniform summary of any Foresignal object."""

    ref: str
    type: str
    title: str
    summary: NotRequired[str | None]
    textOrigin: NotRequired[str]
    status: NotRequired[str | None]
    severity: NotRequired[str | None]
    time: NotRequired[dict[str, str | None]]
    geometry: NotRequired[dict[str, Any] | None]
    bbox: NotRequired[list[float] | None]
    private: NotRequired[bool]
    tlp: NotRequired[str | None]
    tags: NotRequired[list[str]]
    provenance: NotRequired[dict[str, Any]]
    links: NotRequired[dict[str, str]]
    actions: NotRequired[list[ObjectAction]]


class ResolveResult(TypedDict):
    """Envelopes for the resolvable refs, and the refs that are missing or invisible."""

    items: list[ObjectEnvelope]
    missing: list[str]
    asOf: NotRequired[str | None]


class ContractInfo(TypedDict):
    """The versions in force (``GET /v1/contract``)."""

    contractVersion: str
    objectTypes: list[str]
    relationTypes: list[str]
    contextSchemaVersion: int
    deepLinkVersion: int


def contract_problems(contract: ContractInfo | dict[str, Any]) -> list[str]:
    """The reasons a server's contract is not one this SDK understands (empty when compatible)."""
    problems: list[str] = []
    version = str(contract.get("contractVersion", ""))
    try:
        major = int(version.split(".")[0])
    except ValueError:
        major = -1
    if major != CONTRACT_MAJOR_VERSION:
        problems.append(f"contract version {version} (SDK supports {CONTRACT_MAJOR_VERSION}.x)")
    if contract.get("contextSchemaVersion") != CONTEXT_SCHEMA_VERSION:
        problems.append(f"context schema version {contract.get('contextSchemaVersion')} "
                        f"(SDK supports {CONTEXT_SCHEMA_VERSION})")
    if contract.get("deepLinkVersion") != DEEP_LINK_VERSION:
        problems.append(f"deep link version {contract.get('deepLinkVersion')} (SDK supports {DEEP_LINK_VERSION})")
    return problems
