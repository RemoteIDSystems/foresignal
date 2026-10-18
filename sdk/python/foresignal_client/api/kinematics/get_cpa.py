from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.cpa_result import CpaResult
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    a: str,
    b: str,
    horizon: str | Unset = "2h",
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["a"] = a

    params["b"] = b

    params["horizon"] = horizon

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/kinematics/cpa",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> CpaResult | Problem | None:
    if response.status_code == 200:
        response_200 = CpaResult.from_dict(response.json())

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
) -> Response[CpaResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    a: str,
    b: str,
    horizon: str | Unset = "2h",
) -> Response[CpaResult | Problem]:
    """Closest point of approach of two records

     Constant-velocity CPA in a local equirectangular frame at t0 = max(observedAt): tcpa clamped to [0,
    horizon], dcpa, and vertical separation when both have altitude. `stale=true` when a state is older
    than 2 × its source TTL. Errors: `400 CPA_RANGE`, `STATE_TOO_OLD`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        a (str):
        b (str):
        horizon (str | Unset): Search horizon. ISO-8601 duration (P7D, PT15M) or shorthand (7d,
            15m, 12h). Default: '2h'. Example: 2h.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CpaResult | Problem]
    """

    kwargs = _get_kwargs(
        a=a,
        b=b,
        horizon=horizon,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    a: str,
    b: str,
    horizon: str | Unset = "2h",
) -> CpaResult | Problem | None:
    """Closest point of approach of two records

     Constant-velocity CPA in a local equirectangular frame at t0 = max(observedAt): tcpa clamped to [0,
    horizon], dcpa, and vertical separation when both have altitude. `stale=true` when a state is older
    than 2 × its source TTL. Errors: `400 CPA_RANGE`, `STATE_TOO_OLD`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        a (str):
        b (str):
        horizon (str | Unset): Search horizon. ISO-8601 duration (P7D, PT15M) or shorthand (7d,
            15m, 12h). Default: '2h'. Example: 2h.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CpaResult | Problem
    """

    return sync_detailed(
        client=client,
        a=a,
        b=b,
        horizon=horizon,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    a: str,
    b: str,
    horizon: str | Unset = "2h",
) -> Response[CpaResult | Problem]:
    """Closest point of approach of two records

     Constant-velocity CPA in a local equirectangular frame at t0 = max(observedAt): tcpa clamped to [0,
    horizon], dcpa, and vertical separation when both have altitude. `stale=true` when a state is older
    than 2 × its source TTL. Errors: `400 CPA_RANGE`, `STATE_TOO_OLD`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        a (str):
        b (str):
        horizon (str | Unset): Search horizon. ISO-8601 duration (P7D, PT15M) or shorthand (7d,
            15m, 12h). Default: '2h'. Example: 2h.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CpaResult | Problem]
    """

    kwargs = _get_kwargs(
        a=a,
        b=b,
        horizon=horizon,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    a: str,
    b: str,
    horizon: str | Unset = "2h",
) -> CpaResult | Problem | None:
    """Closest point of approach of two records

     Constant-velocity CPA in a local equirectangular frame at t0 = max(observedAt): tcpa clamped to [0,
    horizon], dcpa, and vertical separation when both have altitude. `stale=true` when a state is older
    than 2 × its source TTL. Errors: `400 CPA_RANGE`, `STATE_TOO_OLD`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        a (str):
        b (str):
        horizon (str | Unset): Search horizon. ISO-8601 duration (P7D, PT15M) or shorthand (7d,
            15m, 12h). Default: '2h'. Example: 2h.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CpaResult | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            a=a,
            b=b,
            horizon=horizon,
        )
    ).parsed
