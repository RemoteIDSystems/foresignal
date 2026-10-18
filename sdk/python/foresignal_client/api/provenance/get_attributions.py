from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.attribution_summary import AttributionSummary
from ...models.get_attributions_licence_policy_item import (
    GetAttributionsLicencePolicyItem,
)
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    licence_policy: list[GetAttributionsLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_licence_policy, Unset):
        headers["Foresignal-Licence-Policy"] = foresignal_licence_policy

    params: dict[str, Any] = {}

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

    json_licence_policy: list[str] | Unset = UNSET
    if not isinstance(licence_policy, Unset):
        json_licence_policy = []
        for licence_policy_item_data in licence_policy:
            licence_policy_item = licence_policy_item_data.value
            json_licence_policy.append(licence_policy_item)

    params["licencePolicy"] = json_licence_policy

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/attributions",
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AttributionSummary | Problem | None:
    if response.status_code == 200:
        response_200 = AttributionSummary.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[AttributionSummary | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    licence_policy: list[GetAttributionsLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[AttributionSummary | Problem]:
    """Aggregated attributions for a scope

     Attribution aggregate (one entry per source and licence, with record counts) for the current records
    matching `source` and `kind`, for SSE clients and map footers. Honours the licence policy and token
    restrictions.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        licence_policy (list[GetAttributionsLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AttributionSummary | Problem]
    """

    kwargs = _get_kwargs(
        source=source,
        kind=kind,
        licence_policy=licence_policy,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    licence_policy: list[GetAttributionsLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> AttributionSummary | Problem | None:
    """Aggregated attributions for a scope

     Attribution aggregate (one entry per source and licence, with record counts) for the current records
    matching `source` and `kind`, for SSE clients and map footers. Honours the licence policy and token
    restrictions.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        licence_policy (list[GetAttributionsLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AttributionSummary | Problem
    """

    return sync_detailed(
        client=client,
        source=source,
        kind=kind,
        licence_policy=licence_policy,
        foresignal_licence_policy=foresignal_licence_policy,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    licence_policy: list[GetAttributionsLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[AttributionSummary | Problem]:
    """Aggregated attributions for a scope

     Attribution aggregate (one entry per source and licence, with record counts) for the current records
    matching `source` and `kind`, for SSE clients and map footers. Honours the licence policy and token
    restrictions.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        licence_policy (list[GetAttributionsLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AttributionSummary | Problem]
    """

    kwargs = _get_kwargs(
        source=source,
        kind=kind,
        licence_policy=licence_policy,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    licence_policy: list[GetAttributionsLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> AttributionSummary | Problem | None:
    """Aggregated attributions for a scope

     Attribution aggregate (one entry per source and licence, with record counts) for the current records
    matching `source` and `kind`, for SSE clients and map footers. Honours the licence policy and token
    restrictions.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        licence_policy (list[GetAttributionsLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AttributionSummary | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            source=source,
            kind=kind,
            licence_policy=licence_policy,
            foresignal_licence_policy=foresignal_licence_policy,
        )
    ).parsed
