from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.base_rate import BaseRate
from ...models.base_rate_input import BaseRateInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: BaseRateInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/base-rates",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> BaseRate | Problem | None:
    if response.status_code == 201:
        response_201 = BaseRate.from_dict(response.json())

        return response_201

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
    body: BaseRateInput,
) -> Response[BaseRate | Problem]:
    """Store a custom base rate

     Computes and stores a custom base-rate definition (`origin` = `operator` or `agent`).

    **Scope:** `agent` or `admin`.

    Args:
        body (BaseRateInput): Store a custom base-rate definition

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
    body: BaseRateInput,
) -> BaseRate | Problem | None:
    """Store a custom base rate

     Computes and stores a custom base-rate definition (`origin` = `operator` or `agent`).

    **Scope:** `agent` or `admin`.

    Args:
        body (BaseRateInput): Store a custom base-rate definition

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
    body: BaseRateInput,
) -> Response[BaseRate | Problem]:
    """Store a custom base rate

     Computes and stores a custom base-rate definition (`origin` = `operator` or `agent`).

    **Scope:** `agent` or `admin`.

    Args:
        body (BaseRateInput): Store a custom base-rate definition

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
    body: BaseRateInput,
) -> BaseRate | Problem | None:
    """Store a custom base rate

     Computes and stores a custom base-rate definition (`origin` = `operator` or `agent`).

    **Scope:** `agent` or `admin`.

    Args:
        body (BaseRateInput): Store a custom base-rate definition

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
