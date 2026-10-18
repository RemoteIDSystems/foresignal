from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.base_rate import BaseRate
from ...models.base_rate_definition import BaseRateDefinition
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: BaseRateDefinition,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/base-rates/query",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> BaseRate | Problem | None:
    if response.status_code == 200:
        response_200 = BaseRate.from_dict(response.json())

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
) -> Response[BaseRate | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: BaseRateDefinition,
) -> Response[BaseRate | Problem]:
    """Compute an ad-hoc base rate

     Computes a base rate for a definition without storing it (cached 24 h); returns a stored rate when
    one matches the definition.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        body (BaseRateDefinition): Reference class of a base rate: family, regions or region
            class, window and optional condition

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BaseRate | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: BaseRateDefinition,
) -> BaseRate | Problem | None:
    """Compute an ad-hoc base rate

     Computes a base rate for a definition without storing it (cached 24 h); returns a stored rate when
    one matches the definition.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        body (BaseRateDefinition): Reference class of a base rate: family, regions or region
            class, window and optional condition

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BaseRate | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: BaseRateDefinition,
) -> Response[BaseRate | Problem]:
    """Compute an ad-hoc base rate

     Computes a base rate for a definition without storing it (cached 24 h); returns a stored rate when
    one matches the definition.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        body (BaseRateDefinition): Reference class of a base rate: family, regions or region
            class, window and optional condition

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BaseRate | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: BaseRateDefinition,
) -> BaseRate | Problem | None:
    """Compute an ad-hoc base rate

     Computes a base rate for a definition without storing it (cached 24 h); returns a stored rate when
    one matches the definition.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        body (BaseRateDefinition): Reference class of a base rate: family, regions or region
            class, window and optional condition

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BaseRate | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
