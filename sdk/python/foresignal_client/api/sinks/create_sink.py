from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.sink_create import SinkCreate
from ...types import Response


def _get_kwargs(
    *,
    body: SinkCreate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/sinks",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: SinkCreate,
) -> Response[Problem]:
    """Create a sink

     Creates an outbound sink, disabled unless `enabled=true`. SERVE sinks require `config.port` and bind
    only to loopback, RFC 1918, link-local or ULA addresses (100.64.0.0/10 with `allowTailnet`;
    `0.0.0.0` only with `allowAllInterfaces`). Every record passes the licence egress guard (109):
    opensky-states, acled and aisstream never reach a sink; LOCAL_ONLY records reach only SERVE sinks on
    loopback or private addresses. Target URLs pass the outbound URL policy (URL_NOT_ALLOWED). Audited.

    **Scope:** `admin`.

    Args:
        body (SinkCreate): Create an outbound sink; sinks are created disabled unless enabled=true
            (110)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
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
    body: SinkCreate,
) -> Problem | None:
    """Create a sink

     Creates an outbound sink, disabled unless `enabled=true`. SERVE sinks require `config.port` and bind
    only to loopback, RFC 1918, link-local or ULA addresses (100.64.0.0/10 with `allowTailnet`;
    `0.0.0.0` only with `allowAllInterfaces`). Every record passes the licence egress guard (109):
    opensky-states, acled and aisstream never reach a sink; LOCAL_ONLY records reach only SERVE sinks on
    loopback or private addresses. Target URLs pass the outbound URL policy (URL_NOT_ALLOWED). Audited.

    **Scope:** `admin`.

    Args:
        body (SinkCreate): Create an outbound sink; sinks are created disabled unless enabled=true
            (110)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: SinkCreate,
) -> Response[Problem]:
    """Create a sink

     Creates an outbound sink, disabled unless `enabled=true`. SERVE sinks require `config.port` and bind
    only to loopback, RFC 1918, link-local or ULA addresses (100.64.0.0/10 with `allowTailnet`;
    `0.0.0.0` only with `allowAllInterfaces`). Every record passes the licence egress guard (109):
    opensky-states, acled and aisstream never reach a sink; LOCAL_ONLY records reach only SERVE sinks on
    loopback or private addresses. Target URLs pass the outbound URL policy (URL_NOT_ALLOWED). Audited.

    **Scope:** `admin`.

    Args:
        body (SinkCreate): Create an outbound sink; sinks are created disabled unless enabled=true
            (110)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: SinkCreate,
) -> Problem | None:
    """Create a sink

     Creates an outbound sink, disabled unless `enabled=true`. SERVE sinks require `config.port` and bind
    only to loopback, RFC 1918, link-local or ULA addresses (100.64.0.0/10 with `allowTailnet`;
    `0.0.0.0` only with `allowAllInterfaces`). Every record passes the licence egress guard (109):
    opensky-states, acled and aisstream never reach a sink; LOCAL_ONLY records reach only SERVE sinks on
    loopback or private addresses. Target URLs pass the outbound URL policy (URL_NOT_ALLOWED). Audited.

    **Scope:** `admin`.

    Args:
        body (SinkCreate): Create an outbound sink; sinks are created disabled unless enabled=true
            (110)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
