import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.stix_bundle import StixBundle
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    kind: list[str] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/export/stix",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | StixBundle | None:
    if response.status_code == 200:
        response_200 = StixBundle.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | StixBundle]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    kind: list[str] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> Response[Problem | StixBundle]:
    """Export a STIX 2.1 bundle

     `cisa-kev`/`nvd-cves` → `vulnerability`, abuse.ch IOCs → `indicator` (pattern from the IOC type),
    plus a Foresignal `identity`; deterministic UUIDv5 ids from record refs. Every record passes
    `EgressPolicy` on the EXPORT path (private sources, `LOCAL_ONLY` egress and license terms); refused
    records are counted in `Foresignal-Licence-Filtered`. Kinds: `vulnerability`, `malware-url`,
    `malware-c2` (default all three).

    **Scope:** `agent`.

    Args:
        kind (list[str] | Unset):
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StixBundle]
    """

    kwargs = _get_kwargs(
        kind=kind,
        since=since,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    kind: list[str] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> Problem | StixBundle | None:
    """Export a STIX 2.1 bundle

     `cisa-kev`/`nvd-cves` → `vulnerability`, abuse.ch IOCs → `indicator` (pattern from the IOC type),
    plus a Foresignal `identity`; deterministic UUIDv5 ids from record refs. Every record passes
    `EgressPolicy` on the EXPORT path (private sources, `LOCAL_ONLY` egress and license terms); refused
    records are counted in `Foresignal-Licence-Filtered`. Kinds: `vulnerability`, `malware-url`,
    `malware-c2` (default all three).

    **Scope:** `agent`.

    Args:
        kind (list[str] | Unset):
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StixBundle
    """

    return sync_detailed(
        client=client,
        kind=kind,
        since=since,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    kind: list[str] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> Response[Problem | StixBundle]:
    """Export a STIX 2.1 bundle

     `cisa-kev`/`nvd-cves` → `vulnerability`, abuse.ch IOCs → `indicator` (pattern from the IOC type),
    plus a Foresignal `identity`; deterministic UUIDv5 ids from record refs. Every record passes
    `EgressPolicy` on the EXPORT path (private sources, `LOCAL_ONLY` egress and license terms); refused
    records are counted in `Foresignal-Licence-Filtered`. Kinds: `vulnerability`, `malware-url`,
    `malware-c2` (default all three).

    **Scope:** `agent`.

    Args:
        kind (list[str] | Unset):
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StixBundle]
    """

    kwargs = _get_kwargs(
        kind=kind,
        since=since,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    kind: list[str] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> Problem | StixBundle | None:
    """Export a STIX 2.1 bundle

     `cisa-kev`/`nvd-cves` → `vulnerability`, abuse.ch IOCs → `indicator` (pattern from the IOC type),
    plus a Foresignal `identity`; deterministic UUIDv5 ids from record refs. Every record passes
    `EgressPolicy` on the EXPORT path (private sources, `LOCAL_ONLY` egress and license terms); refused
    records are counted in `Foresignal-Licence-Filtered`. Kinds: `vulnerability`, `malware-url`,
    `malware-c2` (default all three).

    **Scope:** `agent`.

    Args:
        kind (list[str] | Unset):
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StixBundle
    """

    return (
        await asyncio_detailed(
            client=client,
            kind=kind,
            since=since,
        )
    ).parsed
