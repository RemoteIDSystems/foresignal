from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.eta_result import EtaResult
from ...models.problem import Problem
from ...types import UNSET, Response


def _get_kwargs(
    *,
    record: str,
    to: str,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["record"] = record

    params["to"] = to

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/kinematics/eta",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EtaResult | Problem | None:
    if response.status_code == 200:
        response_200 = EtaResult.from_dict(response.json())

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

    if response.status_code == 422:
        response_422 = Problem.from_dict(response.json())

        return response_422

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[EtaResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    record: str,
    to: str,
) -> Response[EtaResult | Problem]:
    """ETA of a record to a target

     Approaching when the bearing difference is ≤ 45° and speed ≥ 0.5 m/s; then eta = distance / (speed ·
    cos Δ). A place target resolves to the place's point (or the interior point of a polygon or route)
    and needs an agent token, since places are never shown as coordinates to read tokens; read callers
    pass `lon,lat`. Errors: `STATE_TOO_OLD`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        record (str):
        to (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EtaResult | Problem]
    """

    kwargs = _get_kwargs(
        record=record,
        to=to,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    record: str,
    to: str,
) -> EtaResult | Problem | None:
    """ETA of a record to a target

     Approaching when the bearing difference is ≤ 45° and speed ≥ 0.5 m/s; then eta = distance / (speed ·
    cos Δ). A place target resolves to the place's point (or the interior point of a polygon or route)
    and needs an agent token, since places are never shown as coordinates to read tokens; read callers
    pass `lon,lat`. Errors: `STATE_TOO_OLD`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        record (str):
        to (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EtaResult | Problem
    """

    return sync_detailed(
        client=client,
        record=record,
        to=to,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    record: str,
    to: str,
) -> Response[EtaResult | Problem]:
    """ETA of a record to a target

     Approaching when the bearing difference is ≤ 45° and speed ≥ 0.5 m/s; then eta = distance / (speed ·
    cos Δ). A place target resolves to the place's point (or the interior point of a polygon or route)
    and needs an agent token, since places are never shown as coordinates to read tokens; read callers
    pass `lon,lat`. Errors: `STATE_TOO_OLD`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        record (str):
        to (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EtaResult | Problem]
    """

    kwargs = _get_kwargs(
        record=record,
        to=to,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    record: str,
    to: str,
) -> EtaResult | Problem | None:
    """ETA of a record to a target

     Approaching when the bearing difference is ≤ 45° and speed ≥ 0.5 m/s; then eta = distance / (speed ·
    cos Δ). A place target resolves to the place's point (or the interior point of a polygon or route)
    and needs an agent token, since places are never shown as coordinates to read tokens; read callers
    pass `lon,lat`. Errors: `STATE_TOO_OLD`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        record (str):
        to (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EtaResult | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            record=record,
            to=to,
        )
    ).parsed
