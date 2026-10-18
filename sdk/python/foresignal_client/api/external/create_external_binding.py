from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.external_binding import ExternalBinding
from ...models.external_binding_input import ExternalBindingInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    source_id: str,
    *,
    body: ExternalBindingInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/external/sources/{source_id}/bindings".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ExternalBinding | Problem | None:
    if response.status_code == 201:
        response_201 = ExternalBinding.from_dict(response.json())

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

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ExternalBinding | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: ExternalBindingInput,
) -> Response[ExternalBinding | Problem]:
    """Create a binding

     Binds a stream to a target (series, canonical family, social, news, evidence, indicator, forecast,
    place, situation, chronicle). Layer and atomic streams may be bound; the stream's item type must fit
    the target (400 BINDING_TARGET_INVALID names both). Backfills from stored records (the newest
    100,000 versions within retention) unless `backfill` is false.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (ExternalBindingInput): Bind an atomic stream to a target (138)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExternalBinding | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: ExternalBindingInput,
) -> ExternalBinding | Problem | None:
    """Create a binding

     Binds a stream to a target (series, canonical family, social, news, evidence, indicator, forecast,
    place, situation, chronicle). Layer and atomic streams may be bound; the stream's item type must fit
    the target (400 BINDING_TARGET_INVALID names both). Backfills from stored records (the newest
    100,000 versions within retention) unless `backfill` is false.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (ExternalBindingInput): Bind an atomic stream to a target (138)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExternalBinding | Problem
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: ExternalBindingInput,
) -> Response[ExternalBinding | Problem]:
    """Create a binding

     Binds a stream to a target (series, canonical family, social, news, evidence, indicator, forecast,
    place, situation, chronicle). Layer and atomic streams may be bound; the stream's item type must fit
    the target (400 BINDING_TARGET_INVALID names both). Backfills from stored records (the newest
    100,000 versions within retention) unless `backfill` is false.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (ExternalBindingInput): Bind an atomic stream to a target (138)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExternalBinding | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: ExternalBindingInput,
) -> ExternalBinding | Problem | None:
    """Create a binding

     Binds a stream to a target (series, canonical family, social, news, evidence, indicator, forecast,
    place, situation, chronicle). Layer and atomic streams may be bound; the stream's item type must fit
    the target (400 BINDING_TARGET_INVALID names both). Backfills from stored records (the newest
    100,000 versions within retention) unless `backfill` is false.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (ExternalBindingInput): Bind an atomic stream to a target (138)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExternalBinding | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            body=body,
        )
    ).parsed
