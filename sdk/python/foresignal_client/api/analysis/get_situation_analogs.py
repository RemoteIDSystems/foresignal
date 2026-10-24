from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.analog_result import AnalogResult
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    situation_id: str,
    *,
    k: int | Unset = 10,
    outcome: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["k"] = k

    params["outcome"] = outcome

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/situations/{situation_id}/analogs".format(
            situation_id=quote(str(situation_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AnalogResult | Problem | None:
    if response.status_code == 200:
        response_200 = AnalogResult.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[AnalogResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    k: int | Unset = 10,
    outcome: str | Unset = UNSET,
) -> Response[AnalogResult | Problem]:
    """Find historical analogs of a situation

     Returns the top k past situations by fused feature and embedding similarity, excluding time overlap
    and the situation's own merges, with outcomes, the count found and, for `outcome`, a distribution
    shrunk toward the base rate. Without embeddings, feature similarity only (labeled).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        k (int | Unset):  Default: 10.
        outcome (str | Unset): Target outcome: `critical` (situation reaches critical) or
            `{family}/P{n}D` (a family within n days), e.g. `conflict:battles/P30D`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalogResult | Problem]
    """

    kwargs = _get_kwargs(
        situation_id=situation_id,
        k=k,
        outcome=outcome,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    k: int | Unset = 10,
    outcome: str | Unset = UNSET,
) -> AnalogResult | Problem | None:
    """Find historical analogs of a situation

     Returns the top k past situations by fused feature and embedding similarity, excluding time overlap
    and the situation's own merges, with outcomes, the count found and, for `outcome`, a distribution
    shrunk toward the base rate. Without embeddings, feature similarity only (labeled).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        k (int | Unset):  Default: 10.
        outcome (str | Unset): Target outcome: `critical` (situation reaches critical) or
            `{family}/P{n}D` (a family within n days), e.g. `conflict:battles/P30D`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalogResult | Problem
    """

    return sync_detailed(
        situation_id=situation_id,
        client=client,
        k=k,
        outcome=outcome,
    ).parsed


async def asyncio_detailed(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    k: int | Unset = 10,
    outcome: str | Unset = UNSET,
) -> Response[AnalogResult | Problem]:
    """Find historical analogs of a situation

     Returns the top k past situations by fused feature and embedding similarity, excluding time overlap
    and the situation's own merges, with outcomes, the count found and, for `outcome`, a distribution
    shrunk toward the base rate. Without embeddings, feature similarity only (labeled).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        k (int | Unset):  Default: 10.
        outcome (str | Unset): Target outcome: `critical` (situation reaches critical) or
            `{family}/P{n}D` (a family within n days), e.g. `conflict:battles/P30D`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalogResult | Problem]
    """

    kwargs = _get_kwargs(
        situation_id=situation_id,
        k=k,
        outcome=outcome,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    k: int | Unset = 10,
    outcome: str | Unset = UNSET,
) -> AnalogResult | Problem | None:
    """Find historical analogs of a situation

     Returns the top k past situations by fused feature and embedding similarity, excluding time overlap
    and the situation's own merges, with outcomes, the count found and, for `outcome`, a distribution
    shrunk toward the base rate. Without embeddings, feature similarity only (labeled).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        k (int | Unset):  Default: 10.
        outcome (str | Unset): Target outcome: `critical` (situation reaches critical) or
            `{family}/P{n}D` (a family within n days), e.g. `conflict:battles/P30D`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalogResult | Problem
    """

    return (
        await asyncio_detailed(
            situation_id=situation_id,
            client=client,
            k=k,
            outcome=outcome,
        )
    ).parsed
